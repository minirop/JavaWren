package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.SignatureType;
import io.wren.enums.TokenType;

public class LeftBracketRule extends GrammarRule {
	
	public LeftBracketRule() {
		this.precedence = Precedence.CALL;
	}

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		compiler.loadCoreVariable("List");
		compiler.callMethod(0, "new()");
		
		do {
			compiler.ignoreNewlines();
			
			if(compiler.peek() == TokenType.RIGHT_BRACKET) break;
			
			compiler.emit(Code.DUP);
			
			compiler.expression();
			compiler.callMethod(1, "add(_)");
			
			compiler.emit(Code.POP);
		} while(compiler.match(TokenType.COMMA));
		
		compiler.ignoreNewlines();
		compiler.consume(TokenType.RIGHT_BRACKET, "Expect ']' after list elements.");
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		Signature signature = new Signature();
		signature.type = SignatureType.SUBSCRIPT;
		
		compiler.finishArgumentList(signature);
		compiler.consume(TokenType.RIGHT_BRACKET, "Expect ']' after arguments.");
		
		if(compiler.match(TokenType.EQ)) {
			if(!allowAssignment) compiler.error("Invalid assignment.");
			
			signature.type = SignatureType.SUBSCRIPT_SETTER;
			
			compiler.validateNumParameters(++signature.arity);
			compiler.expression();
		}
		
		compiler.callSignature(Code.CALL_0, signature);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		signature.type = SignatureType.SUBSCRIPT;
		signature.name = "";
		
		compiler.finishParameterList(signature);
		compiler.consume(TokenType.RIGHT_BRACKET, "Expect ']' after parameters.");
		
		compiler.maybeSetter(signature);
	}

}
