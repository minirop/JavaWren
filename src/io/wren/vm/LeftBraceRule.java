package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LeftBraceRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		compiler.loadCoreVariable("Map");
		compiler.callMethod(0, "new()");
		
		do {
			compiler.ignoreNewlines();
			
			if(compiler.peek() == TokenType.RIGHT_BRACE) break;
			
			compiler.emit(Code.DUP);
			
			compiler.parsePrecedence(false, Precedence.PRIMARY);
			compiler.consume(TokenType.COLON, "Expect ':' after map key.");
			compiler.ignoreNewlines();
			
			compiler.expression();
			compiler.callMethod(2, "[_]=(_)");
			
			compiler.emit(Code.POP);
		} while(compiler.match(TokenType.COMMA));
		
		compiler.ignoreNewlines();
		compiler.consume(TokenType.RIGHT_BRACE, "Expect '}' after map entries.");
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
