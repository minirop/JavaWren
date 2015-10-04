package io.wren.libs;

import io.wren.utils.Wren;
import io.wren.value.Value;

public class WrenNull {
	public static boolean not(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, true);
	}
	
	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, "null");
	}
}
