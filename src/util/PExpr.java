package util;

import java.util.ArrayList;

public class PExpr extends Term {
	SimpleTerm result;
	private ArrayList<Term> expr = new ArrayList<Term>();
	private boolean outerExpr = true;

	// private boolean closed = true;

	PExpr(boolean b) {
		this.outerExpr = b;
	}

	public PExpr() {
		// TODO Auto-generated constructor stub
	}

	static boolean compareExpression(PExpr a, PExpr b) {
		// Object[] arrEx = new Object[a.getExprSize()];
		System.out
				.println("in computer mind / RAM...this two will be checked for equality");
		System.out.println(a);
		System.out.println(b);

		boolean eq = true;

		return eq;
	}

	static void hilangkanTandaKurung(PExpr outer, PExpr inner, int i) {
		// sudah dalam betuk x+(......)
		// maka hilangkan term dalam ()
		if (outer != null && inner != null) {
			// remove at i
			outer.getExpr().remove(i);
			int y = 0;
			for (Term t2 : inner.getExpr()) {
				// sisipkan sebanyak yang harus disisipkan
				outer.getExpr().add(i + y, t2);
				y++;
			}
		}
	}

	ArrayList<Term> getExpr() {
		return expr;
	}

	Term getCertainTerm(int i) {
		return this.expr.get(i);
	}

	int getResult() {
		return this.result.getValue();
	}

	int getExprSize() {
		return expr.size();
	}

	Term getTerm(int i) {
		return expr.get(i);
	}

	@Override
	public void setClue(String clue) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// System.out.println("toString() by SimpleExprHolder");
		StringBuilder b = new StringBuilder();
		// System.out.println("size is " + this.expr.size());
		for (Term t : expr) {
			// System.out.println(t);
			// System.out.println(t);
			b.append(t);
		}
		if (this.outerExpr == true)
			b.append("=").append(this.result);
		else
			b.insert(0, "(").append(")").insert(0, this.getStringifiedToken());
		if (b.substring(0, 1).equals("+") && this.outerExpr == true)
			b.deleteCharAt(0);
		return b.toString();
	}

	@Override
	public String printTree(int lvl) {

		StringBuilder sb = new StringBuilder();
		sb.append(MantikProcessor.printBranch(lvl))
				.append(MantikProcessor.typeCode(this)).append(" ")
				.append(this.getToken().name()).append("\n");
		for (Term t : expr) {
			sb.append(MantikProcessor.printBranch(lvl)).append(
					t.printTree(lvl + 1));
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		// if (this.outerExpr == true && obj instanceof PExpr
		// && !(this.result == ((PExpr) obj).result)) {
		// return false;
		// }

		if (obj instanceof PExpr && this.getToken() == ((Term) obj).getToken()
				&& this.checkMyMember(((PExpr) obj).getExpr())) {

			if (this.outerExpr == true) {
				// System.out.println("this.result=.."+this.result);
				// System.out.println("((PExpr)obj).result=.."+((PExpr)obj).result);
				// System.out.println("this is root of expression");
				// System.out.println(this.result.equals(((PExpr) obj).result));

				if (!this.result.equals(((PExpr) obj).result)) {
					return false;
				}
			}
			return true;
		} else
			return false;
	}

	private boolean checkMyMember(ArrayList<Term> perbandingan) {
		boolean noneForThisOne = false;
		ArrayList<Integer> checked = new ArrayList<Integer>();
		if (perbandingan.size() != expr.size()) {
			System.out.println("tidak sama banyak anggota..");
			return false;
		}

		for (Term t : expr) {
			noneForThisOne = true;
			for (int j = 0; j < perbandingan.size(); j++) {
				// System.out.println("check " + t + " terhadap "+
				// perbandingan.get(j));
				if (t.equals(perbandingan.get(j)) && !checked.contains(j)) {
					// System.out.println(" sama antara  " + t + " dan "+
					// perbandingan.get(j));
					checked.add(j);
					noneForThisOne = false;
					break;
				}
			}
			if (noneForThisOne)
				return false;
		}
		return true;
	}

	@Override
	public Term addParenthesizedExpr(Term t, TokenM k) {
		if (expr.isEmpty())
			expr.add((PExpr) t);
		else if (expr.get(expr.size() - 1) instanceof FactorTerm) {
			FactorTerm f = (FactorTerm) expr.get(expr.size() - 1);
			f.addParenthesizedExpr(t, k);
		} else {
			expr.add((PExpr) t);
		}
		return this;
	}

	@Override
	public boolean cekKurungBerlebih() {
		System.out.println("check for excess () " + "inside " + this);
		// boolean b =somethingChanged = false;
		// greater than one element, we'll fire an iteration and recursive
		// function over every term
		PExpr outer = null, inner = null;
		boolean b = false;
		int i = 0;
		Term t = null;
		for (; i < expr.size(); i++) {

			// break to avoid concurrent modification
			t = expr.get(i);
			if (t instanceof PExpr && t.getToken() == TokenM.ADDER) {
				outer = this;
				inner = (PExpr) t;
				b = true;
				System.out.println("break...");
				break;
			}
			t.cekKurungBerlebih();
		}

		if (b) {
			hilangkanTandaKurung(outer, inner, i);
			i = 0;
			this.cekKurungBerlebih();
		}

		return false;
	}

	public boolean isFactorOnly() {
		for (Term t : expr) {
			if (t instanceof SimpleTerm && t.getToken() != TokenM.FACTOR)
				// jika ada SimpleTerm yang bukan factor maka return false
				return false;
		}
		return true;
	}
}
