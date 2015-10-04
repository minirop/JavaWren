package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.ObjString;
import io.wren.value.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WrenString {
	public static boolean plus(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		
		if(!Validate.String(arg, "Right operand")) return false;
		return Wren.RETURN(stack, stackStart, string.value + arg.asString().value);
	}

	public static boolean subscript(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		
		if(arg.isNum()) {
			int index = Validate.Index(arg, string.length(), "Subscript");
			if(index == Integer.MIN_VALUE) return false;
			
			Wren.RETURN(stack, stackStart, string.value.substring(index, index + 1));
		}

		if(!arg.isRange()) {
			return Wren.RETURN_ERROR("Subscript must be a number or a range.");
		}
		
		throw new NotImplementedException();
	}

	public static boolean byte_at(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		
		int index = Validate.Index(arg, string.value.getBytes().length, "Index");
		if(index == Integer.MIN_VALUE) return false;
		
		return Wren.RETURN(stack, stackStart, string.value.getBytes()[index]);
	}

	public static boolean byte_count(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		return Wren.RETURN(stack, stackStart, string.value.getBytes().length);
	}

	public static boolean code_point_at(Value[] stack, int stackStart, int numArgs) {
//		ObjString string = stack[stackStart].asString();
		return Wren.RETURN(stack, stackStart);
	}

	public static boolean contains(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		if(!Validate.String(arg, "Argument")) return false;
		
		ObjString string = stack[stackStart].asString();
		ObjString search = arg.asString();
		
		return Wren.RETURN(stack, stackStart, string.value.contains(search.value));
	}

	public static boolean ends_with(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		if(!Validate.String(arg, "Argument")) return false;
		
		ObjString string = stack[stackStart].asString();
		ObjString search = arg.asString();
		
		return Wren.RETURN(stack, stackStart, string.value.endsWith(search.value));
	}

	public static boolean index_of(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		if(!Validate.String(arg, "Argument")) return false;
		
		ObjString string = stack[stackStart].asString();
		ObjString search = arg.asString();
		
		return Wren.RETURN(stack, stackStart, string.value.indexOf(search.value));
	}

	public static boolean iterate(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		
		if(arg.isNull()) {
			if(string.length() == 0) return Wren.RETURN(stack, stackStart, false);
			return Wren.RETURN(stack, stackStart, 0);
		}
		
		if(!Validate.Int(arg, "Iterator")) return false;
		
		int index = arg.asInt();
		if(index < 0) return Wren.RETURN(stack, stackStart, false);
		
		index++;
		if(index >= string.length()) return Wren.RETURN(stack, stackStart, false);
		
		return Wren.RETURN(stack, stackStart, index);
	}

	public static boolean iterate_byte(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		int bytesCount = string.value.getBytes().length;
		
		if(arg.isNull()) {
			if(string.length() == 0) return Wren.RETURN(stack, stackStart, false);
			return Wren.RETURN(stack, stackStart, 0);
		}
		
		if(!Validate.Int(arg, "Iterator")) return false;
		
		int index = arg.asInt();
		if(index < 0) return Wren.RETURN(stack, stackStart, false);
		
		index++;
		if(index >= bytesCount) return Wren.RETURN(stack, stackStart, false);
		
		return Wren.RETURN(stack, stackStart, index);
	}

	public static boolean iterator_value(Value[] stack, int stackStart, int numArgs) {
		ObjString string = stack[stackStart].asString();
		Value arg = stack[stackStart + 1];
		int index = Validate.Index(arg, string.length(), "Iterator");
		if(index == Integer.MIN_VALUE) return false;
		
		return Wren.RETURN(stack, stackStart, string.value.charAt(index));
	}

	public static boolean starts_with(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		if(!Validate.String(arg, "Argument")) return false;
		
		ObjString string = stack[stackStart].asString();
		ObjString search = arg.asString();
		
		return Wren.RETURN(stack, stackStart, string.value.startsWith(search.value));
	}

	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, stack[stackStart]);
	}

	// METACLASS
	
	public static boolean from_code_point(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		if(!Validate.Int(arg, "Code point")) return false;
		
		int codePoint = arg.asInt();
		if(codePoint < 0) {
			return Wren.RETURN_ERROR("Code point cannot be negative.");
		} else if(codePoint > 0x10ffff) {
			return Wren.RETURN_ERROR("Code point cannot be greater than 0x10ffff.");
		}
		
		return Wren.RETURN(stack, stackStart, Wren.StringFromCodePoint(codePoint));
	}
}
