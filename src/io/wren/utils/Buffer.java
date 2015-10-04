package io.wren.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Buffer<T> implements Iterable<T> {
	protected List<T> elements;

	public Buffer() {
		elements = new ArrayList<>();
	}

	public void clear() {
		elements.clear();
	}
	
	public void write(T element) {
		elements.add(element);
	}
	
	public T get(int index) {
		return elements.get(index);
	}
	
	public int count() {
		return elements.size();
	}

	public void set(int index, T element) {
		elements.set(index, element);
	}

	public void insert(int index, T element) {
		elements.add(index, element);
	}

	public T remove(int index) {
		return elements.remove(index);
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}
}
