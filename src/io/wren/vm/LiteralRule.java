package io.wren.vm;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LiteralRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		compiler.emitConstant(compiler.parser.value);
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
