package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.Precedence;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class QuestionRule extends GrammarRule {

	public QuestionRule() {
		precedence = Precedence.ASSIGNMENT;
	}
	
	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		compiler.ignoreNewlines();
		
		int ifJump = compiler.emitJump(Code.JUMP_IF);
		
		compiler.parsePrecedence(allowAssignment, Precedence.TERNARY);
		
		compiler.consume(TokenType.COLON, "Expect ':' after then branch of conditional operator.");
		
		compiler.ignoreNewlines();
		
		int elseJump = compiler.emitJump(Code.JUMP);
		
		compiler.patchJump(ifJump);
		compiler.parsePrecedence(allowAssignment, Precedence.ASSIGNMENT);
		
		compiler.patchJump(elseJump);
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
