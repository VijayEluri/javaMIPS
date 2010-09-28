package de.unisb.prog.mips.assembler;

public enum Reg {
	
	/* 00 */ zero,
	/* 01 */ at,
	/* 02 */ v0,
	/* 03 */ v1,
	/* 04 */ a0,
	/* 05 */ a1,
	/* 06 */ a2,
	/* 07 */ a3,
	/* 08 */ t0,
	/* 09 */ t1,
	/* 10 */ t2,
	/* 11 */ t3,
	/* 12 */ t4,
	/* 13 */ t5,
	/* 14 */ t6,
	/* 15 */ t7,
	/* 16 */ s0,
	/* 17 */ s1,
	/* 18 */ s2,
	/* 19 */ s3,
	/* 20 */ s4,
	/* 21 */ s5,
	/* 22 */ s6,
	/* 23 */ s7,
	/* 24 */ t8,
	/* 25 */ t9,
	/* 26 */ k0,
	/* 27 */ k1,
	/* 28 */ gp,
	/* 29 */ sp,
	/* 30 */ fp,
	/* 31 */ ra;
	
	public int get(int[] gp) { return gp[ordinal()]; }
	public static Reg get(int idx) { return Reg.values()[idx & 0x1f]; }

}