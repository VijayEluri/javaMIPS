package de.unisb.prog.mips.parser.ui.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class HighlighMarkerUpdater implements IMarkerUpdater {

	public HighlighMarkerUpdater() {

	}

	public String getMarkerType() {
		return MarkerUtil.ID_Highlighting;
	}

	public String[] getAttribute() {
		return null;
	}

	public boolean updateMarker(IMarker marker, IDocument document,	Position position) {
		return true;
	}

}
