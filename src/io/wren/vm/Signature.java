package io.wren.vm;

import io.wren.enums.SignatureType;

public class Signature {
	public Signature() {
		this(null, SignatureType.INITIALIZER, 0);
	}

	public Signature(SignatureType type) {
		this(null, type, 0);
	}

	public Signature(String name, SignatureType type) {
		this(name, type, 0);
	}
	
	public Signature(String name, SignatureType type, int arity) {
		this.name = name;
		this.type = type;
		this.arity = arity;
	}

	public String name;
	public SignatureType type;
	public int arity;
	
	@Override
	public String toString() {
		String name = new String();
		if(this.name != null)
			name = this.name;

		switch (type) {
			case METHOD:
				name += signatureParameterList(arity, '(', ')');
				break;
			case GETTER:
				break;
			case SETTER:
				name += '=';
				name += signatureParameterList(1, '(', ')');
				break;
			case SUBSCRIPT:
				name += signatureParameterList(arity, '[', ']');
				break;
			case SUBSCRIPT_SETTER:
				name += signatureParameterList(arity, '[', ']');
				name += '=';
				name += signatureParameterList(1, '(', ')');
				break;
			case INITIALIZER:
				name = "init " + name;
				name += signatureParameterList(arity, '(', ')');
				break;
		}

		return name;
	}
	
	private String signatureParameterList(int numParams, char left, char right) {
		StringBuilder builder = new StringBuilder();

		builder.append(left);
		for (int i = 0; i < numParams; i++) {
			if (i > 0) builder.append(',');
			builder.append('_');
		}
		builder.append(right);

		return builder.toString();
	}

	public void copy(Signature other) {
		this.arity = other.arity;
		this.name = other.name;
		this.type = other.type;
	}
}
