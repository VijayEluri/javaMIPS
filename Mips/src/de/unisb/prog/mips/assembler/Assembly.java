package de.unisb.prog.mips.assembler;

import java.util.Map;

import de.unisb.prog.mips.assembler.data.Data;
import de.unisb.prog.mips.assembler.text.Text;

public class Assembly extends SymbolByteBuffer {
	
	private Map<String, Label> labels;
	private Map<String, LabelRef> refs;
	
	private Text text;
	private Data data;
	
	public void registerLabel(Label l) {
		labels.put(l.toString(), l);
	}
	
	public LabelRef getLabelRef(String name) {
		Label l = labels.get(name);
		return new LabelRef(l);
	}
	
	public void computeAddresses() {
		// data.computeAddresses();
		
	}
	
	@SuppressWarnings("serial")
	public static class OutOfRange extends Exception { }
	
	public void patchRelativeJump(int jumpAddr, int targetAddr) throws OutOfRange {
		assert (jumpAddr & 3) == 0 : "target address not aligned to 4";
		assert (targetAddr & 3) == 0 : "target address not aligned to 4";
		
		int ofs = (targetAddr >> 2) - (jumpAddr >> 2);
		if (ofs > Short.MAX_VALUE || ofs < Short.MIN_VALUE)
			throw new OutOfRange();
		
		ofs &= 0xffff;
		int insn = get(jumpAddr);
		insn = (insn & ~0xffff) | ofs;
		set(jumpAddr, insn);
	}
	
	public void patchAbsoluteJump(int jumpAddr, int targetAddr) throws OutOfRange {
		assert (jumpAddr & 3) == 0 : "target address not aligned to 4";
		assert (targetAddr & 3) == 0 : "target address not aligned to 4";
		
		int mask = (1 << 26) - 1;
		int ofs = (targetAddr >> 2) & mask;
		if ((ofs & ~mask) != 0)
			throw new OutOfRange();
		
		int insn = get(jumpAddr);
		insn = (insn & ~mask) | ofs;
		set(jumpAddr, insn);
	}

}
