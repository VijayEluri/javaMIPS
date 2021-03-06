package de.unisb.prog.mips.assembler;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.unisb.prog.mips.assembler.segments.Data;
import de.unisb.prog.mips.assembler.segments.Element;
import de.unisb.prog.mips.assembler.segments.Label;
import de.unisb.prog.mips.assembler.segments.Segment;
import de.unisb.prog.mips.assembler.segments.text.Text;
import de.unisb.prog.mips.simulator.Memory;
import de.unisb.prog.mips.util.Pair;

public class Assembly extends SymbolTable {

	private Text text = new Text(this);
	private Data data = new Data(this);

	private final ErrorReporter<Position> reporter;

	public Assembly(ErrorReporter<Position> reporter) {
		this.reporter = reporter;
	}

	public Assembly() {
		this(ErrorReporter.POSITION_STD_REPORTER);
	}

	public Text getText() {
		return text;
	}

	public Data getData() {
		return data;
	}

	public ErrorReporter<Position> getReporter() {
		return reporter;
	}

	public LabelRef createRef(String label) {
		return createRef(this, label);
	}

	public void prepare() {
		data.prepare(reporter);
		text.prepare(reporter);
	}

	public void relocate(MemoryLayout layout) {
		// set base addresses of segments
		data.setBase(layout.dataStart());
		text.setBase(layout.textStart());
		data.relocate(reporter);
		text.relocate(reporter);
	}

	public void writeToMem(Memory mem) {
		data.writeToMem(mem);
		text.writeToMem(mem);
	}

	@Override
	public void append(Appendable app) throws IOException {
		app.append("Assembly\n");
		super.append(app);
		data.append(app);
		text.append(app);
	}

	public Element getElementAt(int addr) {
		if (text.isInside(addr))
			return text.getElementAt(addr);
		if (data.isInside(addr))
			return data.getElementAt(addr);
		return null;
	}

	public Position getPosition(int addr) {
		Element elm = getElementAt(addr);
		return elm != null ? elm.getPosition() : Position.ILLEGAL;
	}

	public void linkWith(Assembly asm) throws LabelAlreadyDefinedException {
		// integrate the other symbol table
		integrate(asm);

		// add the other segments' elements to this one.
		text.addAll(asm.text);
		data.addAll(asm.data);
	}

	public static Assembly link(Collection<Assembly> assemblies, ErrorReporter<Position> reporter) {
		Assembly linked = new Assembly(reporter);
		for (Assembly asm : assemblies) {
			try {
				linked.linkWith(asm);
			} catch (LabelAlreadyDefinedException e) {
				Label labelled = e.label;
				reporter.error(labelled.getPosition(), "label \"%s\" multiply defined", labelled.getLabel());
			}
		}

		for (String u : linked.getUnresolved().keySet())
			reporter.error("symbol \"%s\" cannot be resolved", u);

		return linked;
	}

	public Map<Pair<URI, Integer>, Element> computeElementMap() {
		Map<Pair<URI, Integer>, Element> res = new HashMap<Pair<URI,Integer>, Element>();
		for (Segment seg : new Segment[] { text, data }) {
			for (Element e : seg) {
				Position pos = e.getPosition();
				if (pos != null && pos.getURI() != null) {
					Pair<URI, Integer> p = new Pair<URI, Integer>(pos.getURI(), pos.getLineNumber());
					if (!res.containsKey(p))
						res.put(p, e);
				}
			}
		}
		return res;
	}
}
