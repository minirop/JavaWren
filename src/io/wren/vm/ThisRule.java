package io.wren.vm;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ThisRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		if(compiler.getEnclosingClass() == null) {
			compiler.error("Cannot use 'this' outside of a method.");
			return;
		}
		
		compiler.loadThis();
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
