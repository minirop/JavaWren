package io.wren.value;

public class ObjClosure extends Obj {
	public ObjFn fn;
	public ObjUpvalue[] upvalues;
	
	public ObjClosure(ObjFn fn) {
		this.fn = fn;
		this.upvalues = new ObjUpvalue[fn.numUpvalues];
	}
}
