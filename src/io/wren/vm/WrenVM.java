package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.MethodType;
import io.wren.enums.TokenType;
import io.wren.enums.WrenInterpretResult;
import io.wren.libs.WrenBool;
import io.wren.libs.WrenClass;
import io.wren.libs.WrenFiber;
import io.wren.libs.WrenFn;
import io.wren.libs.WrenList;
import io.wren.libs.WrenMap;
import io.wren.libs.WrenMeta;
import io.wren.libs.WrenNull;
import io.wren.libs.WrenNum;
import io.wren.libs.WrenObject;
import io.wren.libs.WrenString;
import io.wren.libs.WrenSystem;
import io.wren.utils.Buffer;
import io.wren.utils.ByteBuffer;
import io.wren.utils.SymbolTable;
import io.wren.utils.UnreachableCodeException;
import io.wren.utils.Wren;
import io.wren.value.CallFrame;
import io.wren.value.Method;
import io.wren.value.ObjClass;
import io.wren.value.ObjClosure;
import io.wren.value.ObjFiber;
import io.wren.value.ObjFn;
import io.wren.value.ObjInstance;
import io.wren.value.ObjMap;
import io.wren.value.ObjModule;
import io.wren.value.ObjString;
import io.wren.value.ObjUpvalue;
import io.wren.value.Value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WrenVM {
	public static ObjFiber fiber;
	ObjMap modules;
	short nextFiberId;
	int foreignCallNumArgs;

	Compiler compiler;
	SymbolTable methodNames;

	private static ObjClass objectClass;
	private static ObjClass classClass;
	private static ObjClass boolClass;
	private static ObjClass nullClass;
	private static ObjClass numClass;
	public static ObjClass listClass;
	public static ObjClass mapClass;
	public static ObjClass stringClass;
	public static ObjClass fiberClass;
	public static ObjClass rangeClass;
	public static ObjClass fnClass;

	public WrenVM() {
		modules = new ObjMap();
		methodNames = new SymbolTable();

		ObjModule coreModule = new ObjModule("core");
		modules.set(Value.NULL, new Value(coreModule));

		initializeCore();
		initializeMeta();
	}

	ObjModule getModule(Value name) {
		Value moduleValue = modules.get(name);
		if (moduleValue.isUndefined()) return null;
		return moduleValue.asModule();
	}

	ObjModule getCoreModule() {
		ObjModule module = getModule(Value.NULL);
		if (module == null) new RuntimeException("NO CORE MODULE");
		return module;
	}

	Value importModule(String name) {
		throw new NotImplementedException();
	}

	Value findVariable(ObjModule module, String name) {
		int symbol = module.variableNames.find(name);
		return module.variables.get(symbol);
	}

	ObjFn compile(ObjModule module, String sourcePath, String source, boolean printErrors) {
		Parser parser = new Parser();
		parser.vm = this;
		parser.module = module;
		parser.sourcePath = sourcePath;
		parser.source = source;
		parser.value = Value.UNDEFINED;

		parser.tokenStart = 0;
		parser.currentChar = 0;
		parser.currentLine = 1;

		parser.current = new Token();

		parser.skipNewlines = true;
		parser.printErrors = printErrors;
		parser.hasError = false;

		parser.nextToken();

		Compiler compiler = new Compiler(parser, null, true);
		compiler.ignoreNewlines();

		while (!compiler.match(TokenType.EOF)) {
			compiler.definition();

			if (!compiler.matchLine()) {
				compiler.consume(TokenType.EOF, "Expect end of file.");
				break;
			}
		}

		compiler.emit(Code.NULL);
		compiler.emit(Code.RETURN);

		Buffer<Value> variables = parser.module.variables;
		SymbolTable variableNames = parser.module.variableNames;
		for (int i = 0; i < variables.count(); i++) {
			if (variables.get(i).isUndefined()) {
				compiler.error(String.format("Variable '%s' is used but not defined.", variableNames.get(i)));
			}
		}

		return compiler.endCompiler("(script)");
	}

	public WrenInterpretResult interpret(String sourcePath, String source) {
		if (sourcePath.length() == 0) return loadIntoCore(source);

		ObjFiber fiber = loadModule("main", source);
		if (fiber == null) {
			return WrenInterpretResult.RESULT_COMPILE_ERROR;
		}

		WrenInterpretResult result = runInterpreter(fiber);
		return result;
	}

	private CallFrame frame;
	private int stackStart;
	private int ip;
	private ObjFn fn;
	private ObjClosure closure;
	private ByteBuffer bytecode;

	private void PUSH(Value value) {
		fiber.stack[fiber.stackTop++] = value;
	}

	private Value POP() {
		return fiber.stack[--fiber.stackTop];
	}

	private void DROP() {
		fiber.stackTop--;
	}

	private Value PEEK() {
		return fiber.stack[fiber.stackTop - 1];
	}

	private Value PEEK2() {
		return fiber.stack[fiber.stackTop - 2];
	}

	private short READ_BYTE() {
		short x = bytecode.get(ip++);
		if (x < 0) x += 256; // make the value unsigned
		return x;
	}

	private int READ_SHORT() {
		return (READ_BYTE() << 8 | READ_BYTE());
	}
	
	private void LOAD_FRAME() {
		frame = fiber.frames.get(fiber.frames.size() - 1);
		stackStart = frame.stackStart;
		ip = frame.ip;
		fn = frame.getFn();
		closure = frame.maybeClosure();
		bytecode = fn.bytecode;
	}
	
	private void STORE_FRAME() {
		frame.ip = ip;
	}

	private WrenInterpretResult runInterpreter(ObjFiber fiber) {
		WrenVM.fiber = fiber;

		Code instruction;
		int index;
		
		LOAD_FRAME();

		int offset;
		Value condition;
		short field;
		Value receiver;
		ObjInstance instance;
		ObjClass classObj;

		while (true) {
			instruction = Code.fromByte(bytecode.get(ip++));
			switch (instruction) {
				case AND:
					offset = READ_SHORT();
					condition = PEEK();

					if (condition.isFalse() || condition.isNull()) {
						ip += offset;
					} else {
						DROP();
					}
					break;
				case CALL_0:
				case CALL_1:
				case CALL_2:
				case CALL_3:
				case CALL_4:
				case CALL_5:
				case CALL_6:
				case CALL_7:
				case CALL_8:
				case CALL_9:
				case CALL_10:
				case CALL_11:
				case CALL_12:
				case CALL_13:
				case CALL_14:
				case CALL_15:
				case CALL_16:
				case SUPER_0:
				case SUPER_1:
				case SUPER_2:
				case SUPER_3:
				case SUPER_4:
				case SUPER_5:
				case SUPER_6:
				case SUPER_7:
				case SUPER_8:
				case SUPER_9:
				case SUPER_10:
				case SUPER_11:
				case SUPER_12:
				case SUPER_13:
				case SUPER_14:
				case SUPER_15:
				case SUPER_16:
					int numArgs;
					int argsStart;

					int symbol = READ_SHORT();

					if (instruction.ordinal() < Code.SUPER_0.ordinal()) {
						numArgs = instruction.add(-Code.CALL_0.ordinal() + 1).ordinal();
						argsStart = fiber.stackTop - numArgs;

						classObj = getInlineClass(fiber.stack[argsStart]);
					} else {
						numArgs = instruction.add(-Code.SUPER_0.ordinal() + 1).ordinal();
						argsStart = fiber.stackTop - numArgs;

						int idx = READ_SHORT();
						classObj = fn.constants.get(idx).asClass();
					}

					if (symbol >= classObj.methods.count() || classObj.methods.get(symbol).type == MethodType.NONE) {
						fiber.error = new Value(classObj + " does not implement '" + methodNames.get(symbol) + "'.");
						RUNTIME_ERROR();
					}

					Method method = classObj.methods.get(symbol);
					switch (method.type) {
						case BLOCK: {
							STORE_FRAME();

							fiber.appendCallFrame(method.getFn(), fiber.stackTop - numArgs);

							LOAD_FRAME();
							break;
						}
						case FOREIGN:
							callForeign(method, numArgs);
							break;
						case PRIMITIVE:
							if (callPrimitive(method, numArgs)) {
								fiber.stackTop -= (numArgs - 1);
							} else {
								STORE_FRAME();

								fiber = WrenVM.fiber;
								if (fiber == null) return WrenInterpretResult.RESULT_SUCCESS;

								if (fiber.error != null) RUNTIME_ERROR();

								LOAD_FRAME();
							}
							break;
						case CALL:
							if (!checkArity(fiber.stack[argsStart], numArgs)) RUNTIME_ERROR();

							STORE_FRAME();

							fiber.appendCallFrame(fiber.stack[argsStart].asObj(), fiber.stackTop - numArgs);

							LOAD_FRAME();
							break;
						case NONE:
						default:
							throw new UnreachableCodeException();
					}
					break;
				case CLASS:
					createClass(READ_BYTE(), null);
					if (fiber.error != null) RUNTIME_ERROR();
					break;
				case CLOSE_UPVALUE:
					fiber.openUpvalues.poll();
					DROP();
					break;
				case CLOSURE:
					throw new NotImplementedException();
					// break;
				case CONSTANT:
					PUSH(fn.constants.get(READ_SHORT()));
					break;
				case CONSTRUCT:
					fiber.stack[stackStart] = new Value(new ObjInstance(fiber.stack[stackStart].asClass()));
					break;
				case DUP:
					Value value = PEEK();
					PUSH(value);
					break;
				case END:
					throw new IllegalStateException("END opcode should be unreachable.");
				case FALSE:
					PUSH(Value.FALSE);
					break;
				case FOREIGN_CLASS:
					throw new NotImplementedException();
					// break;
				case FOREIGN_CONSTRUCT:
					throw new NotImplementedException();
					// break;
				case IMPORT_VARIABLE:
					break;
				case JUMP:
					offset = READ_SHORT();
					ip += offset;
					break;
				case JUMP_IF:
					offset = READ_SHORT();
					condition = POP();

					if (condition.isFalse() || condition.isNull()) ip += offset;
					break;
				case LOAD_FIELD:
					field = READ_BYTE();
					receiver = POP();
					instance = receiver.asInstance();
					PUSH(instance.fields[field]);
					break;
				case LOAD_FIELD_THIS:
					field = READ_BYTE();
					receiver = fiber.stack[stackStart];
					instance = receiver.asInstance();
					PUSH(instance.fields[field]);
					break;
				case LOAD_LOCAL:
					index = stackStart + READ_BYTE();
					PUSH(fiber.stack[index]);
					break;
				case LOAD_LOCAL_0:
				case LOAD_LOCAL_1:
				case LOAD_LOCAL_2:
				case LOAD_LOCAL_3:
				case LOAD_LOCAL_4:
				case LOAD_LOCAL_5:
				case LOAD_LOCAL_6:
				case LOAD_LOCAL_7:
				case LOAD_LOCAL_8:
					index = stackStart + instruction.ordinal() - Code.LOAD_LOCAL_0.ordinal();
					PUSH(fiber.stack[index]);
					break;
				case LOAD_MODULE:
					throw new NotImplementedException();
					// break;
				case LOAD_MODULE_VAR:
					PUSH(fn.module.variables.get(READ_SHORT()));
					break;
				case LOAD_UPVALUE:
					PUSH(closure.upvalues[READ_BYTE()].value);
					break;
				case LOOP:
					offset = READ_SHORT();
					ip -= offset;
					break;
				case METHOD_INSTANCE:
				case METHOD_STATIC:
					symbol = READ_SHORT();
					classObj = PEEK().asClass();
					Value methodVal = PEEK2();
					bindMethod(instruction, symbol, fn.module, classObj, methodVal);
					if (fiber.error != null) RUNTIME_ERROR();
					DROP();
					DROP();
					break;
				case NULL:
					PUSH(Value.NULL);
					break;
				case OR:
					offset = READ_SHORT();
					condition = PEEK();

					if (condition.isFalse() || condition.isNull()) {
						DROP();
					} else {
						ip += offset;
					}
					break;
				case POP:
					DROP();
					break;
				case RETURN:
					Value result = POP();
					fiber.frames.pop();

					Value first = fiber.stack[stackStart];
					while (!fiber.openUpvalues.isEmpty() && fiber.openUpvalues.peek().value != first) {
						fiber.openUpvalues.poll();
					}

					if (fiber.frames.size() == 0) {
						if (fiber.caller == null) return WrenInterpretResult.RESULT_SUCCESS;

						ObjFiber callingFiber = fiber.caller;
						fiber.caller = null;

						fiber = callingFiber;
						WrenVM.fiber = fiber;

						fiber.stack[fiber.stackTop - 1] = result;
					} else {
						fiber.stack[stackStart] = result;
						fiber.stackTop = frame.stackStart + 1;
					}

					// load frame
					frame = fiber.frames.get(fiber.frames.size() - 1);
					stackStart = frame.stackStart;
					ip = frame.ip;
					fn = frame.getFn();
					closure = frame.maybeClosure();
					bytecode = fn.bytecode;
					break;
				case STORE_FIELD:
					field = READ_BYTE();
					receiver = POP();
					instance = receiver.asInstance();
					instance.fields[field] = PEEK();
					break;
				case STORE_FIELD_THIS:
					field = READ_BYTE();
					receiver = fiber.stack[stackStart];
					instance = receiver.asInstance();
					instance.fields[field] = PEEK();
					break;
				case STORE_LOCAL:
					index = stackStart + READ_BYTE();
					fiber.stack[index] = PEEK();
					break;
				case STORE_MODULE_VAR:
					fn.module.variables.set(READ_SHORT(), PEEK());
					break;
				case STORE_UPVALUE:
					ObjUpvalue[] upvalues = closure.upvalues;
					upvalues[bytecode.get(ip++)].value = fiber.stack[fiber.stackTop - 1];
					break;
				case TRUE:
					PUSH(Value.TRUE);
					break;
				default:
					break;
			}
		}
	}

	private boolean checkArity(Value value, int numArgs) {
		ObjFn fn = value.asFnOrClosure();
		if (numArgs - 1 >= fn.arity) return true;

		fiber.error = new Value("Function expects more arguments.");
		return false;
	}

	private Value bindMethod(Code instruction, int symbol, ObjModule module, ObjClass classObj, Value methodVal) {
		Method method = new Method();
		if (methodVal.isString()) {
			String name = methodVal.asString().value;
			method.type = MethodType.FOREIGN;
			method.nativeMethod = null;// findForeignMethod();

			if (method.nativeMethod == null) {
				return new Value("Could not find foreign method '" + name + "' for class " + classObj.name + " in module '" + module.name + "'.");
			}
		} else {
			ObjFn methodFn = methodVal.asFnOrClosure();
			classObj.bindMethodCode(methodFn);
			method.type = MethodType.BLOCK;
			method.fn = methodVal.asObj();
		}

		if (instruction == Code.METHOD_STATIC) classObj = classObj.classObj;
		classObj.bindMethod(symbol, method);
		return Value.NULL;
	}

	private boolean callPrimitive(Method method, int numArgs) {
		Object[] params = new Object[3];
		params[0] = fiber.stack;
		params[1] = fiber.stackTop - numArgs;
		params[2] = numArgs;

		boolean returned = false;
		try {
			returned = (boolean) method.nativeMethod.invoke(null, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.out.println(e.getCause());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		return returned;
	}

	private void callForeign(Method method, int numArgs) {
		numArgs--; // remove the caller on foreign methods.
		Object[] params = new Object[numArgs];
		for (int i = 0; i < numArgs; i++) {
			int index = fiber.stackTop - numArgs + i;
			params[i] = fiber.stack[index];
		}

		Value returned = Value.UNDEFINED;
		try {
			returned = (Value) method.nativeMethod.invoke(null, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.out.println(e.getCause());
			e.printStackTrace();
		}

		numArgs++; // add the caller back.
		fiber.stackTop -= numArgs - 1;
		fiber.stack[fiber.stackTop - 1] = returned;
	}

	private void createClass(int numFields, ObjModule module) {
		Value name = PEEK2();
		Value superclass = PEEK();

		fiber.stackTop--;

		fiber.error = validateSuperclass(name, superclass, numFields);
		if (fiber.error != null) return;

		ObjClass classObj = newClass(superclass.asClass(), numFields, name.asString().value);
		fiber.stack[fiber.stackTop - 1] = new Value(classObj);

		if (numFields == -1) bindForeignClass(classObj, module);
	}

	private void bindForeignClass(ObjClass classObj, ObjModule module) {
		throw new NotImplementedException();
	}

	private Value validateSuperclass(Value name, Value superclassValue, int numFields) {
		if (!superclassValue.isClass()) {
			return new Value("Class '" + name + "' cannot inherit from a non-class object.");
		}

		ObjClass superclass = superclassValue.asClass();
		if (superclass == classClass || superclass == fiberClass || superclass == fnClass || superclass == listClass || superclass == mapClass || superclass == rangeClass || superclass == stringClass) {
			return new Value("Class '" + name + "' cannot inherit from built-in class '" + superclass.name + "'.");
		}

		if (superclass.numFields == -1) {
			return new Value("Class '" + name + "' cannot inherit from foreign class '" + superclass.name + "'.");
		}

		if (superclass.numFields + numFields > ClassCompiler.MAX_FIELDS) {
			return new Value("Class '" + name + "' may not have more than 255 fields, including inherited ones.");
		}

		return null;
	}

	private void RUNTIME_ERROR() {
		throw new RuntimeException(fiber.error.toString());
	}

	private WrenInterpretResult loadIntoCore(String source) {
		ObjModule coreModule = getCoreModule();

		ObjFn fn = compile(coreModule, "", source, true);
		if (fn == null) return WrenInterpretResult.RESULT_COMPILE_ERROR;

		ObjFiber fiber = new ObjFiber(fn);
		return runInterpreter(fiber);
	}

	private ObjFiber loadModule(String name, String source) {
		ObjModule module = getModule(new Value(name));

		if (module == null) {
			module = new ObjModule(name);

			modules.set(new Value(name), new Value(module));

			ObjModule coreModule = getCoreModule();
			for (int i = 0; i < coreModule.variables.count(); i++) {
				module.defineVariable(coreModule.variableNames.get(i), coreModule.variables.get(i));
			}
		}

		ObjFn fn = compile(module, name, source, true);
		if (fn == null) {
			return null;
		}

		ObjFiber moduleFiber = new ObjFiber(fn);
		return moduleFiber;
	}

	private void initializeCore() {
		ObjModule coreModule = getCoreModule();

		try {
			// object
			objectClass = defineClass(coreModule, "Object");
			objectClass.bindMethod(methodNames.ensure("!"), WrenObject.class.getMethod("not", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			objectClass.bindMethod(methodNames.ensure("==(_)"), WrenObject.class.getMethod("eqeq", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			objectClass.bindMethod(methodNames.ensure("!=(_)"), WrenObject.class.getMethod("bangeq", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			objectClass.bindMethod(methodNames.ensure("is(_)"), WrenObject.class.getMethod("is", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			objectClass.bindMethod(methodNames.ensure("toString"), WrenObject.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			objectClass.bindMethod(methodNames.ensure("type"), WrenObject.class.getMethod("type", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// class
			classClass = defineClass(coreModule, "Class");
			classClass.bindMethod(methodNames.ensure("name"), WrenClass.class.getMethod("name", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			classClass.bindMethod(methodNames.ensure("supertype"), WrenClass.class.getMethod("supertype", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			classClass.bindMethod(methodNames.ensure("toString"), WrenClass.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			classClass.bindSuperclass(objectClass);

			ObjClass objectMetaclass = defineClass(coreModule, "Object metaclass");

			objectClass.classObj = objectMetaclass;
			objectMetaclass.classObj = classClass;
			classClass.classObj = classClass;

			objectMetaclass.bindSuperclass(classClass);
			objectMetaclass.bindStaticMethod(methodNames.ensure("same(_,_)"), WrenObject.class.getMethod("same", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			String source = Wren.loadFile("src/io/wren/libs/core.wren");
			interpret("", source);

			// bool
			boolClass = findVariable(coreModule, "Bool").asClass();
			boolClass.bindMethod(methodNames.ensure("toString"), WrenBool.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			boolClass.bindMethod(methodNames.ensure("!"), WrenBool.class.getMethod("not", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// fiber
			fiberClass = findVariable(coreModule, "Fiber").asClass();
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("new(_)"), WrenFiber.class.getMethod("new_fiber", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("abort(_)"), WrenFiber.class.getMethod("abort", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("current"), WrenFiber.class.getMethod("current", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("suspend()"), WrenFiber.class.getMethod("suspend", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("yield()"), WrenFiber.class.getMethod("yield", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.classObj.bindStaticMethod(methodNames.ensure("yield(_)"), WrenFiber.class.getMethod("yield1", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("call()"), WrenFiber.class.getMethod("call", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("call(_)"), WrenFiber.class.getMethod("call1", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("error"), WrenFiber.class.getMethod("error", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("isDone"), WrenFiber.class.getMethod("is_done", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("transfer()"), WrenFiber.class.getMethod("transfer", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("transfer(_)"), WrenFiber.class.getMethod("transfer1", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("transferError(_)"), WrenFiber.class.getMethod("transfer_error", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fiberClass.bindMethod(methodNames.ensure("try()"), WrenFiber.class.getMethod("try_call", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// fn
			fnClass = findVariable(coreModule, "Fn").asClass();
			fnClass.classObj.bindStaticMethod(methodNames.ensure("new(_)"), WrenFn.class.getMethod("new_fn", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fnClass.bindMethod(methodNames.ensure("arity"), WrenFn.class.getMethod("arity", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			fnClass.bindMethod(methodNames.ensure("call()"));
			fnClass.bindMethod(methodNames.ensure("call(_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("call(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)"));
			fnClass.bindMethod(methodNames.ensure("toString"), WrenFn.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// null
			nullClass = findVariable(coreModule, "Null").asClass();
			nullClass.bindMethod(methodNames.ensure("!"), WrenNull.class.getMethod("not", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			nullClass.bindMethod(methodNames.ensure("toString"), WrenNull.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// number
			numClass = findVariable(coreModule, "Num").asClass();
			numClass.classObj.bindStaticMethod(methodNames.ensure("fromString(_)"), WrenNum.class.getMethod("fromString", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.classObj.bindStaticMethod(methodNames.ensure("pi"), WrenNum.class.getMethod("pi", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("-(_)"), WrenNum.class.getMethod("sub", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("+(_)"), WrenNum.class.getMethod("add", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("*(_)"), WrenNum.class.getMethod("mul", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("/(_)"), WrenNum.class.getMethod("div", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("<(_)"), WrenNum.class.getMethod("lt", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure(">(_)"), WrenNum.class.getMethod("gt", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("<=(_)"), WrenNum.class.getMethod("le", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure(">=(_)"), WrenNum.class.getMethod("ge", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("&(_)"), WrenNum.class.getMethod("band", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("|(_)"), WrenNum.class.getMethod("bor", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("^(_)"), WrenNum.class.getMethod("xor", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("<<(_)"), WrenNum.class.getMethod("lshift", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure(">>(_)"), WrenNum.class.getMethod("rshift", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("abs"), WrenNum.class.getMethod("abs", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("acos"), WrenNum.class.getMethod("acos", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("asin"), WrenNum.class.getMethod("asin", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("atan"), WrenNum.class.getMethod("atan", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("ceil"), WrenNum.class.getMethod("ceil", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("cos"), WrenNum.class.getMethod("cos", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("floor"), WrenNum.class.getMethod("floor", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("-"), WrenNum.class.getMethod("minus", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("sin"), WrenNum.class.getMethod("sin", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("sqrt"), WrenNum.class.getMethod("sqrt", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("tan"), WrenNum.class.getMethod("tan", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("%(_)"), WrenNum.class.getMethod("mod", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("~"), WrenNum.class.getMethod("tilde", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("..(_)"), WrenNum.class.getMethod("dotdot", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("...(_)"), WrenNum.class.getMethod("dotdotdot", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("atan(_)"), WrenNum.class.getMethod("atan", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("fraction"), WrenNum.class.getMethod("fraction", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("isInfinity"), WrenNum.class.getMethod("is_infinity", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("isInteger"), WrenNum.class.getMethod("is_integer", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("isNan"), WrenNum.class.getMethod("is_nan", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("sign"), WrenNum.class.getMethod("sign", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("toString"), WrenNum.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("truncate"), WrenNum.class.getMethod("truncate", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			numClass.bindMethod(methodNames.ensure("==(_)"), WrenNum.class.getMethod("eqeq", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			numClass.bindMethod(methodNames.ensure("!=(_)"), WrenNum.class.getMethod("bangeq", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// string
			stringClass = findVariable(coreModule, "String").asClass();
			stringClass.classObj.bindStaticMethod(methodNames.ensure("fromCodePoint(_)"), WrenString.class.getMethod("from_code_point", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("+(_)"), WrenString.class.getMethod("plus", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("[_]"), WrenString.class.getMethod("subscript", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("byteAt_(_)"), WrenString.class.getMethod("byte_at", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("byteCount_"), WrenString.class.getMethod("byte_count", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("codePointAt_(_)"), WrenString.class.getMethod("code_point_at", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("contains(_)"), WrenString.class.getMethod("contains", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("endsWith(_)"), WrenString.class.getMethod("ends_with", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("indexOf(_)"), WrenString.class.getMethod("index_of", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("iterate(_)"), WrenString.class.getMethod("iterate", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("iterateByte_(_)"), WrenString.class.getMethod("iterate_byte", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("iteratorValue(_)"), WrenString.class.getMethod("iterator_value", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("startsWith(_)"), WrenString.class.getMethod("starts_with", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			stringClass.bindMethod(methodNames.ensure("toString"), WrenString.class.getMethod("to_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// list
			listClass = findVariable(coreModule, "List").asClass();
			listClass.classObj.bindStaticMethod(methodNames.ensure("new()"), WrenList.class.getMethod("new_list", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("[_]"), WrenList.class.getMethod("subscript", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("[_]=(_)"), WrenList.class.getMethod("subscript_setter", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("add(_)"), WrenList.class.getMethod("add", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("clear()"), WrenList.class.getMethod("clear", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("count"), WrenList.class.getMethod("count", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("insert(_,_)"), WrenList.class.getMethod("insert", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("iterate(_)"), WrenList.class.getMethod("iterate", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("iteratorValue(_)"), WrenList.class.getMethod("iterator_value", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			listClass.bindMethod(methodNames.ensure("removeAt(_)"), WrenList.class.getMethod("remove_at", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// map
			mapClass = findVariable(coreModule, "Map").asClass();
			mapClass.classObj.bindStaticMethod(methodNames.ensure("new()"), WrenMap.class.getMethod("new_map", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("[_]"), WrenMap.class.getMethod("subscript", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("[_]=(_)"), WrenMap.class.getMethod("subscript_setter", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("clear()"), WrenMap.class.getMethod("clear", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("containsKey(_)"), WrenMap.class.getMethod("contains_key", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("count"), WrenMap.class.getMethod("count", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("remove(_)"), WrenMap.class.getMethod("remove", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("iterate_(_)"), WrenMap.class.getMethod("iterate", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("keyIteratorValue_(_)"), WrenMap.class.getMethod("key_iterator_value", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			mapClass.bindMethod(methodNames.ensure("valueIteratorValue_(_)"), WrenMap.class.getMethod("value_iterator_value", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));

			// range

			// system
			ObjClass systemClass = findVariable(coreModule, "System").asClass();
			systemClass.classObj.bindStaticMethod(methodNames.ensure("clock"), WrenSystem.class.getMethod("clock", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
			systemClass.classObj.bindStaticMethod(methodNames.ensure("writeString_(_)"), WrenSystem.class.getMethod("write_string", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		// assign string class to created objects.
		ObjString.finalizeStrings();
	}

	private void initializeMeta() {
		String source = Wren.loadFile("src/io/wren/libs/meta.wren");
		interpret("", source);

		ObjModule coreModule = getCoreModule();

		try {
			ObjClass metaClass = findVariable(coreModule, "Meta").asClass();
			metaClass.classObj.bindStaticMethod(methodNames.ensure("eval(_)"), WrenMeta.class.getMethod("eval", new Class<?>[] {
					Value[].class,
					int.class,
					int.class
			}));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private ObjClass defineClass(ObjModule module, String name) {
		ObjClass classObj = newSingleClass(0, name);
		module.defineVariable(name, new Value(classObj));
		return classObj;
	}

	private ObjClass newSingleClass(int numFields, String name) {
		ObjClass classObj = new ObjClass(name);
		classObj.numFields = numFields;
		return classObj;
	}

	@SuppressWarnings("unused")
	private void bindClass(ObjModule module, String name, String fullName) {
		try {
			Class<?> c = Class.forName(fullName);

			ObjClass objClass = newClass(objectClass, 0, name);

			module.defineVariable(name, new Value(objClass));

			for (java.lang.reflect.Method meth : c.getDeclaredMethods()) {
				int mod = meth.getModifiers();
				if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
					Method m = new Method();
					m.nativeMethod = meth;
					m.type = MethodType.FOREIGN;

					String signature = meth.getName() + "(";
					for (int k = 0; k < meth.getParameterCount(); k++) {
						if (k > 0) signature += ",";
						signature += '_';
					}
					signature += ")";

					int symbol = methodNames.ensure(signature);

					objClass.classObj.bindMethod(symbol, m);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private ObjClass newClass(ObjClass superclass, int numFields, String name) {
		ObjClass metaclass = newSingleClass(0, name + " metaclass");
		metaclass.classObj = classClass;

		metaclass.bindSuperclass(classClass);

		ObjClass classObj = newSingleClass(numFields, name);

		classObj.classObj = metaclass;
		classObj.bindSuperclass(superclass);

		return classObj;
	}

	public static ObjClass getInlineClass(Value value) {
		switch (value.getType()) {
			case FALSE:
			case TRUE:
				return boolClass;
			case NULL:
				return nullClass;
			case NUM:
				return numClass;
			case OBJ:
				return value.asObj().classObj;
			case UNDEFINED:
			default:
				throw new UnreachableCodeException();
		}
	}
}
