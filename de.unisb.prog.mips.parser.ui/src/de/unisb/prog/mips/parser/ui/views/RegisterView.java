package de.unisb.prog.mips.parser.ui.views;


import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.unisb.prog.mips.assembler.Assembly;
import de.unisb.prog.mips.assembler.Reg;
import de.unisb.prog.mips.parser.ui.MIPSCore;
import de.unisb.prog.mips.parser.ui.launching.IExecutionListener;
import de.unisb.prog.mips.simulator.Processor;
import de.unisb.prog.mips.simulator.ProcessorState.ExecutionState;
import de.unisb.prog.mips.simulator.Sys;

public class RegisterView extends ViewPart implements IExecutionListener {
	public static final String ID = "de.unisb.prog.mips.parser.ui.views.RegisterView";

	private TableViewer viewer;
	private Sys system = null;
	private final int[] lastRegValues = new int[Reg.values().length];
	private final int[] lastPRegValues = new int[PReg.values().length];
	private final boolean[] regChanged = new boolean[Reg.values().length];
	private final boolean[] pregChanged = new boolean[PReg.values().length];

	private Action toggleSignedUnsignedAction;
	private boolean showDecimalsSigned = true;

	private enum PReg {
		pc ("The program counter"),
		lo ("low part of multiplication / division"),
		hi ("high  part of multiplication / division");

		public final String description;
		PReg(String desc) {
			description = desc;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

		}

		public void dispose() {

		}

		public Object[] getElements(Object parent) {
			int gpSize = Reg.values().length;
			Object[] objects = new Object[Reg.values().length + 3];
			System.arraycopy(Reg.values(), 0, objects, 0, Reg.values().length);
			objects[gpSize] = PReg.pc;
			objects[gpSize+1] = PReg.lo;
			objects[gpSize+2] = PReg.hi;
			return objects;
		}
	}

	class ViewLabelProvider extends StyledCellLabelProvider {
		private final Font regFont, changedFont;

		public ViewLabelProvider(final Font regFont, final Font changedFont) {
			this.regFont = regFont;
			this.changedFont = changedFont;
		}

