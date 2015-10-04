package io.wren.vm;

import io.wren.enums.SignatureType;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ConstructRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		compiler.consume(TokenType.NAME, "Expect constructor name after 'construct'.");
		
		Signature tokenSignature = compiler.signatureFromToken(SignatureType.INITIALIZER);
		signature.copy(tokenSignature);
		
		if(compiler.match(TokenType.EQ)) {
			compiler.error("A constructor cannot be a setter.");
		}
		
		if(!compiler.match(TokenType.LEFT_PAREN)) {
			compiler.error("A constructor cannot be a getter.");
			return;
		}
		
		if(compiler.match(TokenType.RIGHT_PAREN)) return;
		
		compiler.finishParameterList(signature);
		compiler.consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
	}

}
