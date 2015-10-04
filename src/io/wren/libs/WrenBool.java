package io.wren.libs;

import io.wren.utils.Wren;
import io.wren.value.Value;

public class WrenBool {
	public static boolean not(Value[] stack, int stackStart, int numArgs) {
		Value v = stack[stackStart] == Value.TRUE ? Value.FALSE : Value.TRUE;
		return Wren.RETURN(stack, stackStart, v);
	}
	
	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		if(stack[stackStart] == Value.TRUE) {
			return Wren.RETURN(stack, stackStart, "true");
		}
		return Wren.RETURN(stack, stackStart, "false");
	}
}
