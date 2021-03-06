package de.unisb.prog.mips.assembler.segments;

import java.io.IOException;

import de.unisb.prog.mips.simulator.Memory;
import de.unisb.prog.mips.simulator.Type;

public class Str extends Element {
	private final String str; 
	private final boolean zeroTerminate;
	
	public Str(Segment seg, String str, boolean zeroTerminate) {
		super(seg);
		this.str = str;
		this.zeroTerminate = zeroTerminate;
	}

	@Override
	protected void appendInternal(Appendable app) throws IOException {
		app.append(".ascii");
		app.append(zeroTerminate ? "z \"" : "  \"");
		app.append(str);
		app.append('\"');
	}

	@Override
	public int nextElementOffset(int pos) {
		return pos + str.length() + (zeroTerminate ? 1 : 0);
	}

	@Override
	public void writeToMem(Memory mem, int addr) {
		// TODO: Check Unicode 
		for (int i = 0, n = str.length(); i < n; i++)
			mem.store(addr + i, str.charAt(i), Type.BYTE);
	}

}
