package de.man.mn.gep.client.workspace.searchresult;

import java.util.HashMap;

import com.google.gwt.json.client.JSONArray;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.FileDownload;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.ui.smartgwt.RestDataSourceGrid;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.events.ChangeEvent;
import com.smartgwt.client.widgets.grid.events.ChangeHandler;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasSelectionChangedHandlers;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

import de.man.mn.gep.client.shared.event.SaveFile;
import de.man.mn.gep.client.workspace.WorkspaceTabView;

public abstract class SearchResultView extends WorkspaceTabView implements SearchResultPresenter.View {

	@Override public HasSelectionChangedHandlers selectionChanged() {
		return grid;
	}

	@Override public HasCellClickHandlers cellClicked() {
		return grid;
	}

	@Override public HasTabSelectedHandlers exportableSelected() {
		return asTab();
	}

	@Override public boolean startEditing(final boolean startediting, final Record editingrecord) {
		try {
			if (null != grid) {
				if (startediting) {
					Dialogs.warn("startEditing - " + editingrecord.getAttribute("lastmodified"));
					grid.startEditingNew(editingrecord);
				}
				return false;
			} else {
				return true;
			}
		} catch (final Exception e) {
			Dialogs.warn(e.toString());
			return true;
		}
	}

	RestDataSourceGrid setData(final String url, final DataSource datasource, final JSONArray layout) {
		grid = new RestDataSourceGrid(url, datasource, layout);

		if (url.lastIndexOf("/partnerversions/") != -1) {
			grid.getField("partnername").addChangeHandler(new ChangeHandler() {
				@Override public void onChange(final ChangeEvent event) {

					final HashMap<String, String> partnerMap = new HashMap<String, String>(3);
					partnerMap.put("PHEVOS", "0A734403A3D24539BDF74874DB10EB84");
					partnerMap.put("RMMV", "E137A1C70221436FB881EE2773787EE2");
					partnerMap.put("MAN", "0BB16B0932DE48A6AF048288C10D8153");

					grid.setEditValue(event.getRowNum(), "partner", partnerMap.get(event.getValue()));
				}
			});
		}

		asCanvas().addChild(grid);
		return grid;
	}

	@EventReceiver void saveAs(final SaveFile event) {
		if (asTab().getTabSet().getSelectedTab().equals(asTab())) {
			FileDownload.get(grid.getUrl() + "&from=0&to=2147483647", event.mediatype);
		}
	}

	protected RestDataSourceGrid grid;

}
