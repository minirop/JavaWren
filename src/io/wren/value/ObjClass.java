package io.wren.value;

import io.wren.enums.Code;
import io.wren.enums.MethodType;
import io.wren.utils.Buffer;
import io.wren.utils.Wren;

public class ObjClass extends Obj {
	public String name;
	public int numFields;
	public Buffer<Method> methods;
	public ObjClass superclass;

	public ObjClass(String name) {
		methods = new Buffer<Method>();
		this.name = name;
	}

	public ObjClass(String string, ObjClass superclass) {
		this(string);
		bindSuperclass(superclass);
	}

	public void bindSuperclass(ObjClass superclass) {
		this.superclass = superclass;

		if (numFields != -1) {
			numFields += superclass.numFields;
		} else {
			throw new RuntimeException("A foreign class cannot inherit from a class with fields.");
		}

		for (int i = 0; i < superclass.methods.count(); i++) {
			bindMethod(i, superclass.methods.get(i));
		}
	}

	public void bindMethod(int symbol) {
		bindMethod(symbol, MethodType.CALL, null);
	}
	
	public void bindMethod(int symbol, java.lang.reflect.Method method) {
		bindMethod(symbol, MethodType.PRIMITIVE, method);
	}
	
	public void bindMethod(int symbol, MethodType type, java.lang.reflect.Method method) {
		Method meth = new Method();
		meth.nativeMethod = method;
		meth.type = type;
		bindMethod(symbol, meth);
	}

	public void bindStaticMethod(int symbol, java.lang.reflect.Method method) {
		bindStaticMethod(symbol, MethodType.PRIMITIVE, method);
	}
	
	public void bindStaticMethod(int symbol, MethodType type, java.lang.reflect.Method method) {
		Method meth = new Method();
		meth.nativeMethod = method;
		meth.type = type;
		bindMethod(symbol, meth);
	}
	
	public void bindMethod(int symbol, Method method) {
		if (symbol >= methods.count()) {
			int number = symbol - methods.count() + 1;
			for (int i = 0; i < number; i++) {
				Method m = new Method();
				m.type = MethodType.NONE;
				methods.write(m);
			}
		}

		methods.set(symbol, method);
	}

	public void bindMethodCode(ObjFn fn) {
		int ip = 0;
		for (;;) {
			Code instruction = Code.fromByte(fn.bytecode.get(ip++));
			
			switch (instruction) {
				case LOAD_FIELD:
				case LOAD_FIELD_THIS:
				case STORE_FIELD:
				case STORE_FIELD_THIS: {
					byte c = fn.bytecode.get(ip);
					fn.bytecode.set(ip++, (byte) (c + classObj.superclass.numFields));
					break;
				}
				case SUPER_0:
				case SUPER_1:
				case SUPER_10:
				case SUPER_11:
				case SUPER_12:
				case SUPER_13:
				case SUPER_14:
				case SUPER_15:
				case SUPER_16:
				case SUPER_2:
				case SUPER_3:
				case SUPER_4:
				case SUPER_5:
				case SUPER_6:
				case SUPER_7:
				case SUPER_8:
				case SUPER_9: {
					ip += 2;
					int constant = (fn.bytecode.get(ip) << 8) | fn.bytecode.get(ip + 1);
					fn.constants.set(constant, new Value(classObj.superclass));
					break;
				}
				case CLOSURE: {
					int constant = (fn.bytecode.get(ip) << 8) | fn.bytecode.get(ip + 1);
					classObj.bindMethodCode(fn.constants.get(constant).asFn());

					ip += Wren.getNumArguments(fn.bytecode, fn.constants, ip - 1);
					break;
				}
				case END: {
					return;
				}
				default:
					ip += Wren.getNumArguments(fn.bytecode, fn.constants, ip - 1);
			}
		}
	}
}
