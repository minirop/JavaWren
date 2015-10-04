package io.wren.value;

import io.wren.vm.WrenVM;

import java.util.ArrayList;
import java.util.List;

public class ObjMap extends Obj {
	List<MapEntry> entries;
	int entriesCount;
	
	public ObjMap() {
		entries = new ArrayList<>();
		entriesCount = 0;
		classObj = WrenVM.mapClass;
	}

	public Value get(Value key) {
		MapEntry entry = find(key);
		if(entry != null) return entry.value;
		return Value.UNDEFINED;
	}

	private MapEntry find(Value key) {
		for(MapEntry me : entries) {
			if(me.key.equals(key)) {
				return me;
			}
		}
		return null;
	}

	public void set(Value key, Value value) {
		for(int i = 0;i < entries.size();i++) {
			MapEntry me = entries.get(i);
			if(me.key.isUndefined()) {
				me.key = key;
				me.value = value;
				entriesCount++;
				return;
			} else if(me.key.equals(key)) {
				me.value = value;
				return;
			}
		}
		
		entries.add(new MapEntry(key, value));
		entriesCount++;
	}

	public void clear() {
		entries.clear();
	}

	public int count() {
		return entriesCount;
	}

	public Value remove(Value key) {
		MapEntry entry = find(key);
		if(entry == null) return Value.NULL;
		
		Value value = entry.value;
		entry.key = Value.UNDEFINED;
		entry.value = Value.TRUE;
		
		entriesCount--;
		
		return value;
	}

	public int capacity() {
		return entries.size();
	}

	public MapEntry get(int index) {
		return entries.get(index);
	}
}
