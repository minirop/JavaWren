package io.wren.vm;

import io.wren.utils.SymbolTable;

public class ClassCompiler {
	public static final int MAX_FIELDS = 255;
	
	SymbolTable fields;
	boolean isForeign;
	boolean inStatic;
	Signature signature;
}
