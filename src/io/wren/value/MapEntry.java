package io.wren.value;

public class MapEntry {
	public Value key;
	public Value value;
	
	public MapEntry() {
	}
	
	public MapEntry(Value key, Value value) {
		this.key = key;
		this.value = value;
	}
}
