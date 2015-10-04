package io.wren.value;

public class Obj {
	public ObjClass classObj;
	
	@Override
	public String toString() {
		if(classObj == null) return "Null ObjClass";
		return "instance of " + classObj.name;
	}
}
