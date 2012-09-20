package de.man.mn.gep.client.workspace.searchresult.menu;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.menu.events.ItemClickEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.OpenFromEnovia5;
import de.man.mn.gep.client.shared.event.OpenMillerTree;
import de.man.mn.gep.client.shared.event.OpenSpaceTree;
import de.man.mn.gep.client.shared.event.SearchResult;
import de.man.mn.gep.client.workspace.searchresult.menu.MenuAction.Type;

public abstract class MenuModel extends BaseModel {

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/layout/menus/" + detail.getDataType().name().toLowerCase() + "/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {
		layout = data.isArray();
	}

	@RequestReceiver("ItemClicked") @EventSender(SearchResult.class) void searchResult(
			final Request<ItemClickEvent> request, final Response<JSONValue, SearchResult> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.search == action.getType()) {
			if (action.isValid()) {
				response.event().datatype = action.getDataType();
				final String url = detail.getLink(action.getDataType().name());
				response.event().url = url;
				response.event().alias = action.getTitle() + ": " + detail.getAlias();
				response.event().delimiter = delimiter;
				final String t = action.getTitle().toLowerCase();
				JsonResource.get(url + "&from=0&to=50", response, "Loading " + t + "...", "Could not load " + t + ".",
						true);
			}
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(OpenMillerTree.class) void millerTree(
			final Request<ItemClickEvent> request, final Response<JSONValue, OpenMillerTree> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.millertree == action.getType()) {
			if (action.isValid()) {
				response.event().detailurl = detail.getDetailUrl();
				response.event().displayname = action.getTitle() + ": " + detail.getDisplayName();
				response.event().delimiter = delimiter;
				final String t = action.getTitle().toLowerCase();
				response.event().datasource = new RestletDataSource(detail.getLink(action.getType().name()), response,
						"Loading " + t + "...", "Could not load " + t + ".");
			}
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(OpenSpaceTree.class) void spaceTree(
			final Request<ItemClickEvent> request, final Response<JSONValue, OpenSpaceTree> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.spacetree == action.getType()) {
			if (action.isValid()) {
				response.event().detailurl = detail.getDetailUrl();
				response.event().displayname = action.getTitle() + ": " + detail.getDisplayName();
				response.event().delimiter = delimiter;
				final String t = action.getTitle().toLowerCase();
				JsonResource.get(detail.getLink(action.getType().name()), response, "Loading " + t + "...",
						"Could not load " + t + ".", true);
			}
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(OpenFromEnovia5.class) void enovia5(
			final Request<ItemClickEvent> request, final Response<JSONValue, OpenFromEnovia5> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.enovia5 == action.getType()) {
			if (action.isValid()) {
				response.success();
			}
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(DetailSelected.class) void lockUnlock(
			final Request<ItemClickEvent> request, final Response<JSONValue, DetailSelected> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (action.isValid() && -1 < currentrow) {
			final String lockowner = Type.lock == action.getType() ? Context.get().getChallengeLogin()
					: Type.unlock == action.getType() ? "" : null;
			if (null != lockowner) {
				listgrid.setEditValue(currentrow, "lockowner", lockowner);
				listgrid.saveAllEdits();
			}
		}
	}

	@RequestReceiver("ItemClicked") void edit(final Request<ItemClickEvent> request) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.edit == action.getType() && action.isValid() && -1 < currentrow) {
			listgrid.startEditing(currentrow, 0, false);
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(DetailSelected.class) void delete(
			final Request<ItemClickEvent> request, final Response<JSONValue, DetailSelected> response) {
		final MenuAction action = new MenuAction(request, layout);
		if (Type.delete == action.getType() && action.isValid() && -1 < currentrow) {
			if (action.getDataType().equals(DataType.snapshots)) {
				SC.confirm("Confirm", "Do you really want to delete this snapshot?", new BooleanCallback() {
					@Override public void execute(final Boolean yes) {
						if (yes) {
							listgrid.removeData(listgrid.getRecord(currentrow), new DSCallback() {
								@Override public void execute(final DSResponse response, final Object data,
										final DSRequest request) {
									final DetailSelected event = GWT.create(DetailSelected.class);
									event.fire();
								}
							});
							response.success();
						}
					}
				});
			} else if (action.getDataType().equals(DataType.partnerversions)) {
				SC.confirm("Confirm", "Do you really want to delete this partner part?", new BooleanCallback() {
					@Override public void execute(final Boolean yes) {
						if (yes) {
							listgrid.removeData(listgrid.getRecord(currentrow), new DSCallback() {
								@Override public void execute(final DSResponse response, final Object data,
										final DSRequest request) {
									final DetailSelected event = GWT.create(DetailSelected.class);
									event.fire();
								}
							});
							response.success();
						}
					}
				});
			}
		}
	}

	@RequestReceiver("ItemClicked") @EventSender(SearchResult.class) void create(final Request<ItemClickEvent> request,
			final Response<JSONValue, SearchResult> response) {
		final MenuAction action = new MenuAction(request, layout);

		if (Type.create == action.getType() && action.isValid()) {
			if (action.getDataType().equals(DataType.snapshots)) {
				final Record editingrecord = new Record();
				final Date expirationdate = DateUtil.addDays(DateUtil.now(), 30L * 6L);
				final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd");
				final String today = dateFormatter.format(DateUtil.today());
				editingrecord.setAttribute("name", "Snapshot taken " + today);
				editingrecord.setAttribute("storage", "?");
				editingrecord.setAttribute("iterationstagged", 0);
				editingrecord.setAttribute("lastmodified", DateUtil.now());
				editingrecord.setAttribute("parentoid", detail.getAttribute("id"));
				editingrecord.setAttribute("parenttype", detail.getDataType().name());
				editingrecord.setAttribute("parentname", detail.getDisplayName());
				editingrecord.setAttribute("owner", Context.get().getChallengeLogin());
				editingrecord.setAttribute("expirationdate", expirationdate);
				editingrecord.setAttribute("bom", detail.getLink("bom"));
				editingrecord.setAttribute("whereused", detail.getLink("whereused"));
				editingrecord.setAttribute("formatssummary", detail.getLink("formatssummary"));
				editingrecord.setAttribute("formatsdetails", detail.getLink("formatsdetails"));
				editingrecord.setAttribute("assembly", detail.getLink("assembly"));
				editingrecord.setAttribute("iterations", detail.getLink("iterations"));
				editingrecord.setAttribute("millertree", detail.getLink("millertree"));
				editingrecord.setAttribute("spacetree", detail.getLink("spacetree").replace("/0/", "/1/"));

				final String snapshots = detail.getLink("snapshots");
				final SearchResult event = response.event();
				event.startediting = true;
				event.editingrecord = editingrecord;
				event.url = snapshots;
				event.delimiter = delimiter;
				event.alias = action.getTitle() + ": " + detail.getDisplayName();
				event.datatype = action.getDataType();
				event.recycle = true;
				final String t = action.getTitle().toLowerCase();
				JsonResource.get(snapshots, response, "Loading " + t + "...", "Could not load " + t + ".", true);
			} else if (action.getDataType().equals(DataType.partnerversions)) {
				try {
					final Record editingrecord = new Record();
					editingrecord.setAttribute("versionname", detail.getDisplayName());
					editingrecord.setAttribute("owner", Context.get().getChallengeLogin());
					editingrecord.setAttribute("lastmodified", DateUtil.now());
					editingrecord.setAttribute("versionstring", detail.getAttribute("version"));
					editingrecord.setAttribute("master", detail.getAttribute("masterid"));
					editingrecord.setAttribute("version", detail.getId());
					editingrecord.setAttribute("status", detail.getAttribute("status"));

					if (detail.getAttribute("project") != null) {
						editingrecord.setAttribute("project", detail.getAttribute("project"));
					}

					editingrecord.setAttribute("partner", "0A734403A3D24539BDF74874DB10EB84");
					editingrecord.setAttribute("partnername", "PHEVOS");
					editingrecord.setAttribute("team", "Bus & LKW");

					final String partnerversions = detail.getLink("partnerversions");
					final SearchResult event = response.event();
					event.startediting = true;
					event.editingrecord = editingrecord;
					event.url = partnerversions;
					event.delimiter = delimiter;
					event.alias = action.getTitle() + ": " + detail.getDisplayName();
					event.datatype = action.getDataType();
					event.recycle = true;
					final String t = action.getTitle().toLowerCase();
					JsonResource.get(partnerversions, response, "Loading " + t + "...", "Could not load " + t + ".",
							true);
				} catch (final Exception e) {
					Dialogs.warn(e.toString());
				}
			}
		}
	}

	public void setDetail(final Detail detail) {
		this.detail = detail;
	}

	public void setListGrid(final ListGrid listgrid) {
		this.listgrid = listgrid;
	}

	public void setCurrentRow(final int currentrow) {
		this.currentrow = currentrow;
	}

	public void setDelimiter(final String delimiter) {
		this.delimiter = delimiter;
	}

	public JSONArray getLayout() {
		return layout;
	}

	public Detail getDetail() {
		return detail;
	}

	private JSONArray layout;
	private Detail detail;
	private ListGrid listgrid;
	private int currentrow = -1;
	private String delimiter;

}
