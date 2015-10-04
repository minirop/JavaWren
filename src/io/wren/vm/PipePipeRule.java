package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PipePipeRule extends GrammarRule {

	public PipePipeRule() {
		precedence = Precedence.LOGICAL_OR;
	}
	
	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		compiler.ignoreNewlines();
		
		int jump = compiler.emitJump(Code.OR);
		compiler.parsePrecedence(false,	Precedence.LOGICAL_OR);
		compiler.patchJump(jump);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
