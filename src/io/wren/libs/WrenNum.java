package io.wren.libs;

import io.wren.utils.Validate;
import io.wren.utils.Wren;
import io.wren.value.ObjRange;
import io.wren.value.ObjString;
import io.wren.value.Value;
import io.wren.vm.WrenVM;

public class WrenNum {
	public static boolean sub(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		double x = left.asDouble() - right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean add(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		double x = left.asDouble() + right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean mul(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		double x = left.asDouble() * right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean div(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		double x = left.asDouble() / right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean lt(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		boolean x = left.asDouble() < right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean gt(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		boolean x = left.asDouble() > right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean le(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		boolean x = left.asDouble() <= right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean ge(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		boolean x = left.asDouble() >= right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean band(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		int x = left.asInt() & right.asInt();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean bor(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		int x = left.asInt() | right.asInt();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean xor(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		int x = left.asInt() ^ right.asInt();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean lshift(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		int x = left.asInt() << right.asInt();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean rshift(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		int x = left.asInt() >> right.asInt();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean abs(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.abs(num.asDouble()));
	}

	public static boolean acos(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.acos(num.asDouble()));
	}

	public static boolean asin(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.asin(num.asDouble()));
	}

	public static boolean atan(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.atan(num.asDouble()));
	}

	public static boolean ceil(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.ceil(num.asDouble()));
	}

	public static boolean cos(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.cos(num.asDouble()));
	}

	public static boolean floor(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.floor(num.asDouble()));
	}

	public static boolean minus(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, -(num.asDouble()));
	}

	public static boolean sin(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.sin(num.asDouble()));
	}

	public static boolean sqrt(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.sqrt(num.asDouble()));
	}

	public static boolean tan(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, Math.tan(num.asDouble()));
	}

	public static boolean mod(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right operand")) return false;
		double x = left.asDouble() % right.asDouble();
		return Wren.RETURN(stack, stackStart, x);
	}

	public static boolean tilde(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, ~(num.asInt()));
	}

	public static boolean dotdot(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right hand side of range")) return false;
		
		double l = left.asDouble();
		double r = right.asDouble();
		ObjRange range = new ObjRange(l, r, true);
		return Wren.RETURN(stack, stackStart, range);
	}

	public static boolean dotdotdot(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		if(!Validate.Num(right, "Right hand side of range")) return false;
		
		double l = left.asDouble();
		double r = right.asDouble();
		ObjRange range = new ObjRange(l, r, false);
		return Wren.RETURN(stack, stackStart, range);
	}

	public static boolean atan2(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart + 1];
		return Wren.RETURN(stack, stackStart, (Math.atan2(left.asDouble(), right.asDouble())));
	}

	public static boolean fraction(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		double d = num.asDouble();
		double n = d - (int) d;
		return Wren.RETURN(stack, stackStart, n);
	}

	public static boolean is_infinity(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		double d = num.asDouble();
		return Wren.RETURN(stack, stackStart, Double.isInfinite(d));
	}

	public static boolean is_integer(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		double d = num.asDouble();
		if (!Double.isFinite(d)) return Wren.RETURN(stack, stackStart, false);

		return Wren.RETURN(stack, stackStart, ((int) d == d));
	}

	public static boolean is_nan(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		double d = num.asDouble();
		return Wren.RETURN(stack, stackStart, Double.isNaN(d));
	}

	public static boolean sign(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		double n = num.asDouble();
		double ret = 0;
		if (n < 0)
			ret = -1;
		else if (n > 0) ret = 1;
		return Wren.RETURN(stack, stackStart, ret);
	}

	public static boolean to_string(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, "" + num.asDouble());
	}

	public static boolean truncate(Value[] stack, int stackStart, int numArgs) {
		Value num = stack[stackStart];
		return Wren.RETURN(stack, stackStart, num.asInt());
	}

	public static boolean eqeq(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart];
		if(right.isNum()) return Wren.RETURN(stack, stackStart, false);
		return Wren.RETURN(stack, stackStart, left.asDouble() == right.asDouble());
	}

	public static boolean bangeq(Value[] stack, int stackStart, int numArgs) {
		Value left = stack[stackStart];
		Value right = stack[stackStart];
		if(right.isNum()) return Wren.RETURN(stack, stackStart, false);
		return Wren.RETURN(stack, stackStart, left.asDouble() != right.asDouble());
	}

	// METACLASS

	public static boolean pi(Value[] stack, int stackStart, int numArgs) {
		return Wren.RETURN(stack, stackStart, Math.PI);
	}

	public static boolean fromString(Value[] stack, int stackStart, int numArgs) {
		Value arg1 = stack[stackStart + 1];

		if (!Validate.String(arg1, "Argument")) return false;

		ObjString str = arg1.asString();

		try {
			double n = Double.parseDouble(str.value);
			return Wren.RETURN(stack, stackStart, n);
		} catch (NumberFormatException e) {
			WrenVM.fiber.error = new Value("Number literal is too large.");
			return false;
		}
	}
}
