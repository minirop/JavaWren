package io.wren.vm;

import io.wren.enums.Code;
import io.wren.enums.SignatureType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NameRule extends GrammarRule {

	@Override
	public void prefix(Compiler compiler, boolean allowAssignment) {
		Token token = compiler.parser.previous;
		String name = token.value;

		Instruction loadInstruction = new Instruction();
		int index = compiler.resolveNonmodule(name, loadInstruction);
		if (index != -1) {
			compiler.variable(allowAssignment, index, loadInstruction.code);
			return;
		}

		boolean isLocalName = name.charAt(0) >= 'a' && name.charAt(0) <= 'z';
		if (isLocalName && compiler.getEnclosingClass() != null) {
			compiler.loadThis();
			compiler.namedCall(allowAssignment, Code.CALL_0);
			return;
		}

		int module = compiler.parser.module.variableNames.find(name);
		if (module == -1) {
			if (isLocalName) {
				compiler.error("Undefined variable '" + name + "'.");
				return;
			}

			module = compiler.parser.module.declareVariable(name);

			if (module == -2) {
				compiler.error("Too many module variables defined.");
			}
		}

		compiler.variable(allowAssignment, module, Code.LOAD_MODULE_VAR);
	}

	@Override
	public void infix(Compiler compiler, boolean allowAssignment) {
		throw new NotImplementedException();
	}

	@Override
	public void method(Compiler compiler, Signature signature) {
		signature.type = SignatureType.GETTER;

		if (compiler.maybeSetter(signature)) return;

		compiler.parameterList(signature);
	}

}
