package io.wren.vm;

import io.wren.enums.SignatureType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PrefixOperatorRule extends GrammarRule {

	public PrefixOperatorRule(String name) {
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
		throw new NotImplementedException();
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		signature.type = SignatureType.GETTER;
	}

}
