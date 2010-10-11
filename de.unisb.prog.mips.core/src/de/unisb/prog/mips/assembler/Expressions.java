package de.unisb.prog.mips.assembler;

import java.io.IOException;

public class Expressions {
	
	public static enum IntOp {
		ADD("+") { public int op(int a, int b) { return a + b; } },
		SUB("-") { public int op(int a, int b) { return a - b; } },
		MUL("*") { public int op(int a, int b) { return a * b; } },
		SHL("<<") { public int op(int a, int b) { return a << b; } },
		SHRA(">>") { public int op(int a, int b) { return a >> b; } },
		SHR(">>>") { public int op(int a, int b) { return a >>> b; } };
		
		public final String sym;
		
		private IntOp(String sym) {
			this.sym = sym;
		}
		
		public abstract int op(int a, int b);
	}
	
	private static class IntBinOp implements Expr {
		private final IntOp op;
		private final Expr left, right;
		
		IntBinOp(IntOp op, Expr left, Expr right) {
			this.op = op;
			this.left = left;
			this.right = right;
		}

		@Override
		public int eval() {
			return op.op(left.eval(), right.eval());
		}

		@Override
		public void append(Appendable app) throws IOException {
			app.append('(');
			left.append(app);
			app.append(op.sym);
			right.append(app);
		}
	}

	public static final Expr ZERO = constantInt(0);
	
	public static Expr binary(IntOp op, Expr left, Expr right) {
		return new IntBinOp(op, left, right);
	}
	
	public static Expr constantInt(final int i) {
		return new Expr() {
			@Override
			public int eval() {
				return i;
			}

			@Override
			public void append(Appendable app) throws IOException {
				app.append(Integer.toString(i));
			}
		};
	}

	public static final Expr relative(Address addr) {
		return addr;
	}
	
	public static final Expr absolute(final Address addr) {
		return new Expr() {
			@Override
			public int eval() {
				return addr.getSegment().getBase() + addr.eval();
			}

			@Override
			public void append(Appendable app) throws IOException {
				addr.append(app);
			}
		};
	}
	
	public static String toString(Expr e) {
		StringBuffer sb = new StringBuffer();
		try {
			e.append(sb);
		} catch (IOException e1) {
		}
		return sb.toString();
	}

}
