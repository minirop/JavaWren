package io.wren.vm;

import java.util.HashMap;
import java.util.Map;

import io.wren.enums.TokenType;
import io.wren.value.ObjModule;
import io.wren.value.Value;

public class Parser {
	WrenVM vm;
	ObjModule module;
	String sourcePath;
	String source;

	int tokenStart;
	int currentChar;
	int currentLine;

	Token current;
	Token previous;

	boolean skipNewlines;
	boolean printErrors;
	boolean hasError;

	Value value;

	boolean isName(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	char peekChar() {
		if(currentChar < source.length())
			return source.charAt(currentChar);
		return '\0';
	}

	char peekNextChar() {
		if (peekChar() == '\0') return '\0';
		return source.charAt(currentChar + 1);
	}

	char nextChar() {
		char c = peekChar();
		currentChar++;
		if (c == '\n') currentLine++;
		return c;
	}

	boolean matchChar(char c) {
		if (peekChar() != c) return false;
		nextChar();
		return true;
	}

	void makeToken(TokenType type) {
		int l = (type == TokenType.LINE ? currentLine - 1 : currentLine);
		String word = extractFromSource(tokenStart, currentChar);
		current = new Token(type, word, l);
	}

	private String extractFromSource(int start, int end) {
		return source.substring(start, end);
	}

	void twoCharToken(char c, TokenType two, TokenType one) {
		makeToken(matchChar(c) ? two : one);
	}

	void skipLineComment() {
		while (peekChar() != '\n' && peekChar() != '\0') {
			nextChar();
		}
	}

	void skipBlockComment() {
		int nesting = 1;

		while (nesting > 0) {
			if (peekChar() == '\0') {
				lexError("Unterminated block comment.");
				return;
			}

			if (peekChar() == '/' && peekNextChar() == '*') {
				nextChar();
				nextChar();
				nesting++;
				continue;
			}

			if (peekChar() == '*' && peekNextChar() == '/') {
				nextChar();
				nextChar();
				nesting--;
				continue;
			}

			nextChar();
		}
	}

	int readHexDigit() {
		char c = nextChar();
		if (c >= '0' && c <= '9') return c - '0';
		if (c >= 'a' && c <= 'f') return c - 'a' + 10;
		if (c >= 'A' && c <= 'F') return c - 'A' + 10;

		currentChar--;
		return -1;
	}

	void makeNumber(boolean isHex) {
		String str = getTokenString();
		try {
			double d = isHex ? Long.decode(str).doubleValue() : Double.parseDouble(str);
			this.value = new Value(d);
		} catch (NumberFormatException e) {
			lexError("Number literal was too large.");
			this.value = new Value(0.0);
		}

		makeToken(TokenType.NUMBER);
	}

	void readHexNumber() {
		nextChar();

		while (readHexDigit() != -1)
			continue;

		makeNumber(true);
	}

	void readNumber() {
		while (isDigit(peekChar()))
			nextChar();

		if (peekChar() == '.' && isDigit(peekNextChar())) {
			nextChar();
			while (isDigit(peekChar()))
				nextChar();
		}

		if (matchChar('e') || matchChar('E')) {
			matchChar('-');

			if (!isDigit(peekChar())) {
				lexError("Unterminated scientific notation.");
			}

			while (isDigit(peekChar()))
				nextChar();
		}

		makeNumber(false);
	}

	void nextToken() {
		previous = current;

		if (current.type == TokenType.EOF) return;

		while (peekChar() != '\0') {
			tokenStart = currentChar;
			char c = nextChar();
			switch (c) {
				case '(':
					makeToken(TokenType.LEFT_PAREN);
					return;
				case ')':
					makeToken(TokenType.RIGHT_PAREN);
					return;
				case '[':
					makeToken(TokenType.LEFT_BRACKET);
					return;
				case ']':
					makeToken(TokenType.RIGHT_BRACKET);
					return;
				case '{':
					makeToken(TokenType.LEFT_BRACE);
					return;
				case '}':
					makeToken(TokenType.RIGHT_BRACE);
					return;
				case ':':
					makeToken(TokenType.COLON);
					return;
				case ',':
					makeToken(TokenType.COMMA);
					return;
				case '*':
					makeToken(TokenType.STAR);
					return;
				case '%':
					makeToken(TokenType.PERCENT);
					return;
				case '^':
					makeToken(TokenType.CARET);
					return;
				case '+':
					makeToken(TokenType.PLUS);
					return;
				case '-':
					makeToken(TokenType.MINUS);
					return;
				case '~':
					makeToken(TokenType.TILDE);
					return;
				case '?':
					makeToken(TokenType.QUESTION);
					return;

				case '|':
					twoCharToken('|', TokenType.PIPEPIPE, TokenType.PIPE);
					return;
				case '&':
					twoCharToken('&', TokenType.AMPAMP, TokenType.AMP);
					return;
				case '=':
					twoCharToken('=', TokenType.EQEQ, TokenType.EQ);
					return;
				case '!':
					twoCharToken('=', TokenType.BANGEQ, TokenType.BANG);
					return;

				case '.':
					if (matchChar('.')) {
						twoCharToken('.', TokenType.DOTDOTDOT, TokenType.DOTDOT);
						return;
					}

					makeToken(TokenType.DOT);
					return;

				case '/':
					if (matchChar('/')) {
						skipLineComment();
						break;
					}

					if (matchChar('*')) {
						skipBlockComment();
						break;
					}

					makeToken(TokenType.SLASH);
					return;

				case '<':
					if (matchChar('<')) {
						makeToken(TokenType.LTLT);
					} else {
						twoCharToken('=', TokenType.LTEQ, TokenType.LT);
					}

					return;

				case '>':
					if (matchChar('>')) {
						makeToken(TokenType.GTGT);
					} else {
						twoCharToken('=', TokenType.GTEQ, TokenType.GT);
					}

					return;

				case '\n':
					makeToken(TokenType.LINE);
					return;

				case ' ':
				case '\r':
				case '\t':
					while (peekChar() == ' ' || peekChar() == '\r' || peekChar() == '\t') {
						nextChar();
					}
					break;

				case '"':
					readString();
					return;
				case '_':
					readName(peekChar() == '_' ? TokenType.STATIC_FIELD : TokenType.FIELD);
					return;

				case '#':
					if (peekChar() == '!' && currentLine == 1) {
						skipLineComment();
						break;
					}

					lexError("Invalid character '#'.");
					return;

				case '0':
					if (peekChar() == 'x') {
						readHexNumber();
						return;
					}

					readNumber();
					return;

				default:
					if (isName(c)) {
						readName(TokenType.NAME);
					} else if (isDigit(c)) {
						readNumber();
					} else {
						lexError("Invalid character '" + c + "'.");
					}
					return;

			} // switch
		}

		makeToken(TokenType.EOF);
	}

	private static Map<String, TokenType> keywords = new HashMap<>();
	static {
		keywords.put("break", TokenType.BREAK);
		keywords.put("class", TokenType.CLASS);
		keywords.put("construct", TokenType.CONSTRUCT);
		keywords.put("else", TokenType.ELSE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("for", TokenType.FOR);
		keywords.put("foreign", TokenType.FOREIGN);
		keywords.put("if", TokenType.IF);
		keywords.put("import", TokenType.IMPORT);
		keywords.put("in", TokenType.IN);
		keywords.put("is", TokenType.IS);
		keywords.put("null", TokenType.NULL);
		keywords.put("return", TokenType.RETURN);
		keywords.put("static", TokenType.STATIC);
		keywords.put("super", TokenType.SUPER);
		keywords.put("this", TokenType.THIS);
		keywords.put("true", TokenType.TRUE);
		keywords.put("var", TokenType.VAR);
		keywords.put("while", TokenType.WHILE);
	}
	
	void readName(TokenType type) {
		while (isName(peekChar()) || isDigit(peekChar())) {
			nextChar();
		}

		String s = getTokenString();
		
		for(String kw : keywords.keySet()) {
			if(kw.equals(s)) {
				type = keywords.get(kw);
				break;
			}
		}

		makeToken(type);
	}

	String getTokenString() {
		return source.substring(tokenStart, currentChar);
	}

	int readHexEscape(int digits, String description) {
		int value = 0;
		for (int i = 0; i < digits; i++) {
			if (peekChar() == '"' || peekChar() == '\0') {
				lexError("Incomplete " + description + " escape sequence.");

				// Don't consume it if it isn't expected. Keeps us from reading
				// past the
				// end of an unterminated string.
				currentChar--;
				break;
			}

			int digit = readHexDigit();
			if (digit == -1) {
				lexError("Invalid " + description + " escape sequence.");
				break;
			}

			value = (value * 16) | digit;
		}

		return value;
	}

	void readString() {
		StringBuilder string = new StringBuilder();

		for (;;) {
			char c = nextChar();
			if (c == '"') break;

			if (c == '\0') {
				lexError("Unterminated string.");
				currentChar--;
				break;
			}

			if (c == '\\') {
				switch (nextChar()) {
					case '"':
						string.append('"');
						break;
					case '\\':
						string.append('\\');
						break;
					case '0':
						string.append('\0');
						break;
					case 'a':
						// java don't handle alarm beep.
						break;
					case 'b':
						string.append('\b');
						break;
					case 'f':
						string.append('\f');
						break;
					case 'n':
						string.append('\n');
						break;
					case 'r':
						string.append('\r');
						break;
					case 't':
						string.append('\t');
						break;
					case 'u':
						string.append((char) readHexEscape(4, "Unicode"));
						break;
					case 'v':
						// java don't handle vertical tab.
						break;
					case 'x':
						string.append((char) readHexEscape(2, "Unicode"));
						break;
					default:
						lexError("Invalid escape character '" + source.charAt(currentChar - 1) + "'.");
				}
			} else {
				string.append(c);
			}
		}

		this.value = new Value(string.toString());
		makeToken(TokenType.STRING);
	}

	void lexError(String str) {
		hasError = true;
		if (!printErrors) return;

		System.err.print("[" + sourcePath + " line " + currentLine + "] Error: ");
		System.err.println(str);
	}
}
