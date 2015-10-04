package io.wren.value;

import io.wren.utils.Buffer;
import io.wren.vm.WrenVM;

public class ObjList extends Obj {
	public Buffer<Value> elements;
	
	public ObjList() {
		elements = new Buffer<>();
		classObj = WrenVM.listClass;
	}

	public int count() {
		return elements.count();
	}

	public Value get(int index) {
		return elements.get(index);
	}

	public void set(int index, Value element) {
		elements.set(index, element);
	}

	public void add(Value element) {
		elements.write(element);
	}

	public void clear() {
		elements.clear();
	}

	public void insert(int index, Value element) {
		elements.insert(index, element);
	}

	public Value remove(int index) {
		return elements.remove(index);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int i = 0;i < elements.count();i++) {
			if(i > 0) builder.append(", ");
			builder.append(elements.get(i));
		}
		builder.append("]");
		return builder.toString();
	}
}
