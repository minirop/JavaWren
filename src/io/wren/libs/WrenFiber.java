package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.ObjFiber;
import io.wren.value.Value;
import io.wren.vm.WrenVM;

public class WrenFiber {
	private static boolean runFiber(ObjFiber fiber, Value[] stack, int stackStart, boolean isCall, boolean hasValue) {
		if(isCall) {
			if(fiber.caller != null) Wren.RETURN_ERROR("Fiber has already been called.");
			
			fiber.caller = WrenVM.fiber;
		}
		
		if(fiber.frames.isEmpty()) {
			WrenVM.fiber.error = new Value("Cannot " + (isCall ? "call" : "transfer to") + " a finished fiber.");
			return false;
		}
		
		if(fiber.error != null) {
			WrenVM.fiber.error = new Value("Cannot " + (isCall ? "call" : "transfer to") + " an aborted fiber.");
			return false;
		}
		
		if(hasValue) WrenVM.fiber.stackTop--;
		
		if(fiber.stackTop > 0) {
			fiber.stack[fiber.stackTop - 1] = hasValue ? stack[stackStart + 1] : Value.NULL;
		}
		
		WrenVM.fiber = fiber;
		
		return false;
	}

	public static boolean call(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		return runFiber(fiber, stack, stackStart, true, false);
	}
	
	public static boolean call1(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		return runFiber(fiber, stack, stackStart, true, true);
	}
	
	public static boolean error(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		return Wren.RETURN(stack, stackStart, fiber.error);
	}
	
	public static boolean is_done(Value[] stack, int stackStart, int numArgs) {
		ObjFiber runFiber = stack[stackStart].asFiber();
		return Wren.RETURN(stack, stackStart, runFiber.frames.isEmpty() || runFiber.error != null);
	}
	
	public static boolean transfer(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		return runFiber(fiber, stack, stackStart, false, false);
	}
	
	public static boolean transfer1(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		return runFiber(fiber, stack, stackStart, false, true);
	}
	
	public static boolean transfer_error(Value[] stack, int stackStart, int numArgs) {
		ObjFiber fiber = stack[stackStart].asFiber();
		runFiber(fiber, stack, stackStart, false, true);
		WrenVM.fiber.error = stack[stackStart + 1];
		return false;
	}
	
	public static boolean try_call(Value[] stack, int stackStart, int numArgs) {
		ObjFiber current = WrenVM.fiber;
		ObjFiber tried = stack[stackStart].asFiber();
		
		if(tried.frames.isEmpty()) Wren.RETURN_ERROR("Cannot try a finished fiber.");
		if(tried.caller != null) Wren.RETURN_ERROR("Fiber has already been called.");
		
		WrenVM.fiber = tried;
		
		WrenVM.fiber.caller = current;
		WrenVM.fiber.callerIsTrying = true;
		
		if(WrenVM.fiber.stackTop > 0) {
			WrenVM.fiber.stack[WrenVM.fiber.stackTop - 1] = Value.NULL;
		}
		
		return false;
	}
	
	// METACLASS
	
	public static boolean new_fiber(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		
		if(!Validate.Fn(arg, "Argument")) return false;
		
		ObjFiber newFiber = new ObjFiber(arg.asObj());
		
		newFiber.stack[0] = Value.NULL;
		newFiber.stackTop++;
		
		return Wren.RETURN(stack, stackStart, newFiber);
	}
	
	public static boolean abort(Value[] stack, int stackStart, int numArgs) {
		Value arg = stack[stackStart + 1];
		WrenVM.fiber.error = arg;
		
		return arg.isNull();
	}
	
	public static boolean current(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, WrenVM.fiber);
	}
	
	public static boolean suspend(Value[] stack, int stackStart, int numArgs) {
		WrenVM.fiber = null;
		return false;
	}
	
	public static boolean yield(Value[] stack, int stackStart, int numArgs) {
		ObjFiber current = WrenVM.fiber;
		WrenVM.fiber = current.caller;
		
		current.caller = null;
		current.callerIsTrying = false;
		
		if(WrenVM.fiber != null) {
			WrenVM.fiber.stack[WrenVM.fiber.stackTop - 1] = Value.NULL;
		}
		
		return false;
	}
	
	public static boolean yield1(Value[] stack, int stackStart, int numArgs) {
		ObjFiber current = WrenVM.fiber;
		WrenVM.fiber = current.caller;
		
		current.caller = null;
		current.callerIsTrying = false;
		
		if(WrenVM.fiber != null) {
			WrenVM.fiber.stack[WrenVM.fiber.stackTop - 1] = stack[stackStart + 1];
			current.stackTop--;
		}
		
		return false;
	}
}
