package io.wren.libs;

import io.wren.utils.Wren;
import io.wren.value.ObjClass;
import io.wren.value.Value;

public class WrenClass {
	public static boolean name(Value[] stack, int stackStart, int numArgs) {
		ObjClass classObj = stack[stackStart].asClass();
		return Wren.RETURN(stack, stackStart, classObj.name);
	}
	
	public static boolean supertype(Value[] stack, int stackStart, int numArgs) {
		ObjClass classObj = stack[0].asClass();
		if(classObj.superclass == null) return Wren.RETURN(stack, stackStart);
		return Wren.RETURN(stack, stackStart, classObj.superclass);
	}

	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		return WrenClass.name(stack, stackStart, numArgs);
	}
}
