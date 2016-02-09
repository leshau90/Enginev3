package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Lexeme {
	private Token t;
	private int s;
	private int e;
	//protected boolean isSet =  false;
	private Parameter param;
	
	
	
	
	public Parameter getParam() {
		return param;
	}
	public void setParam(Parameter param) {
		this.param = param;
	}
	public Lexeme(){};
	public Token getToken() {
		return t;
	}

	public Lexeme setNewValues(Lexeme j) {
		this.t = j.getToken();
		this.s = j.getStart();
		this.e = j.getEnd();
	
		return this;
	}

	public int getLength() {
		return this.e - this.s;
	}

	public void setToken(Token t) {
		this.t = t;
	}

	public int getStart() {
		return s;
	}

	public void setStart(int s) {
		this.s = s;
	}

	public int getEnd() {
		return e;
	}

	public void setEnd(int e) {
		this.e = e;
	}

	public Lexeme(Token t, int s, int e) {
		this.t = t;
		this.e = e;
		this.s = s;
		

	}

	public Lexeme(Token t, int s) {
		this.t = t;
		this.e = s + 1;
		this.s = s;
		
	}

	public String toString() {
		StringBuilder sb = new StringBuilder().append(this.getToken()).append(" index is: ")
				.append(this.getStart()).append(" to ").append(this.getEnd());
		return sb.toString();
	}
}

class SubOperand {
	private Satuan satuan;
	private int s, e;

	// private String input;
	public SubOperand() {
		
	}

	public SubOperand(Lexeme l) {
		this.s = l.getStart();
		this.e = l.getEnd();		
	}

	public int getSubStart() {
		return s;
	}

	public SubOperand setSubStart(int s) {
		this.s = s;
		return this;
	}

	public int getSubEnd() {
		return e;
	}

	public SubOperand setSubEnd(int e) {
		this.e = e;
		return this;
	}

	public Satuan getSatuan() {
		return satuan;
	}

	public SubOperand setSatuan(Satuan satuan) {
		this.satuan = satuan;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((satuan == null) ? 0 : satuan.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubOperand other = (SubOperand) obj;
		if (satuan != other.satuan)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubOperand :[satuan=" + satuan + ", s=" + s + ", e=" + e + "]";
	}

}

class TypedLexeme extends Lexeme {
	

	private Kategori kategori;
	private Satuan satuan ;
	private boolean isNegative;

	private Set<SubOperand> JMD = null;
	private SubOperand TempJ = null;

	public SubOperand getTempJ() {
		return TempJ;
	}

	public void setTempJ(SubOperand tempJ) {
		TempJ = tempJ;
	}

	public TypedLexeme(Token t, int s, int e) {
		super(t, s, e);
		
		// TODO Auto-generated constructor stub
	}

	public boolean pushTempJMDLexeme() {
		if (JMD == null) {
			JMD = new HashSet<SubOperand>();
		}

		boolean b = JMD.add(TempJ);
		if (b)
			System.out.println("..temp subOperand " + TempJ + "is inserted");
		else
			System.out.println("..temp subOperand " + TempJ + "is NOT inserted");
		return b;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (this.JMD != null) {
			for (SubOperand b : JMD) {
				s.append("\n--").append(b);
			}
			s.append("\n");
		}
		return "Merged index:" + this.getStart() + " to: " + this.getEnd() + s.toString()
				+ " satuan: " + this.getSatuan() + " kategori: " + this.getKategori()
				+ ((!isNegative) ? " POSITIVE" : " NEGATIVE");
	}

	public boolean pushJMDLexeme(SubOperand l) {
		if (JMD == null) {
			JMD = new HashSet<SubOperand>();
		}

		boolean b = JMD.add(l);
		if (b)
			System.out.println("..first subOperand " + l + "is inserted");
		else
			System.out.println("..first subOperand " + l + "is NOT inserted");
		return b;
	}

	public TypedLexeme(Token t, int s) {
		super(t, s);
		
		// TODO Auto-generated constructor stub
	}

	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	public Kategori getKategori() {
		return kategori;
	}

	public void setKategori(Kategori kategori) {
		this.kategori = kategori;
	}

	public Satuan getSatuan() {
		return satuan;
	}

	public void setSatuan(Satuan satuan) {
		this.satuan = satuan;
	}

}
