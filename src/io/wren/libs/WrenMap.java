package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.MapEntry;
import io.wren.value.ObjMap;
import io.wren.value.Value;

public class WrenMap {
	public static boolean subscript(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value arg = stack[stackStart + 1];
		
		if(!Validate.Key(arg)) return false;
		
		Value value = map.get(arg);
		return Wren.RETURN(stack, stackStart, value);
	}

	public static boolean subscript_setter(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value key = stack[stackStart + 1];
		Value value = stack[stackStart + 2];

		if(!Validate.Key(key)) return false;
		
		map.set(key, value);
		
		return Wren.RETURN(stack, stackStart, value);
	}

	public static boolean clear(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		map.clear();
		return Wren.RETURN(stack, stackStart);
	}

	public static boolean contains_key(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value key = stack[stackStart + 1];
		
		if(!Validate.Key(key)) return false;
		
		Value value = map.get(key);
		return Wren.RETURN(stack, stackStart, !value.isUndefined());
	}

	public static boolean count(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		return Wren.RETURN(stack, stackStart, map.count());
	}

	public static boolean iterate(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value arg = stack[stackStart + 1];
		
		if(map.count() == 0) return Wren.RETURN(stack, stackStart, false);
		
		int index = 0;
		
		if(!arg.isNull()) {
			if(!Validate.Int(arg, "Iterator")) return false;
			
			index = arg.asInt();
			if(index < 0 || index >= map.capacity()) return Wren.RETURN(stack, stackStart, false);
			
			index++;
		}
		
		for(; index < map.capacity();index++) {
			if(!map.get(index).key.isUndefined()) return Wren.RETURN(stack, stackStart, index);
		}
		
		return Wren.RETURN(stack, stackStart, false);
	}

	public static boolean remove(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value key = stack[stackStart + 1];

		if(!Validate.Key(key)) return false;
		
		return Wren.RETURN(stack, stackStart, map.remove(key));
	}

	public static boolean key_iterator_value(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value arg = stack[stackStart + 1];
		
		int index = Validate.Index(arg, map.capacity(), "Iterator");
		if(index == Integer.MIN_VALUE) return false;
		
		MapEntry me = map.get(index);
		if(me.key.isUndefined()) {
			return Wren.RETURN_ERROR("Invalid map iterator value.");
		}
		
		return Wren.RETURN(stack, stackStart, me.key);
	}

	public static boolean value_iterator_value(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = stack[stackStart].asMap();
		Value arg = stack[stackStart + 1];
		
		int index = Validate.Index(arg, map.capacity(), "Iterator");
		if(index == Integer.MIN_VALUE) return false;
		
		MapEntry me = map.get(index);
		if(me.key.isUndefined()) {
			return Wren.RETURN_ERROR("Invalid map iterator value.");
		}
		
		return Wren.RETURN(stack, stackStart, me.value);
	}

	// METACLASS
	
	public static boolean new_map(Value[] stack, int stackStart, int numArgs) {
		ObjMap map = new ObjMap();
		return Wren.RETURN(stack, stackStart, map);
	}
}
