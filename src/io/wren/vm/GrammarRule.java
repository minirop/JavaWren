package io.wren.vm;

import io.wren.enums.Precedence;

public abstract class GrammarRule {
	public abstract void prefix(Compiler compiler, boolean allowAssignment);

	public abstract void infix(Compiler compiler, boolean allowAssignment);

	public abstract void method(Compiler compiler, Signature signature);

	public Precedence precedence = Precedence.NONE;
	public String name;
}
