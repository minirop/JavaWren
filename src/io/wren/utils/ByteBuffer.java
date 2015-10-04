package io.wren.utils;

import io.wren.enums.Code;

import java.util.Arrays;

public class ByteBuffer {
	private byte[] elements;
	private int count;

	public ByteBuffer() {
		clear();
	}
	
	public void clear() {
		elements = new byte[8];
		count = 0;
	}
	
	public void fill(byte data, int count) {
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
	
	public void write(byte data) {
		fill(data, 1);
	}
	
	public int count() {
		return count;
	}
	
	public byte get(int index) {
		return elements[index];
	}

	public void set(int index, byte b) {
		elements[index] = b;
	}
	
	public void set(int index, Code instruction) {
		set(index, instruction.toByte());
	}
}
