package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SuperRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		ClassCompiler enclosingClass = compiler.getEnclosingClass();
		
		if(enclosingClass == null) {
			compiler.error("Cannot use 'super' outside of a method.");
		}
		
		compiler.loadThis();
		
		if(compiler.match(TokenType.DOT)) {
			compiler.consume(TokenType.NAME, "Expect method name after 'super.'.");
			compiler.namedCall(allowAssignment, Code.SUPER_0);
		} else if(enclosingClass != null) {
			compiler.methodCall(Code.SUPER_0, enclosingClass.signature);
		}
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
