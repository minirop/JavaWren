package io.wren.value;

import io.wren.utils.Buffer;
import io.wren.utils.ByteBuffer;
import io.wren.vm.WrenVM;

public class ObjFn extends Obj {
	public Buffer<Value> constants;
	public ByteBuffer bytecode;
	public ObjModule module;
	public int numUpvalues;
	public int numConstants;
	public int bytecodeLength;
	public int arity;
	
	public ObjFn() {
		classObj = WrenVM.fnClass;
	}
}
