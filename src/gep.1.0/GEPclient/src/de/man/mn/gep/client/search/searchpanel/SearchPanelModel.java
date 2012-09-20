package de.man.mn.gep.client.search.searchpanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.ResponseMultiplexer;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventMultiplexer;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Location;
import de.man.mn.gep.client.shared.event.ChangeSearchType;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.LocationsChanged;
import de.man.mn.gep.client.shared.event.RecycleTabs;
import de.man.mn.gep.client.shared.event.SearchResult;
import de.man.mn.gep.client.shared.event.SignedOff;
import de.man.mn.gep.client.shared.event.SignedOn;

public abstract class SearchPanelModel extends BaseModel {

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/model/search/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {

		try {
			final JSONArray criteria = data.isArray();
			this.criteria.clear();

			for (int i = 0; i < criteria.size(); i++) {
				final JSONObject searchTypeJSON = criteria.get(i).isObject();
				final Map<String, Criterion> searchType = new LinkedHashMap<String, Criterion>();

				for (int j = 0; j < searchTypeJSON.get("elements").isArray().size(); j++) {
					final Criterion criterion = Criterion.readMe(j, searchTypeJSON);
					if (j == 0
							&& (selectedCriterion == null && i == 0 || selectedCriterion != null
									&& criterion.displaytype.equalsIgnoreCase(selectedCriterion.displaytype))) {
						selectedCriterion = criterion;
						selectedCriterionHistory.put(selectedCriterion.displaytype, selectedCriterion);
					}
					searchType.put(criterion.getName(), criterion);
				}
				this.criteria.put(searchTypeJSON.get("displaytype").isString().stringValue().trim(), searchType);
			}
		} catch (final Exception e) {
			getLogger().severe(getClass().getName().replace("Impl", "") + ".onInit() : " + e);
		}
	}

	public void setDataType(final DataType datatype) {
		this.datatype = datatype;
	}

	public DataType getDataType() {
		return datatype;
	}

	String[] getCriteria() {
		final ArrayList<String> criteria = new ArrayList<String>();
		final Iterator<Criterion> iter = this.criteria.get(selectedCriterion.displaytype).values().iterator();

		while (iter.hasNext()) {
			final Criterion crit = iter.next();
			criteria.add(crit.name);
		}
		final String[] temp = new String[criteria.size()];
		for (int i = 0; i < criteria.size(); i++) {
			temp[i] = criteria.get(i);
		}
		return temp;
	}

	LinkedHashMap<String, String> getSearchType() {
		final Iterator<String> iter = criteria.keySet().iterator();

		final LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		while (iter.hasNext()) {
			final String temp = iter.next();
			output.put(temp, "\t" + temp);
		}
		return output;
	}

	Criterion getSelectedCriterion() {
		return selectedCriterion;
	}

	Map<String, String> getSearchTypeIcons() {
		final Map<String, String> searchTypeIcon = new LinkedHashMap<String, String>();
		final Iterator<Map<String, Criterion>> criteriaIter = criteria.values().iterator();
		while (criteriaIter.hasNext()) {
			final Criterion criterion = criteriaIter.next().values().iterator().next();
			searchTypeIcon.put(criterion.displaytype, criterion.icon);
		}
		return searchTypeIcon;
	}

	@RequestReceiver("CriterionChanged") @EventSender(CriterionChanged.class) void criterionChanged(
			final Request<ChangedEvent> request, final Response<Criterion, CriterionChanged> response) {
		final String selectedCriterion = request.gesture().getValue().toString();
		this.selectedCriterion = criteria.get(this.selectedCriterion.displaytype).get(selectedCriterion);
		selectedCriterionHistory.put(this.selectedCriterion.displaytype, this.selectedCriterion);
		response.onSuccess(getSelectedCriterion());
	}

	@EventReceiver protected void changeSearchType(final ChangeSearchType event) {
		changeSearchType(event.type);
		final CriterionChanged changeEvent = GWT.create(CriterionChanged.class);
		changeEvent.selectedCriterion = selectedCriterion;
		changeEvent.fire();
	}

	@RequestReceiver("SearchTypeChanged") @EventSender(CriterionChanged.class) void searchtypeChanged(
			final Request<ChangedEvent> request, final Response<Criterion, CriterionChanged> response) {
		changeSearchType(request.gesture().getValue().toString());
		response.event().selectedCriterion = selectedCriterion;
		response.success();
	}

