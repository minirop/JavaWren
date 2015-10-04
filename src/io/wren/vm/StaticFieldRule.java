package io.wren.vm;

import io.wren.enums.Code;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class StaticFieldRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		Instruction loadInstruction = new Instruction();
		loadInstruction.code = Code.LOAD_LOCAL;
		int index = 255;
		
		Compiler classCompiler = compiler.getEnclosingClassCompiler();
		if(classCompiler == null) {
			compiler.error("Cannot use a static field outside of a class definition.");
		} else {
			Token token = compiler.parser.previous;
			
			if(compiler.resolveLocal(token.value) == -1) {
				int symbol = classCompiler.declareVariable(null);
				
				classCompiler.emit(Code.NULL);
				classCompiler.defineVariable(symbol);
			}
			
			index = compiler.resolveName(token.value, loadInstruction);
		}
		
		compiler.variable(allowAssignment, index, loadInstruction.code);
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
