package io.wren.vm;

import io.wren.enums.Code;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BooleanRule extends GrammarRule {

	private boolean bool;
	
	public BooleanRule(boolean bool) {
		this.bool = bool;
	}
	
	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		// (parser.getPrevious().getType() == TokenType.False ? false : true)
		compiler.emit(bool ? Code.TRUE : Code.FALSE);
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
