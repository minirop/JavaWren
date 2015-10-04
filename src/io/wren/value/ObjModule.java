package io.wren.value;

import io.wren.utils.Buffer;
import io.wren.utils.SymbolTable;

public class ObjModule extends Obj {
	private static final int MAX_MODULE_VARS = 65536;
	
	public String name;
	public Buffer<Value> variables;
	public SymbolTable variableNames;

	public ObjModule(String name) {
		this.name = name;
		variables = new Buffer<Value>();
		variableNames = new SymbolTable();
	}

	public int defineVariable(String name, Value value) {
		if(variables.count() == MAX_MODULE_VARS) return -2;
		
		int symbol = variableNames.find(name);
		
		if(symbol == -1) {
			symbol = variableNames.add(name);
			variables.write(value);
		} else if(variables.get(symbol).isUndefined()) {
			variables.set(symbol, value);
		} else {
			symbol = -1;
		}
		
		return symbol;
	}

	public int declareVariable(String name) {
		if(variables.count() == MAX_MODULE_VARS) return -2;
		
		variables.write(Value.UNDEFINED);
		return variableNames.add(name);
	}
}
