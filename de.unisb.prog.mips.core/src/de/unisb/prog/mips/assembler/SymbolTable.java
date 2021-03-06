package de.unisb.prog.mips.assembler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.prog.mips.assembler.segments.Element;
import de.unisb.prog.mips.assembler.segments.Label;
import de.unisb.prog.mips.assembler.segments.Segment;

public class SymbolTable {

	private final Map<String, List<LabelRef>> unresolved = new HashMap<String, List<LabelRef>>();
	private final Map<String, Label> defined             = new HashMap<String, Label>();
	private final Set<String> global                     = new HashSet<String>();

	public LabelRef createRef(Assembly asm, String label) {
		Label target = defined.get(label);
		if (target != null)
			return new LabelRef(target);

		List<LabelRef> referrers = unresolved.get(label);
		if (referrers == null) {
			referrers = new LinkedList<LabelRef>();
			unresolved.put(label, referrers);
		}
		LabelRef res = new LabelRef(asm, label);
		referrers.add(res);
		return res;
	}

	public void defineLabel(Label definer) throws LabelAlreadyDefinedException {
		String label = definer.getLabel();
		if (label == null)
			return;
		if (defined.containsKey(label))
			throw new LabelAlreadyDefinedException(definer);

		defined.put(label, definer);
		List<LabelRef> referrers = unresolved.get(label);
		if (referrers != null) {
			for (LabelRef r : referrers)
				r.connectToElement(definer);
			unresolved.remove(label);
		}
	}

	public Element lookupElement(String label) throws LabelNotDefinedException {
		Element e = defined.get(label);
		if (e == null)
			throw new LabelNotDefinedException(label);
		return e;
	}

	public void makeGlobal(String label) {
		global.add(label);
	}

	public void makeGlobal(Label e) {
		makeGlobal(e.getLabel());
	}

	public void integrate(SymbolTable other) throws LabelAlreadyDefinedException {
		// connect unresolved labels in other assembly to this if possible
		for (String global : this.global) {
			Label e = defined.get(global);
			if (e == null)
				throw new IllegalStateException("global label " + global + " should be defined in this assembly");
			List<LabelRef> refs = other.unresolved.get(e.getLabel());
			if (refs != null) {
				for (LabelRef ref : refs)
					ref.connectToElement(e);
			}
		}

		// kill unresolved labels in this assembly
		for (String global : other.global) {
			Label e = other.defined.get(global);
			if (e != null) {
				defineLabel(e);
				this.global.add(global);
			}
		}

		// add other unresolved labels
		for (String u : other.unresolved.keySet()) {
			if (this.global.contains(u))
				continue;
			List<LabelRef> referrers = unresolved.get(u);
			List<LabelRef> otherReferrers = other.unresolved.get(u);
			if (referrers == null)
				unresolved.put(u, otherReferrers);
			else
				referrers.addAll(otherReferrers);
		}
	}

	public Map<String, List<LabelRef>> getUnresolved() {
		return unresolved;
	}

	public void append(Appendable app) throws IOException {
		for (String s : defined.keySet()) {
			Element e = defined.get(s);
			app.append(String.format("%20s %s", s, global.contains(s) ? "D" : "d", e.getOffset()));
			if (e.getSegment().getState() == Segment.State.RELOCATED)
				app.append(String.format(" %08x", e.addressOf()));
			app.append('\n');
		}

		for (String s : unresolved.keySet()) {
			app.append(String.format("%20s u\n", s, unresolved.get(s)));
		}
	}

}
