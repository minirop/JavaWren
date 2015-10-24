package io.wren.cli;

import io.wren.enums.WrenInterpretResult;
import io.wren.utils.Wren;
import io.wren.vm.WrenVM;

public class Launcher {
	public static void main(String[] args) {
		String filepath;
		if (args.length != 1) {
			System.out.println("You need to give a file to execute.");
			return;
		} else {
			filepath = args[0];
		}

		WrenVM vm = new WrenVM();

		String source = Wren.loadFile(filepath);
		if(source != null) {
			WrenInterpretResult result = vm.interpret(filepath, source);
			System.out.println("result = " + result);
		} else {
			System.out.println("could not load " + filepath);
		}
	}
}
