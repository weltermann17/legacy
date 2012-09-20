package de.man.mn.gep.client.workspace.searchresult;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasSelectionChangedHandlers;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

public abstract class SearchResultPresenter extends BasePresenter<SearchResultModel, SearchResultView> {

	public interface View {

		@RequestSender("SelectionChanged") HasSelectionChangedHandlers selectionChanged();

		@RequestSender("CellClicked") HasCellClickHandlers cellClicked();

		@RequestSender("ExportableSelected") HasTabSelectedHandlers exportableSelected();

		boolean startEditing(final boolean startediting, final Record editingrecord);

	}

	@Override protected void onInit() {
		model().setListGrid(view().setData(model().getUrl(), model().getDataSource(), model().getLayout()));
	}

	@Override protected void onUnbind() {
		model().destroyMenu();
	}

}
