package io.wren.value;

public class ObjInstance extends Obj {
	public Value[] fields;
	
	public ObjInstance(ObjClass classObj) {
		fields = new Value[classObj.numFields];
		for(int i = 0;i < classObj.numFields;i++) {
			fields[i] = Value.NULL;
		}
		
		this.classObj = classObj;
	}
}
