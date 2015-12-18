package util;

import java.util.ArrayList;

class FactorTerm extends Term {
	// also handle negativity of =its own term
	private ArrayList<Term> factors = new ArrayList<Term>();

	static void addAll(FactorTerm f, PExpr p, int i) {
		// remove at i
		f.getExpr().remove(i);
		int y = 0;
		for (Term t2 : p.getExpr()) {
			// sisipkan sebanyak yang harus disisipkan
			f.getExpr().add(i + y, t2);
			y++;
		}

	}

	// private TokenM = null;

	ArrayList<Term> getExpr() {
		return factors;
	}

	void addTerm(Term t) {
		this.factors.add(t);
	}

	void setAfterMultiple(Term x, TokenM tokenSetelahnya) {
		if (tokenSetelahnya == TokenM.MULTIPLE || tokenSetelahnya == TokenM.ADD
				|| tokenSetelahnya == TokenM.SUBTRACT
				|| tokenSetelahnya == TokenM.EQUAL) {
			addTerm(x);
		} else if (tokenSetelahnya == TokenM.DIVIDE) {
			if (factors.get(factors.size() - 1) instanceof SimpleTerm) {
				DivTerm y = new DivTerm();
				y.setNumerator(x);
				this.addTerms(y);
			} else if (factors.get(factors.size() - 1) instanceof DivTerm) {
				((DivTerm) factors.get(factors.size() - 1)).addDenominator(x);
			}
		}
	}

	void setAfterDivision(Term x) {
		if (factors.get(factors.size() - 1) instanceof DivTerm) {
			((DivTerm) factors.get(factors.size() - 1)).addDenominator(x);
		}
	}

	void addTerms(Term x) {
		factors.add(x);
	}

	Term getCertainTerm(int i) {
		return factors.get(i);
	}

	int getExprSize() {
		return factors.size();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		if (this.getToken() == TokenM.ADDER)
			sb.append("+");
		else
			sb.append("-");

		for (Term t : factors) {
			if (t instanceof SimpleTerm) {
				sb.append(t.toString());
				sb.append("x");
			} else if (t instanceof DivTerm) {
				sb.append(t.toString());
				sb.append("x");
			}
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	@Override
	public String printTree(int lvl) {
		int lvl2 = lvl + 1;

		StringBuilder sb = new StringBuilder();

		for (Term t : factors) {
			sb.append(MantikProcessor.printBranch(lvl)).append(
					t.printTree(lvl2));
		}
		return sb.toString();
	}

	@Override
	public void setClue(String clue) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FactorTerm
				&& this.getToken() == ((Term) obj).getToken()
				&& this.checkmyMember(((FactorTerm) obj).getExpr()))
			return true;
		else
			return false;
	}

	private boolean checkmyMember(ArrayList<Term> perbandingan) {
		boolean atLeastOne = false;
		ArrayList<Integer> checked = new ArrayList<Integer>();

		if (perbandingan.size() != factors.size())
			return false;

		for (Term t : factors) {
			atLeastOne = true;
			for (int j = 0; j < perbandingan.size(); j++) {
				if (t.equals(perbandingan.get(j)) && !checked.contains(j)) {
					checked.add(j);
					atLeastOne = false;
					break;
				}
			}
			if (atLeastOne)
				return false;
		}
		return true;
	}

	@Override
	public Term addParenthesizedExpr(Term t, TokenM k) {
		factors.add(t);
		return null;
	}

	@Override
	public boolean cekKurungBerlebih() {
		// is it possible reduce the excess ()without changing operator of a
		// term and without deleting opperand
		System.out.println("check for excess () " + "inside " + this);
		PExpr temp = null;
		int i = 0;
		for (; i < factors.size(); i++) {
			if (factors.get(i) instanceof PExpr) {
				if (((PExpr) factors.get(i)).isFactorOnly())
				// break to add factors inside this term
				{
					temp = (PExpr) factors.get(i);
					break;
				}
				factors.get(i).cekKurungBerlebih();
			} else
				factors.get(i).cekKurungBerlebih();
		}
		if (temp != null) {
			addAll(this, temp, i);
			// recursive call on this instance
			this.cekKurungBerlebih();
		}
		return false;
	}

}