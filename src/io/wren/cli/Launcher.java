package io.wren.cli;

import io.wren.enums.WrenInterpretResult;
import io.wren.utils.Wren;
import io.wren.vm.WrenVM;

public class Launcher {
	public static void main(String[] args) {
		String filepath = "test.wren"; //args[0];
		if (args.length != 1) {
//			System.out.println("You need to give a file to execute.");
//			return;
		}

		WrenVM vm = new WrenVM();

		String source = Wren.loadFile(filepath);
		WrenInterpretResult result = vm.interpret("test.wren", source);
		System.out.println("result = " + result);
	}
}
