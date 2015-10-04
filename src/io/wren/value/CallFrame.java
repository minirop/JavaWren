package io.wren.value;


public class CallFrame {
	public int ip;
	public Obj fn;
	public int stackStart;
	
	public ObjFn getFn() {
		if(fn instanceof ObjClosure)
			return ((ObjClosure)fn).fn;
		return (ObjFn)fn;
	}

	public ObjClosure maybeClosure() {
		if(fn instanceof ObjClosure)
			return (ObjClosure)fn;
		return null;
	}
}
