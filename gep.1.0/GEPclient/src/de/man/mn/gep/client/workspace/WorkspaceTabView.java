package de.man.mn.gep.client.workspace;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.smartgwt.client.widgets.tab.Tab;

import de.man.mn.gep.client.shared.event.DetailSelected;

public abstract class WorkspaceTabView extends CanvasView {

	protected WorkspaceTabView() {
		tab = new Tab();
		tab.setCanClose(true);
		asCanvas().setSize("100%", "100%");
		tab.setPane(asCanvas());
	}

	public Tab asTab() {
		return tab;
	}

	public <D> void setDetailsSelected(final BusEvent<D> event, final boolean force) {
		if (force || tab.getTabSet().getSelectedTab().equals(tab)) {
			tab.setAttribute("detailsselected", event);
		}
	}

	@EventReceiver protected void detailSelected(final DetailSelected event) {
		setDetailsSelected(event, false);
	}

	private final Tab tab;

}
