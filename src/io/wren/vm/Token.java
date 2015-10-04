package io.wren.vm;

import io.wren.enums.TokenType;

public class Token {
	TokenType type;
	String value;
	int line;
	
	public Token() {
		type = TokenType.ERROR;
	}

	public Token(TokenType type, String value, int line) {
		this.type = type;
		this.value = value;
		this.line = line;
	}
	
}
