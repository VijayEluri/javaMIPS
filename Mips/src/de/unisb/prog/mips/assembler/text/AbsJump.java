package de.unisb.prog.mips.assembler.text;

import de.unisb.prog.mips.assembler.LabelRef;
import de.unisb.prog.mips.insn.Encode;
import de.unisb.prog.mips.insn.Opcode;

class AbsJump extends LabelRefInsn {
	
	AbsJump(Opcode opc, LabelRef ref) {
		super(Encode.j(opc, 0), ref);
	}

}
