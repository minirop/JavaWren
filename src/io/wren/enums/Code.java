package io.wren.enums;

public enum Code {
	CONSTANT,
	NULL,
	FALSE,
	TRUE,
	LOAD_LOCAL_0,
	LOAD_LOCAL_1,
	LOAD_LOCAL_2,
	LOAD_LOCAL_3,
	LOAD_LOCAL_4,
	LOAD_LOCAL_5,
	LOAD_LOCAL_6,
	LOAD_LOCAL_7,
	LOAD_LOCAL_8,
	LOAD_LOCAL,
	STORE_LOCAL,
	LOAD_UPVALUE,
	STORE_UPVALUE,
	LOAD_MODULE_VAR,
	STORE_MODULE_VAR,
	LOAD_FIELD_THIS,
	STORE_FIELD_THIS,
	LOAD_FIELD,
	STORE_FIELD,
	POP,
	DUP,
	CALL_0,
	CALL_1,
	CALL_2,
	CALL_3,
	CALL_4,
	CALL_5,
	CALL_6,
	CALL_7,
	CALL_8,
	CALL_9,
	CALL_10,
	CALL_11,
	CALL_12,
	CALL_13,
	CALL_14,
	CALL_15,
	CALL_16,
	SUPER_0,
	SUPER_1,
	SUPER_2,
	SUPER_3,
	SUPER_4,
	SUPER_5,
	SUPER_6,
	SUPER_7,
	SUPER_8,
	SUPER_9,
	SUPER_10,
	SUPER_11,
	SUPER_12,
	SUPER_13,
	SUPER_14,
	SUPER_15,
	SUPER_16,
	JUMP,
	LOOP,
	JUMP_IF,
	AND,
	OR,
	CLOSE_UPVALUE,
	RETURN,
	CLOSURE,
	CONSTRUCT,
	FOREIGN_CONSTRUCT,
	CLASS,
	FOREIGN_CLASS,
	METHOD_INSTANCE,
	METHOD_STATIC,
	LOAD_MODULE,
	IMPORT_VARIABLE,
	END;

	public byte toByte() {
		return (byte)ordinal();
	}
	
	public static Code fromByte(byte index) {
		return values()[index];
	}

	public Code add(int x) {
		int newX = ordinal() + x;
		return values()[newX];
	}
}
