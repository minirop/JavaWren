package io.wren.libs;

import io.wren.utils.Wren;
import io.wren.value.Obj;
import io.wren.value.ObjClass;
import io.wren.value.Value;
import io.wren.vm.WrenVM;

public class WrenObject {
	public static boolean not(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, false);
	}
	
	public static boolean eqeq(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		return Wren.RETURN(stack, stackStart, left.equals(right));
	}
	
	public static boolean bangeq(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		return Wren.RETURN(stack, stackStart, !left.equals(right));
	}
	
	public static boolean is(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		
		if(!right.isClass()) {
			return Wren.RETURN_ERROR("Right operand must be a class.");
		}
		
		ObjClass classObj = WrenVM.getInlineClass(left);
		ObjClass baseClassObj = right.asClass();
		
		do {
			if(baseClassObj == classObj)
				return Wren.RETURN(stack, stackStart, true);
			
			classObj = classObj.superclass;
		} while(classObj != null);
		
		return Wren.RETURN(stack, stackStart, false);
	}
	
	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		Obj obj = stack[stackStart].asObj();
		String string = obj.classObj.toString();
		return Wren.RETURN(stack, stackStart, string);
	}
	
	public static boolean type(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, WrenVM.getInlineClass(stack[stackStart]));
	}
	
	public static boolean same(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		return Wren.RETURN(stack, stackStart, left.equals(right));
	}
}
