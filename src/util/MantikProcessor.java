package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class MantikProcessor {
	private static final int iIsi = 0;
	private static final int iToken = 1;
	private static final int iStart = 2;
	private static final int iEnd = 3;

	static PExpr[] changeParent(ArrayList<ReferenceItem> DepthTracker,
			PExpr midExpr, PExpr innerExpr, Object[] feed) {
		// every reference is equal at first

		System.out.println("pointer juggling...to change parent: ..." + Arrays.toString(feed));

		if (feed[1] instanceof Integer)
			return new PExpr[] { midExpr, innerExpr };
		// if (feed[1] instanceof Integer) return immediately new
		// PExpr[]{innerExpr,
		// midExpr};
		TokenM bukaAtauTutup = (TokenM) feed[1];

		if (bukaAtauTutup == TokenM.BUKA_KURUNG) {
			System.out.println("--change parent--");
			TokenM operatorAwal = (TokenM) feed[0];
			// add and save appropriate reference
			PExpr p = new PExpr(false);

			midExpr.addParenthesizedExpr(p, operatorAwal);

			DepthTracker.add(new ReferenceItem().setExp(p).setBefore(
					operatorAwal));

			// save reference before last item

			midExpr = DepthTracker.get(DepthTracker.size() - 2).getExp();
			innerExpr = p;

		}

		if (bukaAtauTutup == TokenM.TUTUP_KURUNG) {
			System.out.println("--change parent-')'-");
			TokenM m2 = (TokenM) feed[2];

			// prepare the feed, the operator sign is saved in DepthTracker
			Object[] newFeed = new Object[] {
					DepthTracker.get(DepthTracker.size() - 1).getBefore(),
					innerExpr, m2 };

			// after closed parenthesis add this parenthesized Expr based
			// on appropriate reference to insert it at right pos and depth of
			// tree hierarchy

			// getting current parent (mid) and add the inner as child within
			// mid PExpr

			System.out.println("%%-SPECIAL--BUILD--Tree--')'--%%");
			if (midExpr != DepthTracker.get(0).getExp())
				MantikProcessor.addAppropriateTerm(
						DepthTracker.get(0).getExp(), midExpr, newFeed, null);
			
			//special case: theres no factor term because of special exclusion above
			else if ((m2 == TokenM.MULTIPLE || m2 == TokenM.DIVIDE)  ){
				System.out.println("special tree convergence..adding and deleting...");
				MantikProcessor.addAppropriateTerm(
						DepthTracker.get(0).getExp(), midExpr, newFeed, null);
				if (midExpr.getExpr().get(0) instanceof PExpr){
					midExpr.getExpr().remove(0);
				}				
			}

			// set current parent as child again, and the new parent is the
			// element before it in reference tracker
			innerExpr = midExpr;
			DepthTracker.remove(DepthTracker.size() - 1);
			midExpr = DepthTracker.get(DepthTracker.size() - 1).getExp();
		}
		System.out.println("--------------------------------------");
		System.out.println(Arrays.toString(DepthTracker.toArray()));
		System.out.println("--------------------------------------");
		return new PExpr[] { midExpr, innerExpr };
	}

	static SimpleTerm createSimpleTerm(int value, int[] p, TokenM t) {
		SimpleTerm x = new SimpleTerm();
		x.setToken(t);
		x.setValue(value);
		if (p != null) {
			x.setOperatorIndex(p[0]);
			x.setOperandStart(p[1]);
			x.setOperandEnd(p[2]);
		}
		return x;
	}

	static PExpr createPExprTerm(PExpr x, TokenM t) {
		x.setToken(t);
		return x;
	}

	static void addAppropriateTerm(PExpr outer, PExpr innerExpr,
			Object[] mlexeme, int[] indexChar) {
		System.out.println("--add term--");
		System.out.println(Arrays.toString(mlexeme));
		System.out.println("current parrent is..." + innerExpr);
		System.out.println("while outer state is..." + outer);
		// jika token di awal adalah ( ataupun diakhir adalah ) maka ganti
		// dengan +, untuk posisi default

		Object[] lexeme = new Object[mlexeme.length];
		lexeme[0] = mlexeme[0];
		lexeme[1] = mlexeme[1];
		lexeme[2] = mlexeme[2];

		lexeme[0] = (lexeme[0] == TokenM.BUKA_KURUNG || lexeme[0] == TokenM.TUTUP_KURUNG) ? lexeme[0] = TokenM.ADD
				: lexeme[0];
		lexeme[2] = (lexeme[2] == TokenM.BUKA_KURUNG || lexeme[2] == TokenM.TUTUP_KURUNG) ? lexeme[2] = TokenM.ADD
				: lexeme[2];

		if (lexeme[0] == TokenM.ADD
				&& (lexeme[2] == TokenM.ADD || lexeme[2] == TokenM.SUBTRACT || lexeme[2] == TokenM.EQUAL)) {
			innerExpr.getExpr().add(
					(lexeme[1] instanceof PExpr) ? createPExprTerm(
							(PExpr) lexeme[1], TokenM.ADDER)
							: createSimpleTerm((Integer) lexeme[1], indexChar,
									TokenM.ADDER));
		} else if (lexeme[0] == TokenM.ADD && lexeme[2] == TokenM.DIVIDE) {
			FactorTerm x = new FactorTerm();
			x.setToken(TokenM.FACTOR);

			// x.setToken(TokenM.POSITIVE);

			DivTerm y = new DivTerm();
			y.setNumerator((lexeme[1] instanceof PExpr) ? createPExprTerm(
					(PExpr) lexeme[1], TokenM.NUMERATOR) : createSimpleTerm(
					(Integer) lexeme[1], indexChar, TokenM.NUMERATOR));
			x.addTerms(y);
			innerExpr.getExpr().add(x);
		} else if (lexeme[0] == TokenM.ADD && lexeme[2] == TokenM.MULTIPLE) {
			FactorTerm x = new FactorTerm();

			// create a factor term and add that factor to innermost expression

			x.setToken(TokenM.ADDER);
			x.addTerm((lexeme[1] instanceof PExpr) ? createPExprTerm(
					(PExpr) lexeme[1], TokenM.FACTOR) : createSimpleTerm(
					(Integer) lexeme[1], indexChar, TokenM.FACTOR));
			innerExpr.getExpr().add(x);
		}
		// subtraction PART
		else if (lexeme[0] == TokenM.SUBTRACT
				&& (lexeme[2] == TokenM.ADD || lexeme[2] == TokenM.SUBTRACT || lexeme[2] == TokenM.EQUAL)) {
			innerExpr.getExpr().add(
					(lexeme[1] instanceof PExpr) ? createPExprTerm(
							(PExpr) lexeme[1], TokenM.SUBTRACTOR)
							: createSimpleTerm((Integer) lexeme[1], indexChar,
									TokenM.SUBTRACTOR));
		} else if (lexeme[0] == TokenM.SUBTRACT && lexeme[2] == TokenM.DIVIDE) {
			FactorTerm x = new FactorTerm();
			x.setToken(TokenM.SUBTRACTOR);
			DivTerm y = new DivTerm();
			y.setNumerator((lexeme[1] instanceof PExpr) ? createPExprTerm(
					(PExpr) lexeme[1], TokenM.NUMERATOR) : createSimpleTerm(
					(Integer) lexeme[1], indexChar, TokenM.NUMERATOR));
			x.addTerms(y);
			innerExpr.getExpr().add(x);
		} else if (lexeme[0] == TokenM.SUBTRACT && lexeme[2] == TokenM.MULTIPLE) {
			FactorTerm x = new FactorTerm();
			x.setToken(TokenM.SUBTRACTOR);
			x.addTerm((lexeme[1] instanceof PExpr) ? createPExprTerm(
					(PExpr) lexeme[1], TokenM.FACTOR) : createSimpleTerm(
					(Integer) lexeme[1], indexChar, TokenM.FACTOR));
			innerExpr.getExpr().add(x);
		}

		// after division or multiplication
		else if (lexeme[0] == TokenM.MULTIPLE) {

			((FactorTerm) innerExpr.getExpr().get(
					innerExpr.getExpr().size() - 1)).setAfterMultiple(
					(lexeme[1] instanceof PExpr) ? createPExprTerm(
							(PExpr) lexeme[1], TokenM.FACTOR)
							: createSimpleTerm((Integer) lexeme[1], indexChar,
									TokenM.FACTOR), (TokenM) lexeme[2]);
		} else if (lexeme[0] == TokenM.DIVIDE) {
			((FactorTerm) innerExpr.getExpr().get(
					innerExpr.getExpr().size() - 1))
					.setAfterDivision((lexeme[1] instanceof PExpr) ? createPExprTerm(
							(PExpr) lexeme[1], TokenM.DENUMERATOR)
							: createSimpleTerm((Integer) lexeme[1], indexChar,
									TokenM.DENUMERATOR));
		}
		// hasil
		else if (lexeme[1] == TokenM.EQUAL) {
			outer.result = createSimpleTerm((Integer) lexeme[2], indexChar,
					TokenM.ADDER);
		}
	}

	static PExpr createExpr(Queue lexTable) {

		PExpr root = new PExpr();
		PExpr mid = root;
		PExpr inner = root;

		ArrayList<ReferenceItem> DepthTracker = new ArrayList<ReferenceItem>();
		// first feed all positive
		Object[] feed = { TokenM.ADD, TokenM.ADD, TokenM.ADD };
		Object[] lexeme;
		// first feed
		int[] pos = { 0, 0, 0 };
		// to save reference to current parrent in parse-tree
		PExpr[] reference = { mid, inner };

		// insert outer expression as root in reference tracker
		DepthTracker.add(new ReferenceItem().setExp(root).setBefore(
				TokenM.ADDER));

		while (!lexTable.isEmpty()) {
			lexeme = (Object[]) lexTable.remove();
			// System.out.println( "actual feed is>>>>> "
			// +Arrays.toString(lexeme));
			if ((lexeme[iToken] == Token.OPERATOR || lexeme[iToken] == Token.EQUAL)
					&& pos[0] != 0)
				pos[0] = (Integer) lexeme[iStart];
			if (lexeme[iToken] == Token.OPERAND) {
				pos[1] = (Integer) lexeme[iStart];
				pos[2] = (Integer) lexeme[iEnd];
			}

			feed = rotator(feed, lexeme);

			// this will change parent reference as necessary and only triggered
			// only
			// if feed contains open or closed parenthesis

			reference = MantikProcessor.changeParent(DepthTracker, mid, inner,
					feed);

			mid = reference[0];
			inner = reference[1];
			// System.out.println("now feed is " + feed[0] + feed[1] + feed[2]);

			// anggota hanya akan di tambahkan jika
			// (operator--operand--operator) atau
			// (anything--samadengan--operand)
			if (checkFeed(feed)) {

				// System.out.println("feed is : " + Arrays.toString(feed));
				// System.out.println("outer and inner Exp is : " + root + "\t"
				// + inner);

				MantikProcessor.addAppropriateTerm(root, inner, feed, pos);

				pos[0] = 0;
				pos[1] = 0;
				pos[2] = 0;
			}
		}
		return root;
	}

	private static Object[] rotator(Object[] m, Object[] l) {
		// first insertion always begin with tokenm.add

		m[0] = m[1];
		m[1] = m[2];

		if (l[iToken] == Token.OPERATOR || l[iToken] == Token.EQUAL
				|| l[iToken] == Token.KURUNGBUKA
				|| l[iToken] == Token.KURUNGTUTUP) {
			// System.out.println(m[2]);
			m[2] = trans(((String) l[iIsi]).toCharArray()[0]);
		}
		// berarti adalah angka dalam bentuk string
		else
			m[2] = Integer.parseInt((String) l[iIsi]);
		// System.out.println("after rotation : ..."+Arrays.toString(m));
		return m;
	}

	private static TokenM trans(char c) {

		switch (c) {
		case '+':
			return TokenM.ADD;
		case '-':
			return TokenM.SUBTRACT;
		case 'x':
			return TokenM.MULTIPLE;
		case ':':
			return TokenM.DIVIDE;
		case '=':
			return TokenM.EQUAL;
		case ')':
			return TokenM.TUTUP_KURUNG;
		case '(':
			return TokenM.BUKA_KURUNG;
		default:
			return TokenM.ANGKA;
		}
	}

	private static boolean checkFeed(Object[] t) {
		System.out.println("checkfeed : ..." + Arrays.toString(t));

		if (t[0] instanceof TokenM && t[2] instanceof TokenM
				&& t[1] instanceof Integer) {
			// System.out.println("it's checked--chechkOpr--Operand-Syntax");
			return true;
		} else if (t[1] == TokenM.EQUAL && t[2] instanceof Integer) {
			// System.out.println("it's checked--chechkOpr--Result Syntax");
			return true;
		} else
			return false;
	}

	public static String printBranch(int i) {
		// System.out.println("i is"+i);
		int k = 0;
		StringBuilder br = new StringBuilder();

		while (k < i) {
			// System.out.println(k+"<<k i>>"+i);
			br.append(" ");
			k++;
		}
		// br.append("|_");
		return br.toString();
	}

	public static String typeCode(Term t) {
		if (t instanceof SimpleTerm)
			return "";
		else if (t instanceof PExpr)
			return "P";
		else if (t instanceof FactorTerm)
			return "F";
		else if (t instanceof DivTerm)
			return "D";
		else
			return "U";
	}

}

class ReferenceItem {
	private PExpr exp;
	private TokenM before;
	private TokenM after;

	public PExpr getExp() {
		return exp;
	}

	public ReferenceItem setExp(PExpr exp) {
		this.exp = exp;
		return this;
	}

	public TokenM getBefore() {
		return before;
	}

	public ReferenceItem setBefore(TokenM before) {
		this.before = before;
		return this;
	}

	public TokenM getAfter() {
		return after;
	}

	public ReferenceItem setAfter(TokenM after) {
		this.after = after;
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("before: ").append(before).append(" ").append(exp)
				.append(" ").append("after: ").append(after);

		return sb.toString();
	}
}