package io.wren.utils;

import io.wren.value.Value;
import io.wren.vm.WrenVM;

public class Validate {

	public static int Index(Value arg, int count, String argName) {
		if(!Num(arg, argName)) return Integer.MIN_VALUE;
		
		return IndexValue(count, arg.asInt(), argName);
	}
	
	public static boolean Num(Value arg, String argName) {
		if(arg.isNum()) return true;
		
		WrenVM.fiber.error = new Value(argName + " must be a number.");
		return false;
	}

	public static int IndexValue(int count, int value, String argName) {
		if(!IntValue(value, argName)) return Integer.MIN_VALUE;
		
		if(value < 0) value = count + value;
		
		if(value >= 0 && value < count) return value;
		
		WrenVM.fiber.error = new Value(argName + " out of bounds.");
		return Integer.MIN_VALUE;
	}

	public static boolean IntValue(double value, String argName) {
		if(value == (int)value) return true;
		
		WrenVM.fiber.error = new Value(argName + " must be an integer.");
		return false;
	}

	public static boolean Int(Value arg, String argName) {
		if(!Num(arg, argName)) return false;
		
		return IntValue(arg.asDouble(), argName);
	}

	public static boolean String(Value arg, String argName) {
		if(arg.isString()) return true;
		
		WrenVM.fiber.error = new Value(argName + " must be a string.");
		return false;
	}

	public static boolean Key(Value arg) {
		if(arg.isBool() || arg.isClass() || arg.isFiber() || arg.isNull() || arg.isNum() || arg.isRange() || arg.isString()) {
			return true;
		}
		
		WrenVM.fiber.error = new Value("Key must be a value type or fiber.");
		return false;
	}

	public static boolean Fn(Value arg, String argName) {
		if(arg.isFn() || arg.isClosure()) return true;
		
		WrenVM.fiber.error = new Value(argName + " must be a function.");
		return false;
	}
}
