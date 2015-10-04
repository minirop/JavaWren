package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.SignatureType;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class InfixOperatorRule extends GrammarRule {

	public InfixOperatorRule(Precedence precedence, String name) {
		this.precedence = precedence;
		this.name = name;
	}
	
	@Override
	public void prefix(Compiler c, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		GrammarRule rule = compiler.getRule(compiler.parser.previous);
		
		compiler.ignoreNewlines();
		
		compiler.parsePrecedence(false, rule.precedence.add(1));
		
		Signature signature = new Signature(rule.name, SignatureType.METHOD, 1);
		compiler.callSignature(Code.CALL_0, signature);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		signature.type = SignatureType.METHOD;
		signature.arity = 1;
		
		compiler.consume(TokenType.LEFT_PAREN, "Expect '(' after operator name.");
		compiler.declareNamedVariable();
		compiler.consume(TokenType.RIGHT_PAREN, "Expect ')' after parameter name.");
	}

}
