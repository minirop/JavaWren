package io.wren.value;

import io.wren.vm.WrenVM;

import java.util.ArrayList;
import java.util.List;

public class ObjString extends Obj {
	public String value;
	
	private static List<ObjString> strings = new ArrayList<>();
	private static boolean initDone = false;

	public ObjString(String string) {
		this.value = string;
		classObj = WrenVM.stringClass;
		
		if(!initDone) {
			strings.add(this);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ObjString)) return false;

		String s1 = value;
		String s2 = ((ObjString) obj).value;
		return s1.equals(s2);
	}

	@Override
	public String toString() {
		return value;
	}
	
	public static void finalizeStrings() {
		initDone = true;
		for(ObjString s : strings) {
			s.classObj = WrenVM.stringClass;
		}
		strings.clear();
		strings = null;
	}

	public int length() {
		return value.length();
	}
}
