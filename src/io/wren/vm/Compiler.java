package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.SignatureType;
import io.wren.enums.TokenType;
import io.wren.utils.Buffer;
import io.wren.utils.ByteBuffer;
import io.wren.utils.IntBuffer;
import io.wren.utils.SymbolTable;
import io.wren.utils.Wren;
import io.wren.value.ObjFn;
import io.wren.value.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Compiler {
	private static final int MAX_LOCALS = 256;
	private static final int MAX_UPVALUES = 256;
	private static final int MAX_CONSTANTS = (1 << 16);
	private static final int MAX_PARAMETERS = 16;
	private static final int MAX_METHOD_NAME = 64;
	private static final int MAX_VARIABLE_NAME = 64;

	Parser parser;
	Compiler parent;

	Buffer<Value> constants;

	Local[] locals;
	int numLocals;

	CompilerUpvalue[] upvalues;
	int numUpvalues;

	int numParams;

	int scopeDepth;

	Loop loop;

	ClassCompiler enclosingClass;

	ByteBuffer bytecode;
	IntBuffer debugSourceLines;

	public Compiler(Parser parser, Compiler parent, boolean isFunction) {
		this.parser = parser;
		this.parent = parent;
		this.constants = new Buffer<>();
		this.locals = new Local[MAX_LOCALS];
		this.upvalues = new CompilerUpvalue[MAX_UPVALUES];
		this.bytecode = new ByteBuffer();
		this.debugSourceLines = new IntBuffer();

		for (int i = 0; i < MAX_LOCALS; i++) {
			this.locals[i] = new Local();
		}

		for (int i = 0; i < MAX_UPVALUES; i++) {
			this.upvalues[i] = new CompilerUpvalue();
		}

		if (parent == null) {
			this.scopeDepth = -1;
		} else {
			Local local = locals[0];
			if (!isFunction) {
				local.name = "this";
			}
			local.depth = -1;
			local.isUpvalue = false;

			numLocals = 1;

			this.scopeDepth = 0;
		}
	}

	TokenType peek() {
		return parser.current.type;
	}

	boolean match(TokenType expected) {
		if (peek() != expected) return false;
		parser.nextToken();
		return true;
	}

	void consume(TokenType expected, String errorMessage) {
		parser.nextToken();
		if (parser.previous.type != expected) {
			error(errorMessage);

			if (parser.current.type == expected) parser.nextToken();
		}
	}

	void error(String str) {
		parser.hasError = true;
		if (!parser.printErrors) return;

		Token t = parser.previous;
		if (t.type == TokenType.ERROR) return;

		System.err.print("[" + parser.sourcePath + " line " + t.line + "] Error at ");

		if (t.type == TokenType.LINE) {
			System.err.print("newline: ");
		} else if (t.type == TokenType.EOF) {
			System.err.print("end of file: ");
		} else {
			System.err.print("'" + t.type + "': ");
		}

		System.err.println(str);
	}

	boolean matchLine() {
		if (!match(TokenType.LINE)) return false;

		while (match(TokenType.LINE))
			continue;
		return true;
	}

	void ignoreNewlines() {
		matchLine();
	}

	void consumeLine(String errorMessage) {
		consume(TokenType.LINE, errorMessage);
		ignoreNewlines();
	}

	void definition() {
		if (match(TokenType.CLASS)) {
			classDefinition(false);
		} else if (match(TokenType.FOREIGN)) {
			consume(TokenType.CLASS, "Expect 'class' after 'foreign'.");
			classDefinition(false);
		} else if (match(TokenType.IMPORT)) {
			importDefinition();
		} else if (match(TokenType.VAR)) {
			variableDefinition();
		} else {
			block();
		}
	}

	void block() {
		if (match(TokenType.LEFT_BRACE)) {
			pushScope();
			if (finishBlock()) {
				emit(Code.POP);
			}
			popScope();
			return;
		}

		statement();
	}

	void statement() {
		if (match(TokenType.BREAK)) {
			if (loop == null) {
				error("Cannot use 'break' outside of a loop.");
				return;
			}

			discardLocals(loop.scopeDepth + 1);

			emitJump(Code.END);
			return;
		}

		if (match(TokenType.FOR)) {
			forStatement();
			return;
		}

		if (match(TokenType.IF)) {
			ifStatement();
			return;
		}

		if (match(TokenType.RETURN)) {
			if (peek() == TokenType.LINE) {
				emit(Code.NULL);
			} else {
				expression();
			}

			emit(Code.RETURN);
			return;
		}

		if (match(TokenType.WHILE)) {
			whileStatement();
			return;
		}

		expression();
		emit(Code.POP);
	}

	void whileStatement() {
		Loop loop = new Loop();
		startLoop(loop);

		consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
		expression();
		consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.");

		testExitLoop();
		loopBody();
		endLoop();
	}

	void forStatement() {
		pushScope();

		consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");
		consume(TokenType.NAME, "Expect for loop variable name.");

		String name = parser.previous.value;

		consume(TokenType.IN, "Expect 'in' after loop variable.");
		ignoreNewlines();

		expression();

		int seqSlot = addLocal("seq ");

		emitNull();
		int iterSlot = addLocal("iter ");

		consume(TokenType.RIGHT_PAREN, "Expect ')' after loop expression.");

		Loop loop = new Loop();
		startLoop(loop);

		loadLocal(seqSlot);
		loadLocal(iterSlot);

		callMethod(1, "iterate(_)");

		emitByteArg(Code.STORE_LOCAL, iterSlot);

		testExitLoop();

		loadLocal(seqSlot);
		loadLocal(iterSlot);

		callMethod(1, "iteratorValue(_)");

		pushScope();
		addLocal(name);

		loopBody();

		popScope();

		endLoop();

		popScope();
	}

	void endLoop() {
		int loopOffset = bytecode.count() - loop.start + 2;
		emitShortArg(Code.LOOP, loopOffset);

		patchJump(loop.exitJump);

		int i = loop.body;
		while (i < bytecode.count()) {
			if (bytecode.get(i) == Code.END.toByte()) {
				bytecode.set(i, Code.JUMP);
				patchJump(i + 1);
				i += 3;
			} else {
				i += 1 + Wren.getNumArguments(bytecode, constants, i);
			}
		}
	}

	void loopBody() {
		this.loop.body = bytecode.count();
		block();
	}

	void testExitLoop() {
		this.loop.exitJump = emitJump(Code.JUMP_IF);
	}

	void callMethod(int numArgs, String name) {
		int symbol = methodSymbol(name);
		emitShortArg(Code.CALL_0.add(numArgs), symbol);
	}

	int methodSymbol(String name) {
		return parser.vm.methodNames.ensure(name);
	}

	void loadLocal(int slot) {
		if (slot <= 8) {
			emit(Code.LOAD_LOCAL_0.add(slot));
		} else {
			emitByteArg(Code.LOAD_LOCAL, slot);
		}
	}

	void startLoop(Loop loop) {
		loop.enclosing = this.loop;
		loop.start = bytecode.count() - 1;
		loop.scopeDepth = scopeDepth;
		this.loop = loop;
	}

	void emitNull() {
		emit(Code.NULL);
	}

	int addLocal(String name) {
		Local local = locals[numLocals];
		local.name = name;
		local.depth = scopeDepth;
		local.isUpvalue = false;
		return numLocals++;
	}

	void ifStatement() {
		consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
		expression();
		consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

		int ifJump = emitJump(Code.JUMP_IF);

		block();

		if (match(TokenType.ELSE)) {
			int elseJump = emitJump(Code.JUMP);

			patchJump(ifJump);

			block();

			patchJump(elseJump);
		} else {
			patchJump(ifJump);
		}
	}

	void pushScope() {
		scopeDepth++;
	}

	void popScope() {
		numLocals -= discardLocals(scopeDepth);
		scopeDepth--;
	}

	int discardLocals(int depth) {
		int local = numLocals - 1;
		while (local >= 0 && locals[local].depth >= depth) {
			if (locals[local].isUpvalue) {
				emit(Code.CLOSE_UPVALUE);
			} else {
				emit(Code.POP);
			}

			local--;
		}

		return numLocals - local - 1;
	}

	boolean finishBlock() {
		if (match(TokenType.RIGHT_BRACE)) {
			return false;
		}

		if (!matchLine()) {
			expression();
			consume(TokenType.RIGHT_BRACE, "Expect '}' at end of block.");
			return true;
		}

		if (match(TokenType.RIGHT_BRACE)) {
			return false;
		}

		do {
			definition();

			if (peek() == TokenType.EOF) return true;
			consumeLine("Expect newline after statement.");
		} while (!match(TokenType.RIGHT_BRACE));
		return false;
	}

	void finishBody(boolean isInitializer) {
		boolean isExpressionBody = finishBlock();

		if (isInitializer) {
			if (isExpressionBody) emit(Code.POP);

			emit(Code.LOAD_LOCAL_0);
		} else if (!isExpressionBody) {
			emit(Code.NULL);
		}

		emit(Code.RETURN);
	}

	void validateNumParameters(int numArgs) {
		if (numArgs == MAX_PARAMETERS + 1) {
			error(String.format("Methods cannot have more than %d parameters.", MAX_PARAMETERS));
		}
	}

	void expression() {
		parsePrecedence(true, Precedence.LOWEST);
	}

	void variableDefinition() {
		consume(TokenType.NAME, "Expect variable name.");
		Token nameToken = parser.previous;

		if (match(TokenType.EQ)) {
			expression();
		} else {
			emitNull();
		}

		int symbol = declareVariable(nameToken);
		defineVariable(symbol);
	}

	void defineVariable(int symbol) {
		if (scopeDepth >= 0) return;

		emitShortArg(Code.STORE_MODULE_VAR, symbol);
		emit(Code.POP);
	}

	void importDefinition() {
		consume(TokenType.STRING, "Expect a string after 'import'.");
		int moduleConstant = addConstant(parser.value);

		emitShortArg(Code.LOAD_MODULE, moduleConstant);

		emit(Code.POP);

		if (!match(TokenType.FOR)) return;

		do {
			int slot = declareNamedVariable();

			int variableConstant = addConstant(new Value(parser.previous.value));

			emitShortArg(Code.IMPORT_VARIABLE, moduleConstant);
			emitShort(variableConstant);

			defineVariable(slot);
		} while (match(TokenType.COMMA));

	}

	void classDefinition(boolean isForeign) {
		int slot = declareNamedVariable();

		emitConstant(new Value(parser.previous.value));

		if (match(TokenType.IS)) {
			parsePrecedence(false, Precedence.CALL);
		} else {
			loadCoreVariable("Object");
		}

		int numFieldsInstruction = -1;
		if (isForeign) {
			emit(Code.FOREIGN_CLASS);
		} else {
			numFieldsInstruction = emitByteArg(Code.CLASS, 255);
		}

		defineVariable(slot);

		pushScope();

		ClassCompiler classCompiler = new ClassCompiler();
		classCompiler.isForeign = isForeign;

		SymbolTable fields = new SymbolTable();
		classCompiler.fields = fields;

		enclosingClass = classCompiler;

		consume(TokenType.LEFT_BRACE, "Expect '{' after class declaration.");
		matchLine();

		while (!match(TokenType.RIGHT_BRACE)) {
			if (!method(classCompiler, slot)) break;

			if (match(TokenType.RIGHT_BRACE)) break;

			consumeLine("Expect newline after definition in class.");
		}

		if (!isForeign) {
			bytecode.set(numFieldsInstruction, (byte) fields.count());
		}

		enclosingClass = null;

		popScope();
	}

	boolean method(ClassCompiler classCompiler, int classSlot) {
		boolean isForeign = match(TokenType.FOREIGN);
		classCompiler.inStatic = match(TokenType.STATIC);

		GrammarRule rule = getRule(parser.current);
		parser.nextToken();

		Signature signature = new Signature(parser.previous.value, SignatureType.GETTER);
		classCompiler.signature = signature;

		Compiler methodCompiler = new Compiler(parser, this, false);

		try {
			rule.method(methodCompiler, signature);
		} catch (NotImplementedException e) {
			error("Expect method definition.");
			return false;
		}

		if (classCompiler.inStatic && signature.type == SignatureType.INITIALIZER) {
			error("A constructor cannot be static.");
		}

		String fullSignature = signature.toString();

		if (isForeign) {
			emitConstant(new Value(fullSignature));
		} else {
			consume(TokenType.LEFT_BRACE, "Expect '{' to begin method body.");
			methodCompiler.finishBody(signature.type == SignatureType.INITIALIZER);
			methodCompiler.endCompiler(fullSignature);
		}

		int methodSymbol = signatureSymbol(signature);
		defineMethod(classSlot, classCompiler.inStatic, methodSymbol);

		if (signature.type == SignatureType.INITIALIZER) {
			signature.type = SignatureType.METHOD;
			int constructorSymbol = signatureSymbol(signature);

			createConstructor(signature, methodSymbol);
			defineMethod(classSlot, true, constructorSymbol);
		}

		return true;
	}

	void createConstructor(Signature signature, int initializerSymbol) {
		Compiler methodCompiler = new Compiler(parser, this, false);

		methodCompiler.emit(enclosingClass.isForeign ? Code.FOREIGN_CONSTRUCT : Code.CONSTRUCT);

		methodCompiler.emitShortArg(Code.CALL_0.add(signature.arity), initializerSymbol);

		methodCompiler.emit(Code.RETURN);

		methodCompiler.endCompiler("");
	}

	void defineMethod(int classSlot, boolean isStatic, int methodSymbol) {
		if (scopeDepth == 0) {
			emitShortArg(Code.LOAD_MODULE_VAR, classSlot);
		} else {
			loadLocal(classSlot);
		}

		Code instruction = isStatic ? Code.METHOD_STATIC : Code.METHOD_INSTANCE;
		emitShortArg(instruction, methodSymbol);
	}

	void loadCoreVariable(String name) {
		int symbol = parser.module.variableNames.find(name);
		emitShortArg(Code.LOAD_MODULE_VAR, symbol);
	}

	void emitConstant(Value value) {
		int constant = addConstant(value);
		emitShortArg(Code.CONSTANT, constant);
	}

	int emit(Code instruction) {
		return emit(instruction.ordinal());
	}

	int emit(int i) {
		byte b = (byte) i;
		bytecode.write(b);

		debugSourceLines.write(parser.previous.line);

		return bytecode.count() - 1;
	}

	void emitShort(int arg) {
		emit((byte) (arg >> 8));
		emit((byte) (arg));
	}

	int emitByteArg(Code instruction, int arg) {
		emit(instruction);
		return emit(arg);
	}

	void emitShortArg(Code instruction, int arg) {
		emit(instruction);
		emitShort(arg);
	}

	int emitJump(Code instruction) {
		emit(instruction);
		emit(0xff);
		return emit(0xff) - 1;
	}

	ObjFn endCompiler(String string) {
		if (parser.hasError) {
			return null;
		}

		emit(Code.END);

		ObjFn fn = new ObjFn();
		fn.bytecode = bytecode;
		fn.constants = constants;
		fn.module = parser.module;
		fn.numUpvalues = numUpvalues;
		fn.numConstants = constants.count();
		fn.arity = numParams;
		fn.bytecodeLength = bytecode.count();

		if (parent != null) {
			int constant = parent.addConstant(new Value(fn));
			if (numUpvalues == 0) {
				parent.emitShortArg(Code.CONSTANT, constant);
			} else {
				emitShortArg(Code.CLOSURE, constant);

				for (int i = 0; i < numUpvalues; i++) {
					parent.emit(upvalues[i].isLocal ? 1 : 0);
					parent.emit(upvalues[i].index);
				}
			}
		}

		parser.vm.compiler = parent;

		return fn;
	}

	void parsePrecedence(boolean allowAssignment, Precedence precedence) {
		parser.nextToken();

		GrammarRule previous = getRule(parser.previous);

		try {
			previous.prefix(this, allowAssignment);
		} catch (NotImplementedException e) {
			error("Expected expression.");
			return;
		}

		GrammarRule current = getRule(parser.current);
		while (precedence.le(current.precedence)) {
			parser.nextToken();

			previous = getRule(parser.previous);

			previous.infix(this, allowAssignment);

			current = getRule(parser.current);
		}
	}

	private GrammarRule[] rules = new GrammarRule[] {
			new LeftParenRule(),
			new UnusedRule(),
			new LeftBracketRule(),
			new UnusedRule(),
			new LeftBraceRule(),
			new UnusedRule(),
			new UnusedRule(),
			new DotRule(),
			new InfixOperatorRule(Precedence.RANGE, ".."),
			new InfixOperatorRule(Precedence.RANGE, "..."),
			new UnusedRule(),
			new InfixOperatorRule(Precedence.FACTOR, "*"),
			new InfixOperatorRule(Precedence.FACTOR, "/"),
			new InfixOperatorRule(Precedence.FACTOR, "%"),
			new InfixOperatorRule(Precedence.TERM, "+"),
			new OperatorRule("-"),
			new InfixOperatorRule(Precedence.BITWISE_SHIFT, "<<"),
			new InfixOperatorRule(Precedence.BITWISE_SHIFT, ">>"),
			new InfixOperatorRule(Precedence.BITWISE_OR, "|"),
			new PipePipeRule(),
			new InfixOperatorRule(Precedence.BITWISE_XOR, "^"),
			new InfixOperatorRule(Precedence.BITWISE_AND, "&"),
			new AmpAmpRule(),
			new PrefixOperatorRule("!"),
			new PrefixOperatorRule("~"),
			new QuestionRule(),
			new UnusedRule(),
			new InfixOperatorRule(Precedence.COMPARISON, "<"),
			new InfixOperatorRule(Precedence.COMPARISON, ">"),
			new InfixOperatorRule(Precedence.COMPARISON, "<="),
			new InfixOperatorRule(Precedence.COMPARISON, ">="),
			new InfixOperatorRule(Precedence.EQUALITY, "=="),
			new InfixOperatorRule(Precedence.EQUALITY, "!="),
			new UnusedRule(),
			new UnusedRule(),
			new ConstructRule(),
			new UnusedRule(),
			new BooleanRule(false),
			new UnusedRule(),
			new UnusedRule(),
			new UnusedRule(),
			new UnusedRule(),
			new UnusedRule(),
			new InfixOperatorRule(Precedence.IS, "is"),
			new NullRule(),
			new UnusedRule(),
			new UnusedRule(),
			new SuperRule(),
			new ThisRule(),
			new BooleanRule(true),
			new UnusedRule(),
			new UnusedRule(),
			new FieldRule(),
			new StaticFieldRule(),
			new NameRule(),
			new LiteralRule(),
			new LiteralRule(),
			new UnusedRule(),
			new UnusedRule(),
			new UnusedRule()
	};

	GrammarRule getRule(Token token) {
		return getRule(token.type);
	}

	GrammarRule getRule(TokenType type) {
		return rules[type.ordinal()];
	}

	void patchJump(int offset) {
		int jump = bytecode.count() - offset - 2;
		bytecode.set(offset, (byte) (jump >> 8));
		bytecode.set(offset + 1, (byte) (jump));
	}

	Signature signatureFromToken(SignatureType type) {
		Signature signature = new Signature();

		Token token = parser.previous;
		signature.name = token.value;
		signature.type = type;
		signature.arity = 0;

		if (signature.name.length() > MAX_METHOD_NAME) {
			error(String.format("Method names cannot be longer than %d characters.", MAX_METHOD_NAME));
			signature.name = signature.name.substring(0, MAX_METHOD_NAME);
		}

		return signature;
	}

	void finishParameterList(Signature signature) {
		do {
			ignoreNewlines();
			validateNumParameters(++signature.arity);
			declareNamedVariable();
		} while (match(TokenType.COMMA));
	}

	void namedCall(boolean allowAssignment, Code instruction) {
		Signature signature = signatureFromToken(SignatureType.GETTER);

		if (match(TokenType.EQ)) {
			if (!allowAssignment) error("Invalid assignment.");

			ignoreNewlines();

			signature.type = SignatureType.SETTER;
			signature.arity = 1;

			expression();
			callSignature(instruction, signature);
		} else {
			methodCall(instruction, signature);
		}
	}

	void methodCall(Code instruction, Signature signature) {
		Signature called = new Signature(signature.name, SignatureType.GETTER);

		if (match(TokenType.LEFT_PAREN)) {
			called.type = SignatureType.METHOD;

			if (peek() != TokenType.RIGHT_PAREN) {
				finishArgumentList(called);
			}
			consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
		}

		if (match(TokenType.LEFT_BRACE)) {
			called.type = SignatureType.METHOD;
			called.arity++;

			Compiler fnCompiler = new Compiler(parser, this, true);

			Signature fnSignature = new Signature(SignatureType.METHOD);

			if (match(TokenType.PIPE)) {
				fnCompiler.finishParameterList(fnSignature);
				consume(TokenType.PIPE, "Expect '|' after function parameters.");
			}

			fnCompiler.numParams = fnSignature.arity;

			fnCompiler.finishBody(false);

			String blockName = called.toString() + " block argument";
			fnCompiler.endCompiler(blockName);
		}

		if (signature.type == SignatureType.INITIALIZER) {
			if (called.type == SignatureType.METHOD) {
				error("A superclass constructor must have an argument list.");
			}

			called.type = SignatureType.INITIALIZER;
		}

		callSignature(instruction, called);
	}

	void finishArgumentList(Signature signature) {
		do {
			ignoreNewlines();
			validateNumParameters(++signature.arity);
			expression();
		} while (match(TokenType.COMMA));

		ignoreNewlines();
	}

	void callSignature(Code instruction, Signature signature) {
		int symbol = signatureSymbol(signature);
		emitShortArg(instruction.add(signature.arity), symbol);

		if (instruction == Code.SUPER_0) {
			emitShort(addConstant(Value.NULL));
		}
	}

	int addConstant(Value constant) {
		if (parser.hasError) return -1;

		if (constants.count() < MAX_CONSTANTS) {
			constants.write(constant);
		} else {
			error(String.format("A function may only contain %d unique constants.", MAX_CONSTANTS));
		}

		return constants.count() - 1;
	}

	int signatureSymbol(Signature signature) {
		String name = signature.toString();
		return methodSymbol(name);
	}

	int declareNamedVariable() {
		consume(TokenType.NAME, "Expect variable name.");
		return declareVariable(null);
	}

	int declareVariable(Token token) {
		if (token == null) token = parser.previous;

		String tokenName = token.value;
		if (tokenName.length() > MAX_VARIABLE_NAME) {
			error(String.format("Variable name cannot be longer than %d characters.", MAX_VARIABLE_NAME));
		}

		if (scopeDepth == -1) {
			int symbol = parser.module.defineVariable(tokenName, Value.NULL);

			if (symbol == -1) {
				error("Module variable is already defined.");
			} else if (symbol == -2) {
				error("Too many module variables defined.");
			}

			return symbol;
		}

		for (int i = numLocals - 1; i >= 0; i--) {
			Local local = locals[i];

			if (local.depth < scopeDepth) break;

			if (tokenName.equals(local.name)) {
				error("Variable is already declared in this scope.");
				return i;
			}
		}

		if (numLocals == MAX_LOCALS) {
			error(String.format("Cannot declare more than %d variables in one scope.", MAX_LOCALS));
			return -1;
		}

		return addLocal(tokenName);
	}

	void loadThis() {
		Instruction loadInstruction = new Instruction();
		int index = resolveNonmodule("this", loadInstruction);
		Code instruction = loadInstruction.code;

		if (instruction == Code.LOAD_LOCAL) {
			loadLocal(index);
		} else {
			emitByteArg(instruction, index);
		}
	}

	void variable(boolean allowAssignment, int index, Code loadInstruction) {
		if (match(TokenType.EQ)) {
			if (!allowAssignment) error("Invalid assignment.");

			expression();

			switch (loadInstruction) {
				case LOAD_LOCAL:
					emitByteArg(Code.STORE_LOCAL, index);
					break;
				case LOAD_UPVALUE:
					emitByteArg(Code.STORE_UPVALUE, index);
					break;
				case LOAD_MODULE_VAR:
					emitByteArg(Code.STORE_MODULE_VAR, index);
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else if (loadInstruction == Code.LOAD_MODULE_VAR) {
			emitShortArg(loadInstruction, index);
		} else if (loadInstruction == Code.LOAD_LOCAL) {
			loadLocal(index);
		} else {
			emitByteArg(loadInstruction, index);
		}
	}

	int resolveNonmodule(String name, Instruction loadInstruction) {
		loadInstruction.code = Code.LOAD_LOCAL;

		int local = resolveLocal(name);
		if (local != -1) return local;

		loadInstruction.code = Code.LOAD_UPVALUE;
		return findUpvalue(name);
	}

	private int findUpvalue(String name) {
		if (parent == null || enclosingClass == null) return -1;

		int local = parent.resolveLocal(name);
		if (local != -1) {
			parent.locals[local].isUpvalue = true;

			return addUpvalue(true, local);
		}

		int upvalue = parent.findUpvalue(name);
		if (upvalue != -1) {
			return addUpvalue(false, upvalue);
		}

		return -1;
	}

	private int addUpvalue(boolean isLocal, int index) {
		for (int i = 0; i < numUpvalues; i++) {
			CompilerUpvalue upvalue = upvalues[i];
			if (upvalue.index == index && upvalue.isLocal == isLocal) return i;
		}

		upvalues[numUpvalues].isLocal = isLocal;
		upvalues[numUpvalues].index = index;

		return numUpvalues++;
	}

	int resolveLocal(String name) {
		for (int i = numLocals - 1; i >= 0; i--) {
			if (name.equals(locals[i].name)) {
				return i;
			}
		}

		return -1;
	}

	void parameterList(Signature signature) {
		if (!match(TokenType.LEFT_PAREN)) return;

		signature.type = SignatureType.METHOD;

		if (match(TokenType.RIGHT_PAREN)) return;

		finishParameterList(signature);
		consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
	}

	boolean maybeSetter(Signature signature) {
		if (!match(TokenType.EQ)) return false;

		if (signature.type == SignatureType.SUBSCRIPT) {
			signature.type = SignatureType.SUBSCRIPT_SETTER;
		} else {
			signature.type = SignatureType.SETTER;
		}

		consume(TokenType.LEFT_PAREN, "Expect '(' after '='.");
		declareNamedVariable();
		consume(TokenType.RIGHT_PAREN, "Expect ')' after parameter name.");

		signature.arity++;

		return true;
	}

	Compiler getEnclosingClassCompiler() {
		Compiler compiler = this;
		while (compiler != null) {
			if (compiler.enclosingClass != null) return compiler;
			compiler = compiler.parent;
		}

		return null;
	}

	int resolveName(String name, Instruction loadInstruction) {
		int nonmodule = resolveNonmodule(name, loadInstruction);
		if (nonmodule != -1) return nonmodule;

		loadInstruction.code = Code.LOAD_MODULE_VAR;
		return parser.module.variableNames.find(name);
	}

	public ClassCompiler getEnclosingClass() {
		Compiler compiler = getEnclosingClassCompiler();
		return compiler == null ? null : compiler.enclosingClass;
	}
}
