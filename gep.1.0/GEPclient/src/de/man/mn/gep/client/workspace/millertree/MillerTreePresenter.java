package de.man.mn.gep.client.workspace.millertree;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.events.HasMouseOutHandlers;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasCellOutHandlers;
import com.smartgwt.client.widgets.grid.events.HasCellOverHandlers;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

public abstract class MillerTreePresenter extends BasePresenter<MillerTreeModel, MillerTreeView> {

	public interface View {

		@RequestSender("DetailSelected") HasCellClickHandlers[] cellClick();

		@RequestSender("DetailOver") HasCellOverHandlers[] cellOver();

		@RequestSender("DetailOut") HasCellOutHandlers[] cellOut();

		@RequestSender("DetailOut") HasMouseOutHandlers[] mouseOut();

		@RequestSender("ExportableSelected") HasTabSelectedHandlers exportableSelected();
	}

	@Override protected void onBound() {
		view().scrollToCenter();
	}
}