	private void changeSearchType(final String seletectdisplaytype) {
		if (selectedCriterionHistory.containsKey(seletectdisplaytype)) {
			selectedCriterion = selectedCriterionHistory.get(seletectdisplaytype);
		} else {
			selectedCriterion = criteria.get(seletectdisplaytype).values().iterator().next();
			selectedCriterionHistory.put(selectedCriterion.displaytype, selectedCriterion);
		}
		datatype = DataType.valueOf(selectedCriterion.type);
		((SearchPanelView) getPresenter().view()).updateCriterion(getCriteria(), getSearchType(), getSearchTypeIcons(),
				selectedCriterion);
	}

	@RequestReceiver("InputChanged") @EventSender(CriterionChanged.class) void inputChanged(final int index,
			final Request<ChangedEvent> request, final Response<Criterion, CriterionChanged> response) {
		final String formitem = request.gesture().getItem().getName();
		if ("combobox".equals(formitem) || "picker".equals(formitem)) {
			getSelectedCriterion().setText(request.gesture().getValue().toString());
		} else if ("from".equals(formitem)) {
			setDateValue(0, request.gesture().getValue());
		} else if ("to".equals(formitem)) {
			setDateValue(1, request.gesture().getValue());
		}
		response.onSuccess(getSelectedCriterion());
	}

	private void setDateValue(final int i, final Object date) {
		final CriterionValue value = getSelectedCriterion().getValue();
		final Date from = 0 == i ? (Date) date : null != value ? value.from : null;
		final Date to = 1 == i ? (Date) date : null != value ? value.to : null;
		getSelectedCriterion().setFromTo(from, to);
	}

	@RequestReceiver("Back") @EventSender(CriterionChanged.class) void back(
			final Request<FormItemIconClickEvent> request, final Response<Criterion, CriterionChanged> response) {
		final SearchValue search = searchhistory.get(selectedCriterion.displaytype).getPreviousValue();
		selectedCriterion = criteria.get(search.value.type).get(search.criterion);
		selectedCriterionHistory.put(selectedCriterion.displaytype, selectedCriterion);
		getSelectedCriterion().setValue(search.value);
		response.onSuccess(getSelectedCriterion());
	}

	@RequestReceiver("Back") @EventMultiplexer(SearchResult.class) void backSearch(
			final Request<FormItemIconClickEvent> request,
			final ResponseMultiplexer<JSONValue, SearchResult> responsemultiplexer) {
		executeSearch(null, responsemultiplexer);
	}

	@RequestReceiver("Next") @EventSender(CriterionChanged.class) void next(
			final Request<FormItemIconClickEvent> request, final Response<Criterion, CriterionChanged> response) {
		final SearchValue search = searchhistory.get(selectedCriterion.displaytype).getNextValue();
		selectedCriterion = criteria.get(search.value.type).get(search.criterion);
		selectedCriterionHistory.put(selectedCriterion.displaytype, selectedCriterion);
		getSelectedCriterion().setValue(search.value);
		response.onSuccess(getSelectedCriterion());
	}

	@RequestReceiver("Next") @EventMultiplexer(SearchResult.class) void nextSearch(
			final Request<FormItemIconClickEvent> request,
			final ResponseMultiplexer<JSONValue, SearchResult> responsemultiplexer) {
		executeSearch(null, responsemultiplexer);
	}

	@RequestReceiver("Hint") void hint(final Request<FormItemIconClickEvent> request) {
		Dialogs.info(getSelectedCriterion().getHint());
	}

	@RequestReceiver("Search") @EventSender(CriterionChanged.class) void search(final Request<ClickEvent> request,
			final Response<Criterion, CriterionChanged> response) {
		if (cansearch) {
			if (!"Recently accessed".equals(getSelectedCriterion().getName())) {
				getSelectedCriterion().getHistory().commitValue();
				searchhistory.get(selectedCriterion.displaytype).commitValue(
						SearchValue.create(selectedCriterion.getName(), getSelectedCriterion().getValue()));
				store(toJson());
			}
			response.onSuccess(getSelectedCriterion());
		}
	}

