package io.wren.value;

import io.wren.enums.ValueType;

public class Value {
	public static final Value UNDEFINED = new Value(ValueType.UNDEFINED);
	public static final Value NULL = new Value(ValueType.NULL);
	public static final Value TRUE = new Value(ValueType.TRUE);
	public static final Value FALSE = new Value(ValueType.FALSE);
	
	private ValueType type;
	private Obj obj;
	private double num;
	
	public Value() {
		type = ValueType.UNDEFINED;
	}

	public Value(boolean bool) {
		type = (bool ? ValueType.TRUE : ValueType.FALSE);
	}
	
	public Value(double d) {
		type = ValueType.NUM;
		num = d;
	}
	
	public Value(Obj object) {
		type = ValueType.OBJ;
		this.obj = object;
	}
	
	public Value(String string) {
		this(new ObjString(string));
	}
	
	public Value(ValueType type) {
		this.type = type;
	}
	
	public ValueType getType() {
		return type;
	}

	private boolean is(ValueType t) {
		return type == t;
	}
	
	public boolean isTrue() {
		return is(ValueType.TRUE);
	}

	public boolean isFalse() {
		return is(ValueType.FALSE);
	}

	public boolean isNull() {
		return is(ValueType.NULL);
	}

	public boolean isUndefined() {
		return is(ValueType.UNDEFINED);
	}
	
	public boolean isBool() {
		return is(ValueType.FALSE) || is(ValueType.TRUE);
	}

	public boolean isNum() {
		return is(ValueType.NUM);
	}
	
	public boolean isClass() {
		return obj instanceof ObjClass;
	}
	
	public boolean isClosure() {
		return obj instanceof ObjClosure;
	}
	
	public boolean isFiber() {
		return obj instanceof ObjFiber;
	}
	
	public boolean isFn() {
		return obj instanceof ObjFn;
	}
	
	public boolean isForeign() {
		return obj instanceof ObjForeign;
	}
	
	public boolean isInstance() {
		return obj instanceof ObjInstance;
	}
	
	public boolean isRange() {
		return obj instanceof ObjRange;
	}
	
	public boolean isString() {
		return obj instanceof ObjString;
	}
	
	public Obj asObj() {
		return obj;
	}
	
	public ObjClass asClass() {
		return (ObjClass)obj;
	}
	
	public ObjClosure asClosure() {
		return (ObjClosure)obj;
	}
	
	public ObjFiber asFiber() {
		return (ObjFiber)obj;
	}
	
	public ObjFn asFn() {
		return (ObjFn)obj;
	}
	
	public ObjFn asFnOrClosure() {
		if(obj instanceof ObjClosure) {
			return asClosure().fn;
		}
		return asFn();
	}
	
	public ObjForeign asForeign() {
		return (ObjForeign)obj;
	}
	
	public ObjInstance asInstance() {
		return (ObjInstance)obj;
	}
	
	public ObjList asList() {
		return (ObjList)obj;
	}
	
	public ObjMap asMap() {
		return (ObjMap)obj;
	}
	
	public ObjModule asModule() {
		return (ObjModule)obj;
	}
	
	public ObjRange asRange() {
		return (ObjRange)obj;
	}
	
	public ObjString asString() {
		return (ObjString)obj;
	}
	
	public double asDouble() {
		return num;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Value)) return false;
		
		Value val = (Value)obj;
		if(this.type != val.type) return false;
		if(this.type == ValueType.NUM) return this.num == val.num;
		if(this.type != ValueType.OBJ) return true;
		if(this.obj.getClass() != val.obj.getClass()) return false;
		
		return this.obj.equals(val.obj);
	}
	
	@Override
	public String toString() {
		switch(type) {
			case FALSE:
				return "false";
			case NULL:
				return "null";
			case NUM:
				return ""+num;
			case OBJ:
				if(obj == null) return "Obj(null)";
				return obj.toString();
			case TRUE:
				return "true";
			case UNDEFINED:
				return "undefined";
		}
		return "<invalid>";
	}

	public int asInt() {
		return (int)num;
	}
}
