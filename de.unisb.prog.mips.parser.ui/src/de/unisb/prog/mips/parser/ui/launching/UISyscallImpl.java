package de.unisb.prog.mips.parser.ui.launching;

import java.io.PrintStream;

import de.unisb.prog.mips.os.SysCallImplementation;
import de.unisb.prog.mips.parser.ui.MIPSCore;

public class UISyscallImpl implements SysCallImplementation {

	private final PrintStream out;

	public UISyscallImpl(PrintStream output) {
		out = output;
	}

	public void print(CharSequence s) {
		out.print(s);
	}

	public void print(char ch) {
		out.print(ch);
	}

	public void print(int v) {
		out.print(v);
	}

	public void exit(int ret) {
		MIPSCore.getInstance().setExitCode(ret);
	}

	public int readString(byte[] buffer) {
		String in = MIPSCore.getInstance().getConsoleOut().startInput(buffer.length);
		byte[] inBytes = in.getBytes();
		System.arraycopy(inBytes, 0, buffer, 0, Math.min(inBytes.length, buffer.length));
		return inBytes.length;
	}

	public int readInt() {
		String in = MIPSCore.getInstance().getConsoleOut().startInput(11);

		try {
			long v = Long.decode(in);

			int hi = (int) (v >>> 32);
			int lo = (int) (v & 0xffffffff);

			if (hi == -1 || hi == 0)
				return lo;
			else
				return Integer.MIN_VALUE;
		} catch (NumberFormatException e) {
			return Integer.MIN_VALUE;
		}
	}

	public char readChar() {
		String in = MIPSCore.getInstance().getConsoleOut().startInput(1);
		return in.length() == 1 ? in.charAt(0) : 0;
	}

}
