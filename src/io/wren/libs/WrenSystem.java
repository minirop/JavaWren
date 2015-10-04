package io.wren.libs;

import io.wren.utils.Wren;
import io.wren.value.Value;

public class WrenSystem {
	public static boolean clock(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, System.nanoTime() / 1_000_000f);
	}
	
	public static boolean write_string(Value[] stack, int stackStart, int numArgs) {
		Value string = stack[stackStart + 1];
		System.out.print(string);
		return Wren.RETURN(stack, stackStart, string);
	}
}
