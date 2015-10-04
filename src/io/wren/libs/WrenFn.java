package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.ObjFn;
import io.wren.value.Value;

public class WrenFn {
	public static boolean arity(Value[] stack, int stackStart, int numArgs) {
		ObjFn fn = stack[stackStart].asFn();
		return Wren.RETURN(stack, stackStart, fn.arity);
	}

	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, "<fn>");
	}
	
	// METACLASS
	
	public static boolean new_fn(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		
		if(!Validate.Fn(arg, "Argument")) return false;
		
		return Wren.RETURN(stack, stackStart, arg);
	}
}
