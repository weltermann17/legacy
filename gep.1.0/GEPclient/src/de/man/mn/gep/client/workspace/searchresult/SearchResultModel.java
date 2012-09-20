package de.man.mn.gep.client.workspace.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;

import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.RemoteSearch;
import de.man.mn.gep.client.shared.event.SearchResult;
import de.man.mn.gep.client.shared.event.UserSelected;
import de.man.mn.gep.client.workspace.WorkspaceTabModel;
import de.man.mn.gep.client.workspace.searchresult.menu.MenuModel;
import de.man.mn.gep.client.workspace.searchresult.menu.MenuPresenter;
import de.man.mn.gep.client.workspace.searchresult.menu.MenuView;

public abstract class SearchResultModel extends WorkspaceTabModel {

	@Override protected void onInit(final BusEvent<?> e) {
		event = (SearchResult) e;
		delimiter = event.delimiter;
		url = event.url;
		datasource = event.datasource;
		datatype = event.datatype;
		startediting = event.startediting;
		editingrecord = event.editingrecord;
	}

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/layout/searchresult/" + datatype.name().toLowerCase() + "/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {
		layout = data.isArray();
		super.onInit(data);
	}

	@Override public boolean isExportable() {
		return true;
	}

	@RequestReceiver("SelectionChanged") void selectionChanged(final Request<SelectionEvent> request) {
		if (request.gesture().getState()) {
			destroyMenu();
			final MenuModel model = GWT.create(MenuModel.class);
			contextmenu = GWT.create(MenuView.class);
			final MenuPresenter presenter = GWT.create(MenuPresenter.class);
			final Record record = request.gesture().getRecord();
			model.setListGrid(listgrid);
			model.setCurrentRow(listgrid.getRecordIndex(record));
			model.setDetail(Detail.create(record));
			model.setDelimiter(delimiter);
			listgrid.setContextMenu(MVP.create(model, contextmenu, presenter).asMenu());
		}
	}

	@RequestReceiver("SelectionChanged") @EventSender(DetailSelected.class) void detailSelected(
			final Request<SelectionEvent> request, final Response<JSONValue, DetailSelected> response) {
		final SelectionEvent gesture = request.gesture();

		if (gesture.getState()) {
			final Record record = gesture.getRecord();
			final DetailSelected event = response.event();
			event.rowdetail = Detail.create(record);
			event.detail = event.rowdetail;
			event.delimiter = delimiter;
			event.currentrow = listgrid.getRecordIndex(record);
			event.listgrid = listgrid;
			event.datasource = new RestletDataSource(event.detail.getDetailUrl(), response, "Loading details...",
					"No details found. Please try again in a moment.");
		}
	}

	@RequestReceiver("CellClicked") @EventSender(UserSelected.class) void userSelected(
			final Request<CellClickEvent> request, final Response<JSONValue, UserSelected> response) {
		final CellClickEvent gesture = request.gesture();
		final Detail detail = Detail.create(gesture.getRecord());
		final ListGridField field = listgrid.getField(gesture.getColNum());
		if (field.getAttributeAsBoolean("islink") && "user".equalsIgnoreCase(field.getAttribute("linktype"))) {
			final String fieldname = field.getName();
			final String user = detail.getAttribute(fieldname);
			if (null != user) {
				response.event().user = user;
				response.event().datasource = new RestletDataSource(detail.getLink(fieldname), response, "", user
						+ " not found.");
			}
		}
	}

	@RequestReceiver("CellClicked") @EventSender(RemoteSearch.class) void remoteSearch(
			final Request<CellClickEvent> request, final Response<Object, RemoteSearch> response) {
		final CellClickEvent gesture = request.gesture();
		gesture.getColNum();
		final Detail detail = Detail.create(gesture.getRecord());
		final ListGridField field = listgrid.getField(gesture.getColNum());
		if (field.getAttributeAsBoolean("islink") && "remotesearch".equalsIgnoreCase(field.getAttribute("linktype"))) {
			final String value = detail.getAttribute(field.getName());
			if (null != value) {
				final Object[] values = new Object[1];
				values[0] = value;
				response.event().datatype = datatype;
				response.event().criterion = field.getTitle();
				response.event().values = values;
				response.success();
			}
		}
	}

	public JSONArray getLayout() {
		return layout;
	}

	public String getUrl() {
		return url;
	}

	public DataSource getDataSource() {
		return datasource;
	}

	public boolean getStartEditing() {
		return startediting;
	}

	public Record getEditingRecord() {
		return editingrecord;
	}

	void setListGrid(final ListGrid listgrid) {
		this.listgrid = listgrid;
	}

	void destroyMenu() {
		if (null != contextmenu) {
			contextmenu.asMenu().destroy();
		}
	}

	private DataType datatype;
	protected String delimiter;
	private String url;
	private DataSource datasource;
	private JSONArray layout;
	private SearchResult event;
	private MenuView contextmenu;
	private ListGrid listgrid;
	private boolean startediting;
	private Record editingrecord;

}
