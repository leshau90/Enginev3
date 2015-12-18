package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SintakProcessor {

	private static final int iIsi = 0;
	private static final int iToken = 1;
	private static final int iStart = 2;
	private static final int iEnd = 3;
	private static final int iIsNegative = 4;
	private static final int iKategori = 5;
	private static final int iType = 6;
	private static final int iJMDMode = 7;

	// private static final int iIsCurrency = 7;
	// private static final int iRepClock = 8;
	// private static final int iDot = 9;
	// private static final int iDotHour = 10;

	// private static final int iDot = 5;
	private static Boolean allowZeroBefore = false;

	private static boolean isItPermissibleOperator(Token m) {
		if (m == Token.OPERATOR || m == Token.KURUNGBUKA
				|| m == Token.KURUNGTUTUP) {
			return true;
		}
		return false;
	}

	static boolean cekZero(String s) {
		if (allowZeroBefore && s.startsWith("0"))
			return false;
		else
			return true;
	}

	static Object[] startAnalysisPT(Queue<Object[]> lexTable) {
		// mantikTable
		Queue<Object[]> m = new LinkedList<Object[]>();
		// start FSM
		SintakStatePT state = SintakStatePT.SYNTAX_START;
		Object[] lexeme = null;

		StringBuilder temp = new StringBuilder();
		// StringBuilder merging = new StringBuilder();

		Object[] merged = new Object[8];
		ErrMes em = new ErrMes();
		int lvl = 0;
		String pesanError = "";
		while (!lexTable.isEmpty() && state != SintakStatePT.SYNTAX_ERROR) {
			lexeme = lexTable.remove();
			// m.add(lexeme);
			if (lexeme[iToken] == Token.KURUNGBUKA) {
				lvl++;
			}
			if (lexeme[iToken] == Token.KURUNGTUTUP) {
				lvl--;
			}
			pesanError = state.getErr();
			
			if (nullIt(state)){
				merged = new Object[8];	
				merged [7] = new String[3]; 
			}
			
			
			state = state.tokenSelanjutnya(m, merged, temp, lexeme, lvl, em);
		}

		if (state != SintakStatePT.SYNTAX_FINAL_UNTYPED_OPERAND
				|| state != SintakStatePT.SYNTAX_FINAL_UNTYPED_OPERAND)
			return new Object[] {
					false,
					SintakStatePT.SYNTAX_ERROR.getErr() + statusKurung(lvl)
							+ " " + state.getErr(), lexeme };
		else {
			System.out.println("sintak benar mulai semantik");
			return new Object[] { true, m };
		}
	}
	
	private static boolean nullIt(SintakStatePT a){
		switch (a) {
		case SYNTAX_EQUAL:
		case SYNTAX_OPERATOR:
		case SYNTAX_OPEN_PARENTHESIS:
		case SYNTAX_CLOSE_PARENTHESIS:
		
			return true;
		default :
		
			
			
			return false;
		}
		
		
		
	}

	// static parts
	private static String statusKurung(int level) {
		if (level < 0)
			return "jawabanmu kelebihan kurung tutup";
		if (level > 0)
			return "jawabanmu kelebihan kurung buka";
		return "";
	}

	private static void nullifyx(Object[] j) {
		for (int i = 0; i < j.length; i++) {
			j[i] = null;
		}
		j[j.length - 1] = new Boolean[3];
	}

	private static void simpleErrSet(ErrMes err, Object[] lexeme) {
		err.setStart((int) lexeme[iStart]);
		err.setEnd((int) lexeme[iEnd]);
	}

	private enum SintakStatePT {
		// this FSM will also merge operand as necessary
		SYNTAX_START("kamu harus memulai jawabanmu dengan angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				switch ((Token) lexeme[iToken]) {

				case KURUNGBUKA:
					m.add(lexeme);
					return SYNTAX_OPEN_PARENTHESIS;
				case OPERATOR:
					if (((String) lexeme[iIsi]).equals("-")) {
						// don't add it yet to queue, as it will be added after
						// merged as an operand
						return SYNTAX_NEGATIVE;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

						return SYNTAX_ERROR;
					}
				case OPERAND:
					// add to temp instead
					merged[iStart] = lexeme[iStart];
					merged[iKategori] = Kategori.untyped;
					merged[iToken] = Token.OPERAND;

					sb.append((String) lexeme[iIsi]);
					simpleErrSet(err, lexeme);
					return SYNTAX_UNTYPED_OPERAND;
				case TYPE:
					// hanya rp saja yang boleh
					if (satuan.valueOf(((String) lexeme[iIsi]).toLowerCase()) == satuan.rp) {
						// don't add it yet to queue
						return SYNTAX_RP;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("huruf ini tidak boleh ditulis diawal");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},
		// rp types.. routes

		SYNTAX_RP("setelah rupiah harus angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);

				merged[iStart] = lexeme[iStart];
				merged[iKategori] = Kategori.uang;
				merged[iToken] = Token.OPERAND;

				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					sb.setLength(0);

					if (cekZero((String) lexeme[iIsi])) {
						sb.append(lexeme[iIsi]);
						return SYNTAX_RP_OPERAND;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_RP_OPERAND(
				" seteleh angka dalam rupiah hanya boleh tanda matematika ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:

					merged[iIsi] = sb.toString();
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case DOT:
					return SYNTAX_RP_DOT;
				case COMMA:
					return SYNTAX_RP_COMMA;
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_RP_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {

				// merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// BACKTRACK to check the operand string length by looking
					// at variable temp
					if (temp.length() < 3 && temp.length() > 0) {
						// it is true, change it with new one
						if (merged[iIsi] == null)
							merged[iIsi] = temp.toString();
						temp.setLength(0);

						if (((String) lexeme[iIsi]).length() != 3) {
							simpleErrSet(err, lexeme);
							err.setMess("setelah titik hanya boleh tiga angka");
							return SYNTAX_ERROR;
						}

						temp.append(lexeme[iIsi]);
						merged[iIsi] += temp.toString();

						return SYNTAX_RP_OPERAND_AFTER_DOT;

					} else {
						// simpleErrSet(err, lexeme);
						err.setEnd(err.getEnd() - 1);
						err.setMess("hanya tiga angka sebelum tanda titik \".\" ");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_RP_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
//						String r = (level <= 0) ? "kurang  tanda kurung tutup"
//								: "kelebihan tanda kurung tutup";
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case COMMA:
					return SYNTAX_RP_COMMA;
				case DOT:
					return SYNTAX_RP_DOT;
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_RP_COMMA("setelah koma harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// this trigger if not dotted
					if (merged[iIsi] == null)
						merged[iIsi] = temp.toString();
					temp.setLength(0);
					temp.append(".").append(lexeme[iIsi]);
					merged[iIsi] += temp.toString();

					return SYNTAX_RP_OPERAND_AFTER_COMMA;

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_RP_OPERAND_AFTER_COMMA(
				"setelah angka ini haruslah tanda matematika / operator ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				// so we just directly add it to queue
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_NEGATIVE("setelah tanda negatif harus ada angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				merged[iStart] = lexeme[iStart];
				merged[iKategori] = Kategori.negative;
				merged[iToken] = Token.OPERAND;

				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					sb.setLength(0);

					if (cekZero((String) lexeme[iIsi])) {
						sb.append(lexeme[iIsi]);
						return SYNTAX_UNTYPED_OPERAND;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_OPEN_PARENTHESIS("Setelah tanda buka kurung harus ada angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				switch ((Token) lexeme[iToken]) {

				case KURUNGBUKA:
					m.add(lexeme);
					return SYNTAX_OPEN_PARENTHESIS;
				case OPERATOR:
					// boleh tanda negatif juga
					if (((String) lexeme[iIsi]).equals("-")) {
						// don't add it yet to queue, as it will be added after
						// merged as an operand
						return SYNTAX_NEGATIVE;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

						return SYNTAX_ERROR;
					}
				case OPERAND:
					// add to temp instead

					merged[iKategori] = Kategori.untyped;
					merged[iToken] = Token.OPERAND;
					sb.append((String) lexeme[iIsi]);
					simpleErrSet(err, lexeme);
					return SYNTAX_UNTYPED_OPERAND;
				case TYPE:
					// hanya rp saja yang boleh

					if (satuan.valueOf(((String) lexeme[iIsi]).toLowerCase()) == satuan.rp) {
						// don't add it yet to queue

						return SYNTAX_RP;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("huruf ini tidak boleh ditulis diawal");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_CLOSE_PARENTHESIS(
				"Setelah tanda Kurung tutup harusnya tanda + - x : = ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(lexeme);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(lexeme);
					return SYNTAX_OPERATOR;

				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_UNTYPED_OPERAND(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				// merged[iIsi] = sb.toString();
				sb.append(lexeme[iIsi]);
				switch ((Token) lexeme[iToken]) {
				case KURUNGTUTUP:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_OPERATOR;

				case EQUAL:
					merged[iIsi] = sb.toString();
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case DOT:
					return SYNTAX_RP_DOT;
				case COMMA:
					return SYNTAX_RP_COMMA;
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_AFTERTYPE(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {
				// kategori has been set before at syntax operand
				merged[iEnd] = lexeme[iEnd];
				if (merged[iIsi] == null)
					merged[iIsi] = sb.toString();
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:

					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:

					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_TYPEJMD(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				if (merged[iIsi] == null)
					merged[iIsi] = sb.toString();
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:

					m.add(merged);
					return SYNTAX_OPERATOR;
					
				case OPERAND:
					return SYNTAX_UNTYPED_OPERAND;
				case EQUAL:
					
					merged[iIsi] = sb.toString();
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},
		
		SYNTAX_JMD_OPERAND(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				// merged[iIsi] = sb.toString();
				sb.append(lexeme[iIsi]);
				switch ((Token) lexeme[iToken]) {
				case KURUNGTUTUP:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:

					merged[iIsi] = sb.toString();
					m.add(merged);
					return SYNTAX_OPERATOR;

				case EQUAL:
					merged[iIsi] = sb.toString();
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case DOT:
					return SYNTAX_JMD_DOT;
				case COMMA:
					return SYNTAX_JMD_COMMA;
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},
		
		SYNTAX_JMD_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// BACKTRACK to check the operand string length by looking
					// at variable temp
					if (temp.length() < 3 && temp.length() > 0) {
						// it is true, change it with new one
						if (merged[iIsi] == null)
							merged[iIsi] = temp.toString();
						temp.setLength(0);

						if (((String) lexeme[iIsi]).length() != 3) {
							simpleErrSet(err, lexeme);
							err.setMess("setelah titik hanya boleh tiga angka");
							return SYNTAX_ERROR;
						}
						// append directly, so next state wont have to add it
						// again
						temp.append(lexeme[iIsi]);
						merged[iIsi] += temp.toString();

						return SYNTAX_JMD_OPERAND_AFTER_DOT;

					} else {
						err.setEnd(err.getEnd() - 1);
						err.setMess("hanya tiga angka sebelum tanda titik \".\" ");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_JMD_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case COMMA:
					return SYNTAX_UNTYPED_COMMA;
				case DOT:
					return SYNTAX_UNTYPED_DOT;
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_JMD_COMMA("setelah koma harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					//
					if (merged[iIsi] == null)
						merged[iIsi] = temp.toString();
					temp.setLength(0);
					temp.append(".").append(lexeme[iIsi]);
					merged[iIsi] += temp.toString();

					return SYNTAX_JMD_OPERAND_AFTER_COMMA;

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_JMD_OPERAND_AFTER_COMMA(
				"setelah angka ini haruslah tanda matematika / operator ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				// so we just directly add it to queue
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					 if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},



		SYNTAX_FINAL_UNTYPED_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// BACKTRACK to check the operand string length by looking
					// at variable temp
					if (temp.length() < 3 && temp.length() > 0) {
						// it is true, change it with new one
						if (merged[iIsi] == null)
							merged[iIsi] = temp.toString();
						temp.setLength(0);

						if (((String) lexeme[iIsi]).length() != 3) {
							simpleErrSet(err, lexeme);
							err.setMess("setelah titik hanya boleh tiga angka");
							return SYNTAX_ERROR;
						}
						// append directly, so next state wont have to add it
						// again
						temp.append(lexeme[iIsi]);
						merged[iIsi] += temp.toString();

						return SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_DOT;

					} else {
						simpleErrSet(err, lexeme);
						err.setMess("hanya tiga angka sebelum tanda titik \".\" ");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_FINAL_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case COMMA:
					return SYNTAX_FINAL_UNTYPED_COMMA;
				case DOT:
					return SYNTAX_FINAL_UNTYPED_DOT;
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_UNTYPED_COMMA("setelah koma harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					//
					if (merged[iIsi] == null)
						merged[iIsi] = temp.toString();
					temp.setLength(0);
					temp.append(".").append(lexeme[iIsi]);
					merged[iIsi] += temp.toString();

					return SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_COMMA;

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_COMMA(
				"setelah angka ini haruslah tanda matematika / operator ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				// so we just directly add it to queue
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_FINAL_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_OPERATOR(
				" Setelah tanda matematika + - x : = ( ) harus diikuti dengan angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				switch ((Token) lexeme[iToken]) {

				case KURUNGBUKA:
					m.add(lexeme);
					return SYNTAX_OPEN_PARENTHESIS;
				case OPERATOR:
					if (((String) lexeme[iIsi]).equals("-")) {
						// don't add it yet to queue, as it will be added after
						// merged as an operand
						return SYNTAX_NEGATIVE;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

						return SYNTAX_ERROR;
					}

				case OPERAND:
					// add to temp instead
					merged[iStart] = lexeme[iStart];
					merged[iKategori] = Kategori.untyped;
					merged[iToken] = Token.OPERAND;

					sb.append((String) lexeme[iIsi]);
					simpleErrSet(err, lexeme);
					return SYNTAX_UNTYPED_OPERAND;
				case TYPE:
					// hanya rp saja yang boleh
					if (satuan.valueOf(((String) lexeme[iIsi]).toLowerCase()) == satuan.rp) {
						// don't add it yet to queue
						return SYNTAX_RP;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("huruf ini tidak boleh ditulis diawal");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},
		SYNTAX_EQUAL("hasil jawabanmu harus menggunakan angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				switch ((Token) lexeme[iToken]) {

				case OPERATOR:
					if (((String) lexeme[iIsi]).equals("-")) {
						// don't add it yet to queue, as it will be added after
						// merged as an operand
						return SYNTAX_FINAL_NEGATIVE;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("hanya tanda negatif diizinkan di awal");
						return SYNTAX_ERROR;
					}

				case OPERAND:
					// add to temp instead
					merged[iStart] = lexeme[iStart];
					merged[iKategori] = Kategori.untyped;
					merged[iToken] = Token.OPERAND;

					sb.append((String) lexeme[iIsi]);
					simpleErrSet(err, lexeme);
					return SYNTAX_FINAL_UNTYPED_OPERAND;
				case TYPE:
					// hanya rp saja yang boleh
					if (satuan.valueOf(((String) lexeme[iIsi]).toLowerCase()) == satuan.rp) {
						// don't add it yet to queue
						return SYNTAX_RP;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("huruf ini tidak boleh ditulis diawal");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP("setelah rupiah harus angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);

				merged[iStart] = lexeme[iStart];
				merged[iKategori] = Kategori.uang;
				merged[iToken] = Token.OPERAND;

				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					sb.setLength(0);

					if (cekZero((String) lexeme[iIsi])) {
						sb.append(lexeme[iIsi]);
						return SYNTAX_FINAL_RP_OPERAND;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP_OPERAND(
				" seteleh angka dalam rupiah hanya boleh tanda matematika ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case DOT:
					return SYNTAX_RP_DOT;
				case COMMA:
					return SYNTAX_RP_COMMA;
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				// merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// BACKTRACK to check the operand string length by looking
					// at variable temp
					if (temp.length() < 3 && temp.length() > 0) {
						// it is true, change it with new one
						if (merged[iIsi] == null)
							merged[iIsi] = temp.toString();
						temp.setLength(0);

						if (((String) lexeme[iIsi]).length() != 3) {
							simpleErrSet(err, lexeme);
							err.setMess("setelah titik hanya boleh tiga angka");
							return SYNTAX_ERROR;
						}

						temp.append(lexeme[iIsi]);
						merged[iIsi] += temp.toString();

						return SYNTAX_FINAL_RP_OPERAND_AFTER_DOT;

					} else {
						// simpleErrSet(err, lexeme);
						err.setMess("hanya tiga angka sebelum tanda titik \".\" ");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {
				case COMMA:
					return SYNTAX_FINAL_RP_COMMA;
				case DOT:
					return SYNTAX_FINAL_RP_DOT;
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP_COMMA("setelah koma harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// this trigger if not dotted
					if (merged[iIsi] == null)
						merged[iIsi] = temp.toString();
					temp.setLength(0);
					temp.append(".").append(lexeme[iIsi]);
					merged[iIsi] += temp.toString();

					return SYNTAX_FINAL_RP_OPERAND_AFTER_COMMA;

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_RP_OPERAND_AFTER_COMMA(
				"setelah angka ini haruslah tanda matematika / operator ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				// so we just directly add it to queue
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_NEGATIVE("setelah tanda negatif harus ada angka") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				//nullify(merged);
				merged[iStart] = lexeme[iStart];
				merged[iKategori] = Kategori.negative;
				merged[iToken] = Token.OPERAND;

				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					sb.setLength(0);

					if (cekZero((String) lexeme[iIsi])) {
						sb.append(lexeme[iIsi]);
						return SYNTAX_FINAL_UNTYPED_OPERAND;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
						return SYNTAX_ERROR;
					}
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_UNTYPED_OPERAND(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {

				merged[iEnd] = lexeme[iEnd];
				// merged[iIsi] = sb.toString();
				sb.append(lexeme[iIsi]);
				switch ((Token) lexeme[iToken]) {

				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_FINAL_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case DOT:
					return SYNTAX_FINAL_RP_DOT;
				case COMMA:
					return SYNTAX_FINAL_RP_COMMA;
				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_UNTYPED_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					// BACKTRACK to check the operand string length by looking
					// at variable temp
					if (temp.length() < 3 && temp.length() > 0) {
						// it is true, change it with new one
						if (merged[iIsi] == null)
							merged[iIsi] = temp.toString();
						temp.setLength(0);

						if (((String) lexeme[iIsi]).length() != 3) {
							simpleErrSet(err, lexeme);
							err.setMess("setelah titik hanya boleh tiga angka");
							return SYNTAX_ERROR;
						}
						// append directly, so next state wont have to add it
						// again
						temp.append(lexeme[iIsi]);
						merged[iIsi] += temp.toString();

						return SYNTAX_UNTYPED_OPERAND_AFTER_DOT;

					} else {
						err.setEnd(err.getEnd() - 1);
						err.setMess("hanya tiga angka sebelum tanda titik \".\" ");
						return SYNTAX_ERROR;
					}

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_UNTYPED_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				case COMMA:
					return SYNTAX_UNTYPED_COMMA;
				case DOT:
					return SYNTAX_UNTYPED_DOT;
				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_UNTYPED_COMMA("setelah koma harus ada angka ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case OPERAND:
					//
					if (merged[iIsi] == null)
						merged[iIsi] = temp.toString();
					temp.setLength(0);
					temp.append(".").append(lexeme[iIsi]);
					merged[iIsi] += temp.toString();

					return SYNTAX_UNTYPED_OPERAND_AFTER_COMMA;

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_UNTYPED_OPERAND_AFTER_COMMA(
				"setelah angka ini haruslah tanda matematika / operator ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder temp, Object[] merged, int level, ErrMes err) {
				// last piece of operand after dot is already in temp variable
				// merged operand already appended with that piece as well
				// so we just directly add it to queue
				merged[iEnd] = lexeme[iEnd];
				switch ((Token) lexeme[iToken]) {

				case KURUNGTUTUP:
					m.add(merged);
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					m.add(merged);
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0) {
						m.add(merged);
						return SYNTAX_EQUAL;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tanda kurungmu");
						return SYNTAX_ERROR;
					}
				case TYPE:
					merged[iKategori] = toKategori(lexeme);
					if (isItSimplePostType(lexeme)) {
						return SYNTAX_AFTERTYPE;
					} else if (toKategori(lexeme) == Kategori.jmd) {
						return SYNTAX_TYPEJMD;
					} else {
						simpleErrSet(err, lexeme);
						err.setMess("cek tulisan satuanmu");
						return SYNTAX_ERROR;
					}

				default:
					err.setEnd((int) lexeme[iEnd]);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_AFTERTYPE(
				" setelah satuan tidak boleh ada tulisan lainnya  ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] lexeme,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {
				// kategori has been set before at syntax operand
				merged[iEnd] = lexeme[iEnd];
				if (merged[iIsi] == null)
					merged[iIsi] = sb.toString();
				switch ((Token) lexeme[iToken]) {

				default:
					simpleErrSet(err, lexeme);
					err.setMess(this.getErr());
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_ERROR("Ada yang salah dengan jawaban mu Karena: ") {
			@Override
			SintakStatePT tokenSelanjutnya(Queue<Object[]> m, Object[] token,
					StringBuilder sb, Object[] merged, int level, ErrMes err) {
				// TODO Auto-generated method stub
				return SYNTAX_ERROR;
			}
		};
		private String errM;

		SintakStatePT(String b) {
			this.errM = b;
		}

		String getErr() {
			return errM;
		}

		abstract SintakStatePT tokenSelanjutnya(Queue<Object[]> modified,
				Object[] aParameter, StringBuilder temp, Object[] merged,
				int level, ErrMes em);
	}

	private enum SintakStateP {

		SYNTAX_START("kamu harus memulai jawabanmu dengan angka") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				// harus operand kalau tidak error
				case KURUNGBUKA:
					return SYNTAX_OPEN_PARENTHESIS;
				case OPERAND:
					return cekZero((String) token[iIsi]) ? SYNTAX_OPERAND
							: SYNTAX_ERROR;
				default:
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_OPEN_PARENTHESIS("Setelah tanda buka kurung harus ada angka") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				// harus operand kalau tidak error
				case KURUNGBUKA:
					return SYNTAX_OPEN_PARENTHESIS;
				case OPERAND:
					return cekZero((String) token[iIsi]) ? SYNTAX_OPERAND
							: SYNTAX_ERROR;
				default:
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_CLOSE_PARENTHESIS(
				"Setelah tanda Kurung tutup harusnya tanda + - x : = ") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				case KURUNGTUTUP:
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0)
						return SYNTAX_EQUAL;
					else
						return SYNTAX_ERROR;
				default:
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_OPERAND(
				" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				// harus operand kalau tidak error
				case KURUNGTUTUP:
					return SYNTAX_CLOSE_PARENTHESIS;
				case OPERATOR:
					return SYNTAX_OPERATOR;
				case EQUAL:
					if (level == 0)
						return SYNTAX_EQUAL;
					else
						return SYNTAX_ERROR;
				default:
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_FINAL_OPERAND(" setelah angka hasil tidak boleh ada tulisan") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {
				return SYNTAX_ERROR;
			}
		},

		SYNTAX_OPERATOR(
				" Setelah tanda matematika + - x : = ( ) harus diikuti dengan angka") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				// harus operand kalau tidak error
				case OPERAND:
					return cekZero((String) token[iIsi]) ? SYNTAX_OPERAND
							: SYNTAX_ERROR;
				case KURUNGBUKA:
					return SYNTAX_OPEN_PARENTHESIS;
				default:
					return SYNTAX_ERROR;
				}
			}
		},
		SYNTAX_EQUAL("hasil jawabanmu harus menggunakan angka") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {

				switch ((Token) token[iToken]) {
				// harus operand kalau tidak error
				case OPERAND:
					return cekZero((String) token[iIsi]) ? SYNTAX_FINAL_OPERAND
							: SYNTAX_ERROR;
				default:
					return SYNTAX_ERROR;
				}
			}
		},

		SYNTAX_ERROR("Ada yang salah dengan jawaban mu Karena: ") {
			@Override
			SintakStateP tokenSelanjutnya(Object[] token, int level) {
				// TODO Auto-generated method stub
				return SYNTAX_ERROR;
			}
		};
		private String errM;

		SintakStateP(String b) {
			this.errM = b;
		}

		String getErr() {
			return errM;
		}

		abstract SintakStateP tokenSelanjutnya(Object[] aParameter, int level);
	}

	static Object[] startAnalysisP(Queue<Object[]> lexTable) {
		// mantikTable
		Queue<Object[]> m = new LinkedList<Object[]>();
		// start FSM
		SintakStateP state = SintakStateP.SYNTAX_START;
		Object[] lexeme = null;
		int lvl = 0;
		String pesanError = "";
		while (!lexTable.isEmpty() && state != SintakStateP.SYNTAX_ERROR) {
			lexeme = lexTable.remove();
			m.add(lexeme);
			if (lexeme[iToken] == Token.KURUNGBUKA) {
				lvl++;
			}
			if (lexeme[iToken] == Token.KURUNGTUTUP) {
				lvl--;
			}
			pesanError = state.getErr();
			state = state.tokenSelanjutnya(lexeme, lvl);
		}

		if (state != SintakStateP.SYNTAX_FINAL_OPERAND)
			return new Object[] {
					false,
					SintakStateP.SYNTAX_ERROR.getErr() + statusKurung(lvl)
							+ " " + state.getErr(), lexeme };
		else {
			System.out.println("sintak benar mulai semantik");
			return new Object[] { true, m };
		}
	}

	private static boolean isItSimplePostType(Object[] t) {
		Kategori k = Converter.determineCategory(satuan
				.valueOf(((String) t[iIsi]).toLowerCase()));
		if (k == Kategori.berat || k == Kategori.panjang || k == Kategori.luas
				|| k == Kategori.volume)
			return true;
		else
			return false;
	}

	public static Kategori toKategori(Object[] t) {
		return Converter.determineCategory(satuan.valueOf(((String) t[iIsi])
				.toLowerCase()));
	}

	private static boolean checkJMDState(Object[] a, satuan t) {
		switch (t) {
		case jam:
			if ((boolean) a[0] == false)
				return true;
		case menit:
			if ((boolean) a[1] == false)
				return true;
		case detik:
			if ((boolean) a[2] == false)
				return true;
		}
		return false;
	}
}

class ErrMes {
	private String mess = "";
	private int start;
	private int end;

	public static ErrMes creatErr(String msg, int s, int e) {
		ErrMes Er = new ErrMes();
		Er.setMess(msg);
		Er.setStart(s);
		Er.setEnd(e);
		return Er;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getMess() {
		return mess;
	}

	public void setMess(String mess) {
		this.mess = mess;
	}
}