package de.unisb.prog.mips.assembler.segments.text;

import de.unisb.prog.mips.assembler.Address;
import de.unisb.prog.mips.assembler.ErrorReporter;
import de.unisb.prog.mips.assembler.Expressions;
import de.unisb.prog.mips.assembler.Position;
import de.unisb.prog.mips.assembler.segments.Segment;
import de.unisb.prog.mips.insn.Instruction;

class AbsJump extends LabelRefInsn implements Relocateable {

	AbsJump(Segment seg, Instruction insn, Address addr) {
		super(seg, insn.encodeOpcodeInto(0), addr);
	}

	public void relocate(int startAddress, ErrorReporter<Position> reporter) {
		int addr = startAddress + this.ref.eval();
		if ((addr & 0x0fffffff) != addr)
			reporter.error(String.format("jump target \"%s\" out of range", Expressions.toString(this.ref)), getPosition());
		this.word = Instruction.FIELD_TARGET.insert(this.word, addr >>> 2);
	}

}
