package io.wren.value;

import io.wren.vm.WrenVM;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class ObjFiber extends Obj {
	private static final int STACK_SIZE = 1024;
	
	public Value[] stack;
	public int stackTop;
	public Stack<CallFrame> frames;
	public ObjFiber caller;
	public short id;
	public boolean callerIsTrying;
	public Queue<ObjUpvalue> openUpvalues;
	public Value error;
	
	public ObjFiber(Obj fn) {
		stack = new Value[STACK_SIZE];
		stackTop = 0;
		frames = new Stack<>();
		openUpvalues = new LinkedList<>();
		
		appendCallFrame(fn, 0);
		
		classObj = WrenVM.fiberClass;
	}
	
	public void appendCallFrame(Obj fn, int stackStart) {
		CallFrame cf = new CallFrame();
		cf.fn = fn;
		cf.ip = 0;
		cf.stackStart = stackStart;
		frames.push(cf);
	}

}
