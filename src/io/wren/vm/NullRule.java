package io.wren.vm;

import io.wren.enums.Code;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NullRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		compiler.emit(Code.NULL);
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
