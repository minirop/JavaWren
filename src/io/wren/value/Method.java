package io.wren.value;

import io.wren.enums.MethodType;

public class Method {
	public ObjFn getFn() {
		if(fn instanceof ObjClosure) {
			return ((ObjClosure)fn).fn;
		}
		return (ObjFn)fn;
	}
	
	public java.lang.reflect.Method nativeMethod;
	public MethodType type;
	public Obj fn;
}