	@RequestReceiver("Search") @EventMultiplexer(SearchResult.class) void executeSearch(
			final Request<ClickEvent> request, final ResponseMultiplexer<JSONValue, SearchResult> responsemultiplexer) {
		if (cansearch) {
			if ("Recently accessed".equals(getSelectedCriterion().getName())) {
				final Response<JSONValue, SearchResult> response = responsemultiplexer.multiplex();
				final SearchResult event = response.event();
				event.url = null;
				event.alias = getSelectedCriterion().getAlias();
				event.recycle = recycle;
				event.delimiter = null;
				event.datasource = getRecentlyAccessed();
				event.datatype = getSelectedCriterion().getDataType();
				final String data = "{\"response\":{\"data\":[],\"startRow\":0,\"endRow\":0,\"totalRows\":0,\"status\":0}}";
				response.onSuccess(JSONParser.parseStrict(data));
			} else {
				if (0 == locations.selection.length) {
					Dialogs.error("You need to select at least one location to search in.");
				} else {
					for (final Location location : locations.selectedlocations) {
						final Response<JSONValue, SearchResult> response = responsemultiplexer.multiplex();
						final SearchResult event = response.event();
						event.url = location.url + getSelectedCriterion().getType() + "/?" + getQuery();
						event.alias = location.shortname + ": " + getAlias();
						event.delimiter = ":";
						event.recycle = recycle;
						event.datatype = DataType.valueOf(getSelectedCriterion().getType());
						final String queryurl = event.url + "&from=0&to=50";
						JsonResource.get(queryurl, response, "Searching...", "No result: " + queryurl, true);
					}
				}
			}
		}
	}

	@EventReceiver void locationsChanged(final LocationsChanged event) {
		locations = event;
	}

	@EventReceiver void signedOn(final SignedOn event) {
		if (event.isSuccess()) {
			cansearch = true;
			try {
				for (final String key : criteria.keySet()) {
					if (!searchhistory.containsKey(key)) {
						final SearchHistory history = new SearchHistory();
						history.setLimit(SearchPanelModel.SEARCH_HISTORY_LIMIT);
						searchhistory.put(key, history);
					}
					if (!accesshistory.containsKey(key)) {
						final AccessHistory history = new AccessHistory();
						history.setLimit(SearchPanelModel.ACCESS_HISTORY_LIMIT);
						accesshistory.put(key, history);
					}
				}
				fromJson(restore());
				final ChangeSearchType searchTypeChanged = GWT.create(ChangeSearchType.class);
				searchTypeChanged.type = getSelectedCriterion().displaytype;
				searchTypeChanged.fire();
				/**
				 * ugly
				 */
				((SearchPanelView) getPresenter().view()).criterionChanged(getSelectedCriterion().getName());

			} catch (final Exception e) {
				getLogger().severe("SearchModel.onInit() : restore failed");
				reset();
			}
		}
	}

	@EventReceiver void signedOff(final SignedOff event) {
		if (event.withresethistory) {
			reset();
		}
	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		if (event.isSuccess() && datatype.equals(event.detail.getDataType())) {
			accesshistory.get(selectedCriterion.displaytype).commitValue(new AccessString(event.detail.toString()));
			store(toJson());
		}
	}

	@EventReceiver void recycleTabChanged(final RecycleTabs event) {
		recycle = event.recycle;
	}

	private String getQuery() {
		String template = getSelectedCriterion().getTemplate();
		if ("combobox".equals(getSelectedCriterion().getUi())) {
			String value = getSelectedCriterion().getValue().text;
			final String name = getSelectedCriterion().getName();
			if ("Partnumber".equals(name)) {
				if (!value.startsWith("*")) {
					if (11 == value.length() && !value.contains("*")) {
						value = delimiter(value, 2);
						value = delimiter(value, 8);
					} else if (2 < value.length()) {
						value = delimiter(value, 2);
					} else if (0 == value.length()) {
						value = "*";
					}
					if (13 > value.length() && !value.contains("*")) {
						value = value + "*";
					}
				}
			}
			template = template.replace("%%1", value);
		} else if ("picker".equals(getSelectedCriterion().getUi())) {
			template = template.replace("%%1", getSelectedCriterion().getPickerValue());
		} else if ("fromto".equals(getSelectedCriterion().getUi())) {
			template = template.replace("%%1", convertDate(getSelectedCriterion().getValue().from));
			template = template.replace("%%2", convertDate(getSelectedCriterion().getValue().to));
		}
		return template.replace("#", "%23").replace("+", "%2b").replace(",", "%2c");
	}

