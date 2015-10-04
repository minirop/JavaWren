package io.wren.vm;

import io.wren.enums.Precedence;
import io.wren.enums.SignatureType;
import io.wren.enums.TokenType;

public class OperatorRule extends GrammarRule {

	public OperatorRule(String name) {
		this.precedence = Precedence.TERM;
		this.name = name;
	}

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		GrammarRule rule = compiler.getRule(compiler.parser.previous);
		compiler.ignoreNewlines();
		
		compiler.parsePrecedence(false, rule.precedence.add(1));
		
		compiler.callMethod(0, rule.name);
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		GrammarRule rule = compiler.getRule(compiler.parser.previous);
		
		compiler.ignoreNewlines();
		
		compiler.parsePrecedence(false, rule.precedence.add(1));
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		if(compiler.match(TokenType.LEFT_PAREN)) {
			signature.type = SignatureType.METHOD;
			signature.arity = 1;
			compiler.declareNamedVariable();
			compiler.consume(TokenType.RIGHT_PAREN, "Expect ')' after parameter name.");
		} else {
			signature.type = SignatureType.GETTER;
		}
	}

}
