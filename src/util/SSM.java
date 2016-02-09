package util;

import java.util.Queue;

enum SSM {
	// this FSM will also merge operand as necessary
	SYNTAX_START("kamu harus memulai jawabanmu dengan angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGBUKA:
				m.add(lexeme);
				return SYNTAX_OPEN_PARENTHESIS;
			case OPERATOR:
				if ((input.substring(lexeme.getStart(), lexeme.getEnd())).equals("-")) {
					// don't add it yet to queue, as it will be added after
					// merged as an operand
					merged.setNegative(true);
					return SYNTAX_NEGATIVE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

					return SYNTAX_ERROR;
				}

			case OPERAND:
				// add to temp instead
				merged.setStart(lexeme.getStart());
				merged.setEnd(lexeme.getEnd());
				merged.setKategori(Kategori.untyped);
				merged.setToken(Token.OPERAND);

				// sb.append((String) lexeme[SintakProcessor.iIsi]);
				SintakProcessor.simpleErrSet(err, lexeme);
				return SYNTAX_UNTYPED_OPERAND;
			case SATUAN:
				// hanya rp saja yang boleh
				
				if (Satuan.rp == Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					// don't add it yet to queue

					return SYNTAX_RP;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("huruf ini tidak boleh ditulis diawal");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},
	// rp types.. routes
	SYNTAX_OPERATOR(" ini tidak bisa ditulis setelah tanda operasi matematika") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGBUKA:
				m.add(lexeme);
				return SYNTAX_OPEN_PARENTHESIS;
			case OPERATOR:
				if ((input.substring(lexeme.getStart(), lexeme.getEnd())).equals("-")) {
					// don't add it yet to queue, as it will be added after
					// merged as an operand
					merged.setNegative(true);
					return SYNTAX_NEGATIVE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

					return SYNTAX_ERROR;
				}
			case OPERAND:
				// add to temp instead
				merged.setStart(lexeme.getStart());
				merged.setEnd(lexeme.getEnd());
				merged.setKategori(Kategori.untyped);
				merged.setToken(Token.OPERAND);
				// System.out.println("is it ERROR--"+lexeme);
				lastLexeme.setNewValues(lexeme);
				// sb.append((String) lexeme[SintakProcessor.iIsi]);
				SintakProcessor.simpleErrSet(err, lexeme);
				return SYNTAX_UNTYPED_OPERAND;
			case SATUAN:
				// hanya rp saja yang boleh
				if (Satuan.rp == Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					// don't add it yet to queue
					return SYNTAX_RP;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("huruf ini tidak boleh ditulis diawal");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_EQUAL("hasil jawabanmu harus menggunakan angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			// nullify(merged);
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERATOR:
				if ((input.substring(lexeme.getStart(), lexeme.getEnd())).equals("-")) {
					// don't add it yet to queue, as it will be added after
					// merged as an operand
					merged.setNegative(true);
					return SYNTAX_FINAL_NEGATIVE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");

					return SYNTAX_ERROR;
				}
			case OPERAND:
				// add to temp instead
				merged.setStart(lexeme.getStart());
				merged.setEnd(lexeme.getEnd());
				merged.setKategori(Kategori.untyped);
				merged.setToken(Token.OPERAND);

				lastLexeme.setNewValues(lexeme);

				SintakProcessor.simpleErrSet(err, lexeme);
				m.add(merged);
				return SYNTAX_FINAL_UNTYPED_OPERAND;
			case SATUAN:
				if (Satuan.rp == Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					//m.add(merged);
					return SYNTAX_FINAL_RP;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("huruf ini tidak boleh ditulis diawal");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_CLOSE_PARENTHESIS("Setelah tanda Kurung tutup harusnya tanda + - x : = ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			// nullify(merged);
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:
				if (lvl == 0) {
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_OPEN_PARENTHESIS("Setelah tanda buka kurung harus ada angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			// nullify(merged);
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGBUKA:
				m.add(lexeme);
				return SYNTAX_OPEN_PARENTHESIS;
			case OPERATOR:
				if (isItNegativeSym(lexeme, input)) {
					// don't add it yet to queue, as it will be added after
					// merged as an operand

					merged.setNegative(true);
					return SYNTAX_NEGATIVE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya tanda buka kurung atau tanda negatif diizinkan di awal");
					return SYNTAX_ERROR;
				}
			case OPERAND:
				// add to temp instead
				merged.setStart(lexeme.getStart());
				merged.setEnd(lexeme.getEnd());
				merged.setKategori(Kategori.untyped);
				merged.setToken(Token.OPERAND);

				// sb.append((String) lexeme[SintakProcessor.iIsi]);
				SintakProcessor.simpleErrSet(err, lexeme);
				return SYNTAX_UNTYPED_OPERAND;
			case SATUAN:
				// hanya rp saja yang boleh
				if (Satuan.rp == Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					// don't add it yet to queue
					return SYNTAX_RP;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("huruf ini tidak boleh ditulis diawal");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP("setelah mata uang rupiah harus angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			merged.setKategori(Kategori.uang);
			merged.setSatuan(Satuan.rp);
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERAND:
				// sb.setLength(0);

				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {
					// sb.append(lexeme[SintakProcessor.iIsi]);
					merged.setToken(Token.OPERAND);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());

					lastLexeme.setNewValues(lexeme);

					return SYNTAX_RP_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP_OPERAND(" seteleh angka dalam rupiah hanya boleh tanda matematika ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_RP_DOT;
			case COMMA:
				return SYNTAX_RP_COMMA;
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				// BACKTRACK to check the operand string length by looking
				// at variable temp
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_RP_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak beoleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:
				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			case COMMA:
				return SYNTAX_RP_COMMA;
			case TITIK:
				return SYNTAX_RP_DOT;
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_RP_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_RP_OPERAND_AFTER_COMMA("setelah angka ini haruslah tanda matematika / operator ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well
			// so we just directly add it to queue
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:
				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_NEGATIVE("setelah tanda negatif harus ada angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERAND:

				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {
					merged.setNegative(true);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());
					merged.setKategori(Kategori.untyped);
					
					return SYNTAX_UNTYPED_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_UNTYPED_OPERAND(" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_UNTYPED_DOT;
			case COMMA:
				return SYNTAX_UNTYPED_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_AFTERTYPE(" harus ada tanda matematika + - x : = ( ) setelah satuan ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// kategori has been set before at syntax operand

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_JMD_TYPE(" harus diikuti dengan angka untuk jam, menit, atau detik berikutnya ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// kategori has been set before at syntax operand
			// this here should be new instance of "merged"
			if (merged.isNegative()) {
				SintakProcessor.simpleErrSet(err, merged);
				err.setMess("tidak boleh negatif");
				return SYNTAX_ERROR;
			}

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;

			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}

			case OPERAND:
				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {

					merged.setToken(Token.OPERAND);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					// create temporary sub first
					merged.setTempJ(new SubOperand(lexeme));
					return SYNTAX_JMD_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_JMD_OPERAND(" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya boleh jam, menit dan detik");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_JMD_DOT;
			case COMMA:
				return SYNTAX_JMD_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_JMD_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					merged.getTempJ().setSubEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_JMD_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak beoleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_JMD_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// vopied from untyped after dot
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_JMD_DOT;
			case COMMA:
				return SYNTAX_JMD_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_JMD_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				merged.getTempJ().setSubEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_JMD_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}

		}
	},

	SYNTAX_JMD_OPERAND_AFTER_COMMA("setelah angka ini haruslah tanda matematika / operator ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// copied from UNTYPED AFTER COMMA
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_UNTYPED_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub

			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak beoleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_FINAL_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_UNTYPED_DOT;
			case COMMA:
				return SYNTAX_UNTYPED_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_UNTYPED_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_UNTYPED_OPERAND_AFTER_COMMA(
			"setelah angka ini haruslah tanda matematika / operator ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well
			// so we just directly add it to queue
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_FINAL_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}

			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP("setelah rupiah harus angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			merged.setKategori(Kategori.uang);
			merged.setSatuan(Satuan.rp);
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERAND:
				// sb.setLength(0);

				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {
					// sb.append(lexeme[SintakProcessor.iIsi]);
					merged.setToken(Token.OPERAND);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());

					lastLexeme.setNewValues(lexeme);

					return SYNTAX_FINAL_RP_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP_OPERAND(" seteleh angka dalam rupiah hanya boleh tanda matematika ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case TITIK:
				return SYNTAX_FINAL_RP_DOT;
			case COMMA:
				return SYNTAX_FINAL_RP_COMMA;
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				// BACKTRACK to check the operand string length by looking
				// at variable temp
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_FINAL_RP_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak beoleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case COMMA:
				return SYNTAX_FINAL_RP_COMMA;
			case TITIK:
				return SYNTAX_FINAL_RP_DOT;
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_FINAL_RP_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_RP_OPERAND_AFTER_COMMA("setelah angka ini haruslah tanda matematika / operator ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well
			// so we just directly add it to queue
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_NEGATIVE("setelah tanda negatif harus ada angka") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERAND:
				
				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {
					merged.setNegative(true);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());
					merged.setKategori(Kategori.untyped);
					
					return SYNTAX_FINAL_UNTYPED_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_UNTYPED_OPERAND(" harus ada tanda matematika + - x : = ( ) setelah angka  ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_FINAL_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_FINAL_UNTYPED_DOT;
			case COMMA:
				return SYNTAX_FINAL_UNTYPED_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},
	SYNTAX_FINAL_JMD_TYPE(" harus diikuti dengan angka untuk jam, menit, atau detik berikutnya ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// kategori has been set before at syntax operand
			// this here should be new instance of "merged"
			if (merged.isNegative()) {
				SintakProcessor.simpleErrSet(err, merged);
				err.setMess("tidak boleh negatif");
				return SYNTAX_ERROR;
			}

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case OPERAND:
				if (SintakProcessor.cekZero(input.substring(lexeme.getStart(), lexeme.getEnd()))) {

					merged.setToken(Token.OPERAND);
					merged.setStart(lexeme.getStart());
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					// create temporary sub first
					merged.setTempJ(new SubOperand(lexeme));
					return SYNTAX_FINAL_JMD_OPERAND;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan angka mu, tidak boleh dimulai dengan 0");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},
	SYNTAX_FINAL_JMD_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			// merged[SintakProcessor.iEnd] = lexeme[SintakProcessor.iEnd];
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				merged.getTempJ().setSubEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_FINAL_JMD_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}

		}
	},
	SYNTAX_FINAL_JMD_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					merged.getTempJ().setSubEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_FINAL_JMD_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak beoleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},
	SYNTAX_FINAL_JMD_OPERAND(" harus dilengkapi dengan jam menit atau detik  ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("hanya boleh jam, menit dan detik");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_FINAL_JMD_DOT;
			case COMMA:
				return SYNTAX_FINAL_JMD_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_FINAL_JMD_OPERAND_AFTER_DOT("harus dilengkapi dengan jam menit atai detik") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// vopied from untyped after dot
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_FINAL_JMD_DOT;
			case COMMA:
				return SYNTAX_FINAL_JMD_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_FINAL_JMD_OPERAND_AFTER_COMMA("harus dilengkapi dengan jam, menit atau detik") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// copied from UNTYPED AFTER COMMA
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				Satuan s = Satuan.valueOf(input.substring(lexeme.getStart(), lexeme.getEnd())
						.toLowerCase());
				merged.setKategori(k);
				if (k == Kategori.jmd) {
					merged.setKategori(Kategori.jmd);
					merged.setSatuan(s);
					// no need to set the end of it since already set before
					merged.getTempJ().setSatuan(s);
					if (!merged.pushTempJMDLexeme()) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess(s.toString() + " sudah di sebutkan sebelum nya");
						return SYNTAX_ERROR;
					}
					return SYNTAX_FINAL_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_UNTYPED_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub

			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda titik");
				return SYNTAX_ERROR;
			case OPERAND:
				if (lastLexeme.getLength() <= 3 && lastLexeme.getLength() > 0) {

					// check again if current lexeme has exactly 3 digit (after
					// dot)
					if (lexeme.getLength() != 3) {
						SintakProcessor.simpleErrSet(err, lexeme);
						err.setMess("setelah titik hanya boleh tiga angka");
						return SYNTAX_ERROR;
					}
					merged.setEnd(lexeme.getEnd());
					lastLexeme.setNewValues(lexeme);
					return SYNTAX_UNTYPED_OPERAND_AFTER_DOT;

				} else {
					// simpleErrSet(err, lexeme);
					SintakProcessor.simpleErrSet(err, lastLexeme);
					err.setMess("sebelum titik tidak boleh lebih dari tiga angka");
					return SYNTAX_ERROR;
				}

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_UNTYPED_OPERAND_AFTER_DOT("setelah titik harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case EQUAL:

				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case TITIK:
				return SYNTAX_UNTYPED_DOT;
			case COMMA:
				return SYNTAX_UNTYPED_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;

			}
		}
	},

	SYNTAX_UNTYPED_COMMA("setelah koma harus ada angka ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// TODO Auto-generated method stub
			switch (lexeme.getToken()) {
			case WHITESPACE:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess("tidak boleh ada spasi setelah tanda koma");
				return SYNTAX_ERROR;
			case OPERAND:
				merged.setEnd(lexeme.getEnd());
				lastLexeme.setNewValues(lexeme);
				return SYNTAX_UNTYPED_OPERAND_AFTER_COMMA;

			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_UNTYPED_OPERAND_AFTER_COMMA("setelah angka ini haruslah tanda matematika / operator ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// last piece of operand after dot is already in temp variable
			// merged operand already appended with that piece as well
			// so we just directly add it to queue
			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			case KURUNGTUTUP:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_CLOSE_PARENTHESIS;
			case OPERATOR:
				m.add(merged);
				m.add(lexeme);
				return SYNTAX_OPERATOR;
			case SATUAN:
				// merged[SintakProcessor.iKategori] =
				// SintakProcessor.toKategori(lexeme);
				Kategori k = Converter.toKategori(input.substring(lexeme.getStart(),
						lexeme.getEnd()).toLowerCase());
				merged.setKategori(k);
				if (Converter.isItSimplePostType(input
						.substring(lexeme.getStart(), lexeme.getEnd()).toLowerCase())) {
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.setKategori(k);
					return SYNTAX_AFTERTYPE;
				} else if (k == Kategori.jmd) {
					merged.setKategori(k);
					merged.setSatuan(Satuan.valueOf(input.substring(lexeme.getStart(),
							lexeme.getEnd()).toLowerCase()));
					merged.pushJMDLexeme(new SubOperand().setSatuan(merged.getSatuan())
							.setSubEnd(merged.getEnd()).setSubStart(merged.getStart()));
					return SYNTAX_JMD_TYPE;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tulisan satuanmu");
					return SYNTAX_ERROR;
				}
			case EQUAL:
				if (lvl == 0) {
					m.add(merged);
					m.add(lexeme);
					return SYNTAX_EQUAL;
				} else {
					SintakProcessor.simpleErrSet(err, lexeme);
					err.setMess("cek tanda kurungmu");
					return SYNTAX_ERROR;
				}
			default:
				err.setEnd(lexeme.getEnd());
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_FINAL_AFTERTYPE(" setelah satuan tidak boleh ada tulisan lainnya  ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme merged,
				String input, int lvl, ErrMes err) {
			// kategori has been set before at syntax operand

			switch (lexeme.getToken()) {
			case WHITESPACE:
				return this;
			default:
				SintakProcessor.simpleErrSet(err, lexeme);
				err.setMess(this.getErr());
				return SYNTAX_ERROR;
			}
		}
	},

	SYNTAX_ERROR("Ada yang salah dengan jawaban mu Karena: ") {
		@Override
		SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme, TypedLexeme tLex,
				String input, int lvl, ErrMes em) {
			// TODO Auto-generated method stub
			return SYNTAX_ERROR;
		}
	};

	private String errM;

	SSM(String b) {
		this.errM = b;
	}

	String getErr() {
		return errM;
	}

	static boolean isItNegativeSym(Lexeme l, String s) {
		return s.substring(l.getStart(), l.getEnd()).equalsIgnoreCase("-");
	}

	abstract SSM tokenSelanjutnya(Queue<Lexeme> m, Lexeme lexeme, Lexeme lastLexeme,
			TypedLexeme tLex, String input, int lvl, ErrMes em);
}