	private String convertDate(final Date date) {
		String result = "";
		if (null != date) {
			final double delta = date.getTime() - DateUtil.today().getTime();
			final long deltaindays = Math.round(delta / SearchPanelModel.ONEDAY);
			if (0 == deltaindays) {
				result = "today";
			} else if (0 < deltaindays) {
				result = "today+" + deltaindays;
			} else {
				result = "today" + deltaindays;
			}
		}
		return result;
	}

	private String getAlias() {
		if ("combobox".equals(getSelectedCriterion().getUi())) {
			return getSelectedCriterion().getAlias() + " " + getSelectedCriterion().getValue().text;
		} else if ("picker".equals(getSelectedCriterion().getUi())) {
			return getSelectedCriterion().getValue().text.toUpperCase();
		} else if ("fromto".equals(getSelectedCriterion().getUi())) {
			return SearchPanelModel.ISO8601.format(getSelectedCriterion().getValue().from) + " \u21e8 "
					+ SearchPanelModel.ISO8601.format(getSelectedCriterion().getValue().to);
		} else {
			return null;
		}
	}

	private String delimiter(final String in, final int pos) {
		final String c = in.substring(pos, pos + 1);
		final String replacement = 2 == pos ? "." : "-";
		if (!(".".equals(c) || "_".equals(c) || "-".equals(c) || "".equals(c))) {
			return in.substring(0, pos) + replacement + in.substring(pos);
		} else if (".".equals(c) || "_".equals(c) || "-".equals(c)) {
			return in.substring(0, pos) + replacement + in.substring(pos + 1);
		} else {
			return in;
		}
	}

	private DataSource getRecentlyAccessed() {
		final DataSource datasource = new DataSource();
		datasource.setClientOnly(true);
		datasource.setCacheAllData(true);
		datasource.setAutoCacheAllData(true);
		int j = accesshistory.get(selectedCriterion.displaytype).getStack().size();
		final Record[] records = new Record[j];
		for (final AccessString access : accesshistory.get(selectedCriterion.displaytype).getStack()) {
			final Record record = new Record(JSONParser.parseStrict(access.asString()).isObject().getJavaScriptObject());
			record.setAttribute("row", j);
			record.setAttribute("lastmodified", record.getAttribute("lastmodified").substring(0, 10));
			records[--j] = record;
		}
		datasource.setTestData(records);
		return datasource;
	}

	private JSONValue toJson() {
		final JSONObject data = null == restoreddata ? new JSONObject() : restoreddata;
		try {
			data.put("historyversion", new JSONString(SearchPanelModel.historyversion));
			data.put("selectedtype", new JSONString(selectedCriterion.displaytype));
			final Iterator<String> iter = criteria.keySet().iterator();
			while (iter.hasNext()) {
				final String searchTypeKey = iter.next();
				if (!selectedCriterionHistory.containsKey(searchTypeKey)) {
					continue;
				}
				final JSONObject searchdata = new JSONObject();
				final JSONObject userdata = new JSONObject();
				searchdata.put(Context.get().getChallengeLogin().toLowerCase(), userdata);
				userdata.put("selectedcriterion", new JSONString(selectedCriterionHistory.get(searchTypeKey).getName()));
				final JSONObject criteriadata = new JSONObject();
				for (final Criterion criterion : criteria.get(searchTypeKey).values()) {
					final String s = CriterionHistory.Writer.toJson(criterion.getHistory());
					criteriadata.put(criterion.getName(), JSONParser.parseStrict(s));
				}
				userdata.put("criteria", criteriadata);
				if (searchhistory.containsKey(searchTypeKey)) {
					userdata.put("searchhistory",
							JSONParser.parseStrict(SearchHistory.Writer.toJson(searchhistory.get(searchTypeKey))));
				}
				if (accesshistory.containsKey(searchTypeKey)) {
					userdata.put("accesshistory",
							JSONParser.parseStrict(AccessHistory.Writer.toJson(accesshistory.get(searchTypeKey))));
				}
				data.put(selectedCriterionHistory.get(searchTypeKey).getType().toLowerCase(), searchdata);
			}
		} catch (final Exception e) {
			getLogger().severe("SearchModel.toJson() : " + e);
		}
		return data;
	}

