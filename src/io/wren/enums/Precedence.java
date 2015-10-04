package io.wren.enums;

public enum Precedence {
	NONE,
	LOWEST,
	ASSIGNMENT,
	TERNARY,
	LOGICAL_OR,
	LOGICAL_AND,
	EQUALITY,
	IS,
	COMPARISON,
	BITWISE_OR,
	BITWISE_XOR,
	BITWISE_AND,
	BITWISE_SHIFT,
	RANGE,
	TERM,
	FACTOR,
	UNARY,
	CALL,
	PRIMARY;
	
	public Precedence add(int x) {
		int newX = ordinal() + x;
		return values()[newX];
	}
	
	public boolean le(Precedence p) {
		return ordinal() <= p.ordinal();
	}
}
