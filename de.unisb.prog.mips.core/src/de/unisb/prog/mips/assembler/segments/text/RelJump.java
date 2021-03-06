package de.unisb.prog.mips.assembler.segments.text;


import de.unisb.prog.mips.assembler.ErrorReporter;
import de.unisb.prog.mips.assembler.Expressions;
import de.unisb.prog.mips.assembler.LabelRef;
import de.unisb.prog.mips.assembler.Position;
import de.unisb.prog.mips.assembler.Reg;
import de.unisb.prog.mips.assembler.segments.Segment;
import de.unisb.prog.mips.insn.Immediate;
import de.unisb.prog.mips.insn.Instruction;

class RelJump extends LabelRefInsn {
	
	RelJump(Segment seg, Instruction insn, Reg rs, Reg rt, LabelRef ref) {
		this(seg, rt.encodeInto(rs.encodeInto(insn.encodeOpcodeInto(0), Instruction.FIELD_RS), Instruction.FIELD_RT), ref);
	}
	
	RelJump(Segment seg, Instruction insn, Reg rs, LabelRef ref) {
		this(seg, rs.encodeInto(insn.encodeOpcodeInto(0), Instruction.FIELD_RS), ref);
	}
	
	RelJump(Segment seg, int word, LabelRef ref) {
		super(seg, word, ref);
	}
	
	public void rewrite(ErrorReporter<Position> reporter) {
		int labelOffset = ref.eval();
		
		if ((labelOffset & 3) != 0)
			reporter.error(String.format("jump target \"%s\" is not aligned", Expressions.toString(ref)), getPosition());
		
		int rel = (labelOffset - getOffset()) >> 2;
		if (! Immediate.SEXT_16.fits(rel))
			reporter.error(String.format("jump target \"%s\" out of range", Expressions.toString(ref)), getPosition());
		
		word = Instruction.FIELD_IMM.insert(word, rel);
	}

}
