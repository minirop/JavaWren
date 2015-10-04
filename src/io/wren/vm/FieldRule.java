package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.TokenType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FieldRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		int field = 255;

		ClassCompiler enclosingClass = compiler.getEnclosingClass();

		if (enclosingClass == null) {
			compiler.error("Cannot reference a field outside of a class definition.");
		} else if (enclosingClass.isForeign) {
			compiler.error("Cannot define fields in a foreign class.");
		} else if (enclosingClass.inStatic) {
			compiler.error("Cannot use an instance field in a static method.");
		} else {
			field = enclosingClass.fields.ensure(compiler.parser.previous.value);

			if (field >= ClassCompiler.MAX_FIELDS) {
				compiler.error("A class can only have " + ClassCompiler.MAX_FIELDS + " fields.");
			}
		}

		boolean isLoad = true;

		if (compiler.match(TokenType.EQ)) {
			if (!allowAssignment) compiler.error("Invalid assignment.");

			compiler.expression();
			isLoad = false;
		}

		if (compiler.parent != null && compiler.parent.enclosingClass == enclosingClass) {
			compiler.emitByteArg(isLoad ? Code.LOAD_FIELD_THIS : Code.STORE_FIELD_THIS, field);
		} else {
			compiler.loadThis();
			compiler.emitByteArg(isLoad ? Code.LOAD_FIELD : Code.STORE_FIELD, field);
		}
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		throw new NotImplementedException();
	}

}