	private void fromJson(final JSONValue data) {
		try {
			if (null != data) {
				restoreddata = data.isObject();
				final String version = restoreddata.get("historyversion").isString().stringValue();
				if (!version.equals(SearchPanelModel.historyversion)) {
					throw new Exception("versionstring of search history has changed, need to reset history");
				}
				final String selectedtype = restoreddata.get("selectedtype").isString().stringValue();
				final Iterator<String> iter = criteria.keySet().iterator();
				while (iter.hasNext()) {
					final String searchTypeDisplayKey = iter.next();
					if (!restoreddata.containsKey(criteria.get(searchTypeDisplayKey).values().iterator().next().type)) {
						searchhistory.put(searchTypeDisplayKey, new SearchHistory());
						searchhistory.get(searchTypeDisplayKey).setLimit(SearchPanelModel.SEARCH_HISTORY_LIMIT);
						accesshistory.put(searchTypeDisplayKey, new AccessHistory());
						accesshistory.get(searchTypeDisplayKey).setLimit(SearchPanelModel.ACCESS_HISTORY_LIMIT);
						continue;
					}
					final String type = criteria.get(searchTypeDisplayKey).values().iterator().next().type;
					final JSONObject searchdata = restoreddata.get(type.toLowerCase()).isObject();
					final JSONObject userdata = searchdata.get(Context.get().getChallengeLogin().toLowerCase())
							.isObject();
					final Criterion criterion = criteria.get(searchTypeDisplayKey).get(
							userdata.get("selectedcriterion").isString().stringValue());
					if (criterion.displaytype.equalsIgnoreCase(selectedtype)) {
						selectedCriterion = criterion;
					}
					selectedCriterionHistory.put(criterion.displaytype, criterion);
					final JSONObject criteriadata = userdata.get("criteria").isObject();
					for (final String key : criteriadata.keySet()) {
						criteria.get(criterion.displaytype).get(key).setHistory(criteriadata.get(key).isObject());
					}

					if (userdata.containsKey("searchhistory")) {
						searchhistory.put(searchTypeDisplayKey,
								SearchHistory.Reader.read(userdata.get("searchhistory").isObject()));
					} else {
						searchhistory.put(searchTypeDisplayKey, new SearchHistory());
						searchhistory.get(searchTypeDisplayKey).setLimit(SearchPanelModel.SEARCH_HISTORY_LIMIT);
					}
					if (userdata.containsKey("accesshistory")) {
						accesshistory.put(searchTypeDisplayKey,
								AccessHistory.Reader.read(userdata.get("accesshistory").isObject()));
					} else {
						accesshistory.put(searchTypeDisplayKey, new AccessHistory());
						accesshistory.get(searchTypeDisplayKey).setLimit(SearchPanelModel.ACCESS_HISTORY_LIMIT);
					}
				}
			}
		} catch (final Exception e) {
			getLogger().severe("SearchModel.fromJson() : " + e);
			reset();
		}
	}

	private DataType datatype;
	private boolean cansearch = false;
	private Criterion selectedCriterion;
	private final Map<String, Criterion> selectedCriterionHistory = new LinkedHashMap<String, Criterion>();
	private LocationsChanged locations;
	private final Map<String, Map<String, Criterion>> criteria = new LinkedHashMap<String, Map<String, Criterion>>();
	private final Map<String, SearchHistory> searchhistory = new LinkedHashMap<String, SearchHistory>();
	private final Map<String, AccessHistory> accesshistory = new LinkedHashMap<String, AccessHistory>();
	private JSONObject restoreddata;
	private static final DateTimeFormat ISO8601 = DateTimeFormat.getFormat("yyyy-MM-dd");
	private static final double ONEDAY = 24 * 60 * 60 * 1000;
	private static final String historyversion = "6";
	private boolean recycle = true;

	private static final int SEARCH_HISTORY_LIMIT = 20;
	private static final int ACCESS_HISTORY_LIMIT = 100;
}
