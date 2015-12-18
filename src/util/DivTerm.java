package util;

import java.util.ArrayList;

class DivTerm extends Term {

	private Term numerator;
	private ArrayList<Term> deNom = new ArrayList<Term>();

	public ArrayList<Term> getdeNom() {
		return deNom;
	}

	void setSimpleNumerator(SimpleTerm x) {
		this.numerator = x;
	}

	void setNumerator(Term x) {
		this.numerator = x;
	}

	int getNumeratorValue() {
		return ((SimpleTerm) this.numerator).getValue();
	}

	void addDenominator(Term t) {
		deNom.add(t);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(this.numerator);
		for (Term t : deNom) {
			if (t instanceof SimpleTerm) {
				sb.append(" : ").append(((SimpleTerm) t).getValue())
						.append(" ");
			}
		}
		return sb.toString();
	}

	@Override
	public String printTree(int lvl) {
		int lvl2 = lvl + 1;

		StringBuilder sb = new StringBuilder();
		sb.append(MantikProcessor.typeCode(this.numerator))
		// .append(MantikProcessor.printBranch(lvl))
				.append(" ").append(this.numerator.printTree(lvl2));
		for (Term t : deNom) {
			sb.append(MantikProcessor.printBranch(lvl)).append(
					t.printTree(lvl2));
		}
		return sb.toString();
	}

	private boolean checkDenumerator(ArrayList<Term> perbandingan) {
		boolean atLeastOne = false;
		ArrayList<Integer> checked = new ArrayList<Integer>();
		if (perbandingan.size() != deNom.size())
			return false;
		for (Term t : deNom) {
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
	public void setClue(String clue) {

	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof DivTerm
				&& this.getToken() == ((Term) obj).getToken()
				&& ((DivTerm) obj).numerator.equals(this.numerator)
				&& this.checkDenumerator(((DivTerm) obj).getdeNom()))
			return true;
		else
			return false;
	}

	@Override
	public Term addParenthesizedExpr(Term t, TokenM k) {

		return null;
	}

	@Override
	public boolean cekKurungBerlebih() {
		System.out.println("no need --its division -- " + this);
		// this.numerator.cekKurungBerlebih();
		// for (Term t : deNom) {
		// t.cekKurungBerlebih();
		// }
		return false;
	}
}
