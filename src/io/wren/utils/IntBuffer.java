package io.wren.utils;

import java.util.Arrays;

public class IntBuffer {
	private int[] elements;
	private int count;

	public IntBuffer() {
		clear();
	}
	
	public void clear() {
		elements = new int[8];
		count = 0;
	}
	
	public void fill(int data, int count) {
		if(elements.length < this.count + count) {
			int capacity = elements.length;
			while(capacity < this.count + count) {
				capacity = capacity * 2;
			}
			
			elements = Arrays.copyOf(elements, capacity);
		}
		
		for (int i = 0; i < count; i++) {
			elements[this.count++] = data;
		}
	}
	
	public void write(int data) {
		fill(data, 1);
	}
	
	public int count() {
		return elements.length;
	}
}
