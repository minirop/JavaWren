package io.wren.value;

import io.wren.vm.WrenVM;

public class ObjRange extends Obj {
	public double from;
	public double to;
	public boolean isInclusive;
	
	public ObjRange(double from, double to, boolean isInclusive) {
		this.from = from;
		this.to = to;
		this.isInclusive = isInclusive;
		
		classObj = WrenVM.rangeClass;
	}
	

	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("TODO");
	}
}
