package io.wren.utils;


public class SymbolTable extends Buffer<String> {
	public int add(String name) {
		if(name.equals("null[_]"))
			throw new RuntimeException();
		write(name);
		return elements.size() - 1;
	}
	
	public int ensure(String name) {
		int existing = find(name);
		if(existing != -1) return existing;
		
		return add(name);
	}
	
	public int find(String name) {
		return elements.indexOf(name);
	}
}
