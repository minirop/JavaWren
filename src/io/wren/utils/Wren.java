package io.wren.utils;

import io.wren.enums.Code;
import io.wren.value.Obj;
import io.wren.value.ObjFn;
import io.wren.value.ObjString;
import io.wren.value.Value;
import io.wren.vm.WrenVM;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Wren {
	public static int Utf8EncodeNumBytes(int value) {
		if (value < 0) throw new IllegalArgumentException("Cannot encode a negative value.");

		if (value <= 0x7f) return 1;
		if (value <= 0x7ff) return 2;
		if (value <= 0xffff) return 3;
		if (value <= 0x10ffff) return 4;
		return 0;
	}

	public static int Utf8DecodeNumBytes(char value) {
		if ((value & 0xc0) == 0x80) return 0;

		if ((value & 0xf8) == 0xf0) return 4;
		if ((value & 0xf0) == 0xe0) return 3;
		if ((value & 0xe0) == 0xc0) return 2;
		return 1;
	}

	public static byte[] Utf8Encode(int value) {
		byte[] bytes;
		if (value <= 0x7f) {
			bytes = new byte[] {
				(byte) (value & 0x7f)
			};
		} else if (value <= 0x7ff) {
			bytes = new byte[] {
					(byte) (0xc0 | ((value & 0x7c0) >> 6)),
					(byte) (0x80 | (value & 0x3f))
			};
		} else if (value <= 0xffff) {
			bytes = new byte[] {
					(byte) (0xe0 | ((value & 0xf000) >> 12)),
					(byte) (0x80 | ((value & 0xfc0) >> 6)),
					(byte) (0x80 | (value & 0x3f))
			};
		} else if (value <= 0x10ffff) {
			bytes = new byte[] {
					(byte) (0xf0 | ((value & 0x1c0000) >> 18)),
					(byte) (0x80 | ((value & 0x3f000) >> 12)),
					(byte) (0x80 | ((value & 0xfc0) >> 6)),
					(byte) (0x80 | (value & 0x3f))
			};
		} else {
			throw new IllegalArgumentException("Invalid unicode value");
		}

		return bytes;
	}

	public static byte[] Utf8Decode(int value) {
		throw new NotImplementedException();
	}

	public static int getNumArguments(ByteBuffer bytecode, Buffer<Value> constants, int ip) {
		Code instruction = Code.fromByte(bytecode.get(ip));

		switch (instruction) {
			case NULL:
			case FALSE:
			case TRUE:
			case POP:
			case DUP:
			case CLOSE_UPVALUE:
			case RETURN:
			case END:
			case LOAD_LOCAL_0:
			case LOAD_LOCAL_1:
			case LOAD_LOCAL_2:
			case LOAD_LOCAL_3:
			case LOAD_LOCAL_4:
			case LOAD_LOCAL_5:
			case LOAD_LOCAL_6:
			case LOAD_LOCAL_7:
			case LOAD_LOCAL_8:
			case CONSTRUCT:
			case FOREIGN_CONSTRUCT:
			case FOREIGN_CLASS:
				return 0;

			case LOAD_LOCAL:
			case STORE_LOCAL:
			case LOAD_UPVALUE:
			case STORE_UPVALUE:
			case LOAD_FIELD_THIS:
			case STORE_FIELD_THIS:
			case LOAD_FIELD:
			case STORE_FIELD:
			case CLASS:
				return 1;

			case CONSTANT:
			case LOAD_MODULE_VAR:
			case STORE_MODULE_VAR:
			case CALL_0:
			case CALL_1:
			case CALL_2:
			case CALL_3:
			case CALL_4:
			case CALL_5:
			case CALL_6:
			case CALL_7:
			case CALL_8:
			case CALL_9:
			case CALL_10:
			case CALL_11:
			case CALL_12:
			case CALL_13:
			case CALL_14:
			case CALL_15:
			case CALL_16:
			case JUMP:
			case LOOP:
			case JUMP_IF:
			case AND:
			case OR:
			case METHOD_INSTANCE:
			case METHOD_STATIC:
			case LOAD_MODULE:
				return 2;

			case SUPER_0:
			case SUPER_1:
			case SUPER_2:
			case SUPER_3:
			case SUPER_4:
			case SUPER_5:
			case SUPER_6:
			case SUPER_7:
			case SUPER_8:
			case SUPER_9:
			case SUPER_10:
			case SUPER_11:
			case SUPER_12:
			case SUPER_13:
			case SUPER_14:
			case SUPER_15:
			case SUPER_16:
			case IMPORT_VARIABLE:
				return 4;

			case CLOSURE:
				int constant = (bytecode.get(ip + 1) << 8 | bytecode.get(ip + 2));
				ObjFn loadedFn = constants.get(constant).asFn();
				return 2 + (loadedFn.numUpvalues * 2);
		}

		throw new IllegalArgumentException();
	}

	public static String loadFile(String filepath) {
		try {
			return new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean RETURN(Value[] stack, int stackStart, Value value) {
		stack[stackStart] = value;
		return true;
	}

	public static boolean RETURN(Value[] stack, int stackStart, String string) {
		return RETURN(stack, stackStart, new Value(string));
	}

	public static boolean RETURN(Value[] stack, int stackStart) {
		return RETURN(stack, stackStart, Value.NULL);
	}

	public static boolean RETURN(Value[] stack, int stackStart, Obj obj) {
		return RETURN(stack, stackStart, new Value(obj));
	}

	public static boolean RETURN(Value[] stack, int stackStart, int i) {
		return RETURN(stack, stackStart, new Value(i));
	}

	public static boolean RETURN(Value[] stack, int stackStart, boolean b) {
		return RETURN(stack, stackStart, b ? Value.TRUE : Value.FALSE);
	}

	public static boolean RETURN_ERROR(String msg) {
		WrenVM.fiber.error = new Value(msg);
		return false;
	}

	public static boolean RETURN(Value[] stack, int stackStart, double d) {
		return RETURN(stack, stackStart, new Value(d));
	}

	public static Value StringFromCodePoint(int value) {
		byte[] bytes = Utf8Encode(value);
		String decoded;
		try {
			decoded = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return Value.NULL;
		}
		ObjString string = new ObjString(decoded);
		return new Value(string);
	}
}
