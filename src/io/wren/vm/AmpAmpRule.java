package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class AmpAmpRule extends GrammarRule {

	public AmpAmpRule() {
		this.precedence = Precedence.LOGICAL_AND;
	}
	
	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		compiler.ignoreNewlines();
		
		int jump = compiler.emitJump(Code.AND);
		compiler.parsePrecedence(false, Precedence.LOGICAL_AND);
		compiler.patchJump(jump);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
