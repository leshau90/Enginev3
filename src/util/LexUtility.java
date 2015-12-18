/**
 *
 */
package util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author ilman
 */
public class LexUtility {

	private static boolean isAllNumber(StringBuilder sb) {
		for (int i = 0; i < sb.length(); i++) {
			if (!Character.isDigit(sb.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static Token getRightToken(StringBuilder sb) {
		if (isAllNumber(sb))
			return Token.OPERAND;
		else
			return Token.TYPE;
	}

	static Queue<Object[]> getLex(String str) {

		int s = 0, e = 0;
		Queue<Object[]> lexTable = new LinkedList<Object[]>();
		StringBuilder temp = new StringBuilder();
		// Boolean idd = false;

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c)) {

				if ((c == '2' || c == '3') && temp.length() == 2
						&& Converter.isItLengthZ(temp.toString().toLowerCase())) {
					temp.append(c);
					if (temp.length() == 1)
						s = i;

				}

				else if (temp.length() != 0 && !isAllNumber(temp)) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				} else {
					temp.append(c);
					if (temp.length() == 1)
						s = i;
				}

			} else if (Character.isLetter(c)) {

				if (temp.length() != 0 && isAllNumber(temp)) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				} else {
					temp.append(c);
					if (temp.length() == 1)
						s = i;
				}
			}

			else if (c == ':' || c == 'x' || c == '+' || c == '-') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c),
						Token.OPERATOR, i });
			}

			else if (c == '=') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c), Token.EQUAL,
						i });
			} else if (Character.isWhitespace(c)) {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;

					temp = new StringBuilder();
				}
			}
			// the part where dot or comma comes to play

			else if (c == ',') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c), Token.COMMA,
						i });
			}

			else if (c == '.') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c), Token.DOT, i });
			}

			// inside parenthesessssssss part

			else if (c == '(') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c),
						Token.KURUNGBUKA, i });
			} else if (c == ')') {
				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;
					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c),
						Token.KURUNGTUTUP, i });
			}

			else {

				if (temp.length() != 0) {
					e = i - 1;
					lexTable.add(new Object[] { temp.toString(),
							getRightToken(temp), s, e });
					s = 0;
					e = 0;

					temp = new StringBuilder();
				}
				lexTable.add(new Object[] { Character.toString(c),
						Token.UNKNOWN, i });
			}
		}

		if (temp.length() != 0) {
			e = str.length() - 1;
			lexTable.add(new Object[] { temp.toString(), getRightToken(temp),
					s, e });
			s = 0;
			e = 0;

			temp = new StringBuilder();
		}

		lexTable = lexPrinter(lexTable);
		return lexTable;
	}

	static Queue<Object[]> lexPrinter(Queue<Object[]> s) {
		Queue<Object[]> m = new LinkedList<Object[]>();
		System.out.println("---LEX--table--LEX--LEX---");
		while (!s.isEmpty()) {
			Object[] obj = (Object[]) s.remove();
			m.add(obj);

			System.out.println(obj[0] + "  :  " + obj[1].toString());

			if (obj[1] == Token.OPERAND)
				System.out.print("---index at: " + obj[2].toString() + "  to  "
						+ obj[3].toString() + "\n");

			else
				System.out.print("---index at: " + obj[2].toString() + "\n");
		}
		return m;
	}
}