		@Override
		public void update(ViewerCell cell) {
			Processor proc = system == null ? null : system.getProcessor();

			if (cell.getElement() instanceof Reg) {
				Reg reg = (Reg) cell.getElement();
				cell.setFont(regChanged[reg.ordinal()] ? changedFont : regFont);

				switch (cell.getColumnIndex()) {
				case 0:
					cell.setText(reg.name());
					break;
				case 1:
					if (system == null || system.getProcessor().state == ExecutionState.RUNNING)
						cell.setText("---");
					else {
						long val = proc.gp[reg.ordinal()];
						if (!showDecimalsSigned)
							val &= (1l << Integer.SIZE) - 1;

						cell.setText("" + val);
					}
					break;
				case 2:
					if (system == null || system.getProcessor().state == ExecutionState.RUNNING)
						cell.setText("---");
					else {
						int val = proc.gp[reg.ordinal()];
						cell.setText(String.format("0x%08x", val));
					}
					break;
				}
			} else if (cell.getElement() instanceof PReg) {
				long val = 0;
				PReg reg = (PReg) cell.getElement();
				cell.setFont(pregChanged[reg.ordinal()] ? changedFont : regFont);

				if (system != null) {
					switch (reg) {
					case pc:
						val = system.getProcessor().pc;
						break;
					case hi:
						val = system.getProcessor().hi;
						break;
					case lo:
						val = system.getProcessor().lo;
						break;
					}
				}

				if (!showDecimalsSigned)
					val &= (1l << Integer.SIZE) - 1;

				switch (cell.getColumnIndex()) {
				case 0:
					cell.setText(reg.name());
					break;
				case 1:
					if (system == null || system.getProcessor().state == ExecutionState.RUNNING)
						cell.setText("---");
					else {
						cell.setText("" + val);
					}
					break;
				case 2:
					if (system == null || system.getProcessor().state == ExecutionState.RUNNING)
						cell.setText("---");
					else {
						cell.setText(String.format("0x%08x", val));
					}
					break;
				}
			}
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof Reg) {
				Reg reg = (Reg) element;
				return reg.description;
			} else if (element instanceof PReg) {
				PReg reg = (PReg) element;
				return reg.description;
			} else {
				return null;
			}
		}
	}

	private void createColumns(TableViewer viewer) {
		String[] titles = { "Reg", "Dec", "Hex"};
		int[] bounds = { 50, 80, 80 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
			column.getColumn().setAlignment(SWT.RIGHT);
		}

		Table table = viewer.getTable();
		table.setFont(JFaceResources.getTextFont());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		createColumns(viewer);
		ColumnViewerToolTipSupport.enableFor(viewer);
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
		Font regFont = JFaceResources.getTextFont();
		viewer.getTable().setFont(regFont);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider(regFont, boldFont));
		viewer.setInput(getViewSite());

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "de.unisb.prog.mips.parser.ui.viewer");

		makeActions();
		contributeToActionBars();

		MIPSCore.getInstance().addExecutionListener(this);

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(toggleSignedUnsignedAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(toggleSignedUnsignedAction);
	}

	private void setSignedUnsignedIcon() {
		toggleSignedUnsignedAction.setImageDescriptor(
			showDecimalsSigned
			? JFaceResources.getImageRegistry().getDescriptor(MIPSCore.ICN_SIGNED)
			: JFaceResources.getImageRegistry().getDescriptor(MIPSCore.ICN_UNSIGNED)
		);
	}

	private void makeActions() {
		toggleSignedUnsignedAction = new Action() {
			@Override
			public void run() {
				showDecimalsSigned = !showDecimalsSigned;
				viewer.getTable().getDisplay().syncExec(new Runnable() {
					public void run() {
						viewer.refresh();
						setSignedUnsignedIcon();
					}
				});
			}
		};
		toggleSignedUnsignedAction.setText("Interpret numbers as unsigned");
		toggleSignedUnsignedAction.setToolTipText("Toggle if the decimal view in the table should interpret numbers signed or unsigned.");
		setSignedUnsignedIcon();
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		MIPSCore.getInstance().removeExecutionListener(this);
	}

	private void resetChangedRegs(Sys sys) {
		Arrays.fill(regChanged, false);
		Arrays.fill(pregChanged, false);

		Processor proc = sys.getProcessor();

		for (Reg r : Reg.values())
			lastRegValues[r.ordinal()] = sys.getProcessor().gp[r.ordinal()];
		lastPRegValues[PReg.pc.ordinal()] = proc.pc;
		lastPRegValues[PReg.hi.ordinal()] = proc.hi;
		lastPRegValues[PReg.lo.ordinal()] = proc.lo;
	}

	private void checkChangedRegs(Sys sys) {
		Processor proc = sys.getProcessor();

		for (Reg r : Reg.values()) {
			int oldVal = lastRegValues[r.ordinal()];
			int newVal = proc.gp[r.ordinal()];
			regChanged[r.ordinal()] = oldVal != newVal;
		}

		pregChanged[PReg.pc.ordinal()] = lastPRegValues[PReg.pc.ordinal()] != proc.pc;
		pregChanged[PReg.lo.ordinal()] = lastPRegValues[PReg.lo.ordinal()] != proc.lo;
		pregChanged[PReg.hi.ordinal()] = lastPRegValues[PReg.hi.ordinal()] != proc.hi;
	}

	// Execution Event Handling

	public void execStarted(Sys sys, Assembly asm) {
		system = sys;
		resetChangedRegs(sys);
	}

	public void execPaused(Sys sys, Assembly asm) {
		checkChangedRegs(sys);

		viewer.getTable().getDisplay().syncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});

		resetChangedRegs(sys);
	}

	public void execContinued(Sys sys, Assembly asm) {
		execStarted(sys, asm); // Does the same
	}

	public void execStepped(Sys sys, Assembly asm) {
		execPaused(sys, asm); // Does the same
	}

	public void execFinished(Sys sys, Assembly asm, boolean interrupted) {
		execPaused(sys, asm); // Does the same
	}

	public void inputModeStarted() {
		// Nothing
	}

	public void inputModeDone() {
		// Nothing
	}

	public void dbgBrkptReached(Sys sys, Assembly asm) {
		// this is done in execPaused
	}
}