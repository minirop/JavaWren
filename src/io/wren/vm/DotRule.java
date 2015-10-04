package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DotRule extends GrammarRule {

	public DotRule() {
		this.precedence = Precedence.CALL;
	}
	
	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		compiler.ignoreNewlines();
		compiler.consume(TokenType.NAME, "Expect method name after '.'.");
		compiler.namedCall(allowAssignment, Code.CALL_0);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
