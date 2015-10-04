package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.ObjList;
import io.wren.value.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WrenList {
	public static boolean subscript(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		Value arg = stack[stackStart + 1];
		
		if(arg.isNum()) {
			int index = Validate.Index(arg, list.count(), "Subscript");
			if(index == Integer.MIN_VALUE) return false;
			
			Wren.RETURN(stack, stackStart, list.get(index));
		}

		if(!arg.isRange()) {
			return Wren.RETURN_ERROR("Subscript must be a number or a range.");
		}
		
		throw new NotImplementedException();
		
//		ObjList sublist = new ObjList();
//		return Wren.RETURN(stack, stackStart, sublist);
	}

	public static boolean subscript_setter(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		Value arg1 = stack[stackStart + 1];
		Value arg2 = stack[stackStart + 2];
		
		int index = Validate.Index(arg1, list.count(), "Subscript");
		if(index == Integer.MIN_VALUE) return false;
		
		list.set(index, arg2);

		return Wren.RETURN(stack, stackStart, arg2);
	}

	public static boolean add(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		list.add(stack[stackStart + 1]);
		return Wren.RETURN(stack, stackStart, stack[stackStart + 1]);
	}

	public static boolean clear(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		list.clear();
		return Wren.RETURN(stack, stackStart);
	}

	public static boolean count(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		return Wren.RETURN(stack, stackStart, list.count());
	}

	public static boolean insert(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		
		int index = Validate.Index(stack[stackStart + 1], list.count(), "Index");
		if(index == Integer.MIN_VALUE) return false;
		
		Value element = stack[stackStart + 2];
		list.insert(index, element);
		return Wren.RETURN(stack, stackStart, element);
	}

	public static boolean iterate(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		Value arg1 = stack[stackStart + 1];

		if (arg1.isNull()) {
			if (list.count() == 0) return Wren.RETURN(stack, stackStart, false);
			return Wren.RETURN(stack, stackStart, 0);
		}
		
		if(!Validate.Int(arg1, "Iterator")) return false;

		int index = arg1.asInt();
		if(index < 0 || index >= list.count() - 1) return Wren.RETURN(stack, stackStart, false);
		
		return Wren.RETURN(stack, stackStart, index + 1);
	}

	public static boolean iterator_value(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		Value arg1 = stack[stackStart + 1];
		
		int index = Validate.Index(arg1, list.count(), "Iterator");
		if(index == Integer.MIN_VALUE) return false;
		
		return Wren.RETURN(stack, stackStart, list.get(index));
	}

	public static boolean remove_at(Value[] stack, int stackStart, int numArgs) {
		ObjList list = stack[stackStart].asList();
		Value arg1 = stack[stackStart + 1];
		int index = Validate.Index(arg1, list.count(), "Index");
		if(index == Integer.MIN_VALUE) return false;
		
		Value removed = list.remove(index);
		return Wren.RETURN(stack, stackStart, removed);
	}

	// METACLASS
	
	public static boolean new_list(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, new ObjList());
	}
}
