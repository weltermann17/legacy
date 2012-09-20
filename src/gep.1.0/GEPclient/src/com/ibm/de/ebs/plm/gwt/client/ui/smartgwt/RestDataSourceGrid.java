package com.ibm.de.ebs.plm.gwt.client.ui.smartgwt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restlet.client.data.Status;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.ResultSet;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.GroupStartOpen;
import com.smartgwt.client.types.RPCTransport;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.JSOHelper;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.validator.DateRangeValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.EditorEnterEvent;
import com.smartgwt.client.widgets.grid.events.EditorEnterHandler;
import com.smartgwt.client.widgets.grid.events.EditorExitEvent;
import com.smartgwt.client.widgets.grid.events.EditorExitHandler;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class RestDataSourceGrid extends ListGrid {

	public RestDataSourceGrid(final String url, final DataSource datasource, final JSONArray layout) {
		this.url = url;
		this.layout = layout;
		setWidth100();
		setHeight100();
		setLeaveScrollbarGap(true);
		setAutoFetchData(false);
		setPreventDuplicates(true);
		setDataPageSize(50);
		setUseAllDataSourceFields(true);
		setAlternateRecordStyles(true);
		setGroupStartOpen(GroupStartOpen.NONE);
		setCanFreezeFields(false);
		setShowRollOver(true);
		setSelectionType(SelectionStyle.SINGLE);
		setCanHover(true);
		setShowHover(false);
		setHoverWrap(false);
		setHoverWidth(320);
		setCanSelectText(true);
		setShowEmptyMessage(true);
		setLoadingDataMessage("Loading...");
		setEmptyMessage("<br>Nothing found.</br>");

		setCanEdit(false);
		setWaitForSave(true);
		addEditorEnterHandler(new EditorEnterHandler() {
			@Override public void onEditorEnter(final EditorEnterEvent event) {
				setShowHover(true);
				setPrompt("Press 'Enter' to save your modifications or 'Esc' to cancel the edit.");
			}
		});
		addEditorExitHandler(new EditorExitHandler() {
			@Override public void onEditorExit(final EditorExitEvent event) {
				setShowHover(false);
				setPrompt("");
			}
		});
		addEditCompleteHandler(new EditCompleteHandler() {
			@Override public void onEditComplete(final EditCompleteEvent event) {
				selectSingleRecord(event.getRowNum());
			}
		});

		final List<DataSourceField> fields = new LinkedList<DataSourceField>();
		boolean onlyonce = true;
		for (int i = 0; i < layout.size(); ++i) {
			final JSONObject fieldlayout = layout.get(i).isObject();
			String name = null;
			String title = null;
			String type = "TEXT";
			boolean primarykey = false;
			int length = 0;
			if (fieldlayout.containsKey("name")) {
				name = fieldlayout.get("name").isString().stringValue();
			}
			if (fieldlayout.containsKey("title")) {
				title = fieldlayout.get("title").isString().stringValue();
			}
			if (fieldlayout.containsKey("type")) {
				type = fieldlayout.get("type").isString().stringValue();
				if ("text".equalsIgnoreCase(type)) {
					type = "TEXT";
				} else if ("description".equalsIgnoreCase(type)) {
					type = "TEXT";
				}
			}
			if (fieldlayout.containsKey("islink")) {
				final boolean islink = fieldlayout.get("islink").isBoolean().booleanValue();
				if (islink) {
					type = "LINK";
				}
			}
			if (fieldlayout.containsKey("primarykey")) {
				primarykey = fieldlayout.get("primarykey").isBoolean().booleanValue();
			}
			if (fieldlayout.containsKey("length")) {
				length = (int) fieldlayout.get("length").isNumber().doubleValue();
			}
			final FieldType fieldtype = FieldType.valueOf(type.toUpperCase());

			final DataSourceField field = new DataSourceField(name, fieldtype, null != title ? title : name);

			if (primarykey) {
				field.setPrimaryKey(true);
			}
			if (0 < length && FieldType.TEXT == fieldtype) {
				field.setLength(length);
			}
			if (fieldlayout.containsKey("hidden")) {
				if (fieldlayout.get("hidden").isBoolean().booleanValue()) {
					field.setHidden(true);
				}
			}
			field.setCanEdit(false);
			if (fieldlayout.containsKey("canedit")) {
				if (fieldlayout.get("canedit").isBoolean().booleanValue()) {
					field.setCanEdit(true);
					if (onlyonce) {
						setCanEdit(true);
						setRecordEditProperty("editable");
						firsteditablecolumn = i + 1;
						onlyonce = false;
					}
				}
			}
			if (fieldlayout.containsKey("required")) {
				if (fieldlayout.get("required").isBoolean().booleanValue()) {
					field.setRequired(true);
				}
			}
			if (field.getType().equals(FieldType.DATE)) {
				int hasvalidator = 0;
				long minrange = 0;
				long maxrange = 0;
				String message = "Invalid date.";
				if (fieldlayout.containsKey("minimum")) {
					minrange = (long) fieldlayout.get("minimum").isNumber().doubleValue();
					hasvalidator++;
				}
				if (fieldlayout.containsKey("maximum")) {
					maxrange = (long) fieldlayout.get("maximum").isNumber().doubleValue();
					hasvalidator++;
				}
				if (fieldlayout.containsKey("message")) {
					message = fieldlayout.get("message").isString().stringValue();
					hasvalidator++;
				}
				if (1 < hasvalidator) {
					final DateRangeValidator validator = new DateRangeValidator();
					validator.setMin(DateUtil.addDays(DateUtil.now(), minrange));
					validator.setMax(DateUtil.addDays(DateUtil.now(), maxrange));
					validator.setErrorMessage(message);
					field.setValidators(validator);
				}
			} else if (field.getType().equals(FieldType.ENUM)) {
				if (field.getName().equalsIgnoreCase("partnername")) {
					if (url.lastIndexOf("/partnerversions/") != -1) {
						final HashMap<String, String> partnerMap = new HashMap<String, String>();
						partnerMap.put("PHEVOS", "PHEVOS");
						partnerMap.put("RMMV", "RMMV");
						partnerMap.put("MAN", "MAN");

						field.setValueMap(partnerMap);
					}
				}
			}
			field.setCanSortClientOnly(true);
			fields.add(field);
		}
		if (getCanEdit()) {
			setShowRowNumbers(true);
			final ListGridField counter = new ListGridField("counter", "#", 40);
			counter.setTitle("#");
			setRowNumberFieldProperties(counter);
			setRowNumberStyle("cell");
		}

		serverdatasource = new RestDataSource() {

			@SuppressWarnings({ "rawtypes", "unchecked" }) @Override protected Object transformRequest(
					final DSRequest request) {
				try {
					request.setUseSimpleHttp(true);
					request.setWillHandleError(true);
					Map headers = request.getHttpHeaders();
					if (null == headers) {
						headers = new LinkedHashMap();
					}
					headers.put(BaseContext.get().getChallengeCustomHeader(), BaseContext.get().getBasicAuthorization());
					if (DateUtil.now().getTime() <= RestDataSourceGrid.nocacheuntil) {
						headers.put("Cache-Control", "no-cache, no-store, max-age=0");
					}
					request.setHttpHeaders(headers);
					final DSOperationType operationtype = request.getOperationType();
					if (DSOperationType.FETCH.equals(operationtype)) {
						if (0 == total || 51 == total || RestDataSourceGrid.MAXSHOWALLRECORDS < total) {
							show("Loading...");
						}
						request.setHttpMethod("GET");
						final JavaScriptObject range = new JSONObject().getJavaScriptObject();
						JSOHelper.setAttribute(range, "from", floor(request.getStartRow()));
						JSOHelper.setAttribute(range, "to", ceil(request.getEndRow()));
						return range;
					} else if (DSOperationType.ADD.equals(operationtype)) {
						final JSONObject newvalues = new JSONObject(request.getData());
						final int e = url.lastIndexOf("/?") + 1;
						request.setActionURL(url.substring(0, e));
						show("Creating object in database...");
						request.setHttpMethod("POST");
						request.setContentType("application/x-www-form-urlencoded");
						final JSONObject body = new JSONObject();
						body.put("json", newvalues);
						return body.getJavaScriptObject();
					} else if (DSOperationType.UPDATE.equals(operationtype)) {
						final JSONObject newvalues = new JSONObject(request.getData());
						final JSONObject oldvalues = new JSONObject(request.getOldValues().getJsObj());
						oldvalues.put("links", null);
						final String id = newvalues.get("id").isString().stringValue();
						final int e = url.lastIndexOf("/?") + 1;
						request.setActionURL(url.substring(0, e) + id + "/");
						request.setHttpMethod("PUT");
						request.setContentType("application/x-www-form-urlencoded");
						final JSONObject body = new JSONObject();
						final JSONObject json = new JSONObject();
						json.put("old", oldvalues);
						json.put("new", newvalues);
						body.put("json", json);
						return body.getJavaScriptObject();
					} else if (DSOperationType.REMOVE.equals(operationtype)) {
						final JSONObject newvalues = new JSONObject(request.getData());
						final String id = newvalues.get("id").isString().stringValue();
						final int e = url.lastIndexOf("/?") + 1;
						request.setActionURL(url.substring(0, e) + id + "/");
						request.setHttpMethod("DELETE");
						return null;
					} else {
						return null;
					}
				} catch (final Exception e) {
					Dialogs.warn("RestDataSource.transformRequest: " + e);
					return super.transformRequest(request);
				}

			}

			@Override protected void transformResponse(final DSResponse response, final DSRequest request,
					final Object data) {
				try {
					clear();
					super.transformResponse(response, request, data);
					final DSOperationType operationtype = request.getOperationType();
					if (DSOperationType.FETCH.equals(operationtype)) {
						super.transformResponse(response, request, data);
						if (response.getStatus() == RPCResponse.STATUS_SUCCESS) {
							from = response.getStartRow();
							to = response.getEndRow();
							if (-1 == response.getTotalRows()) {
								response.setTotalRows(total);
							} else {
								total = response.getTotalRows();
							}
							if (51 != total && RestDataSourceGrid.MAXSHOWALLRECORDS >= total) {
								setShowAllRecords(true);
							}
						} else {
							Dialogs.warn(convertRPCStatus(response.getStatus()));
						}
					} else if (DSOperationType.ADD.equals(operationtype)) {
						if (response.getStatus() == RPCResponse.STATUS_SUCCESS) {

						} else if (response.getStatus() == RPCResponse.STATUS_VALIDATION_ERROR) {
							Dialogs.info("Part for this Partner already exists");
						}
					} else if (DSOperationType.UPDATE.equals(operationtype)) {
						if (response.getStatus() == RPCResponse.STATUS_SUCCESS) {

						} else if (response.getStatus() == RPCResponse.STATUS_VALIDATION_ERROR) {
							Dialogs.info("Part for this Partner already exists");
						}
					}
				} catch (final Exception e) {
					Dialogs.warn("RestDataSource.transformResponse: " + e);
				}
			}

			int floor(final int i) {
				final double N = getDataPageSize();
				final int o = (int) (Math.floor(i / N) * N);
				return o;
			}

			int ceil(final int i) {
				final double N = getDataPageSize();
				final int o = (int) (Math.ceil(i / N) * N);
				return o;
			}

			private final void show(final String prompt) {
				if (0 < prompt.length()) {
					this.prompt = prompt;
					timer = new Timer() {
						@Override public void run() {
							timer = null;
							SC.showPrompt(prompt);
							prompts++;
							new Timer() {
								@Override public void run() {
									clear();
								}
							}.schedule(BaseContext.get().timeout());
						}
					};
					timer.schedule(BaseContext.get().veryLongUiTimeout());
				}
			}

			private final void clear() {
				if (0 < prompt.length()) {
					if (null != timer) {
						timer.cancel();
						timer = null;
					}
					if (0 < prompts) {
						prompts--;
						if (0 == prompts) {
							SC.clearPrompt();
						}
					}
					prompt = "";
				}
			}

			private int prompts = 0;
			private Timer timer = null;
			private String prompt = "";

		};

		serverdatasource.setPreventHTTPCaching(false);
		serverdatasource.setShowPrompt(false);
		serverdatasource.setDataFormat(DSDataFormat.JSON);
		serverdatasource.setDataProtocol(DSProtocol.CLIENTCUSTOM);
		serverdatasource.setDataTransport(RPCTransport.XMLHTTPREQUEST);
		serverdatasource.setSendMetaData(false);
		serverdatasource.setClientOnly(false);
		serverdatasource.setFields(fields.toArray(new DataSourceField[fields.size()]));
		serverdatasource.setDataURL(url);

		clientonly = null != datasource;
		if (clientonly) {
			datasource.setFields(fields.toArray(new DataSourceField[fields.size()]));
			switchToClientMode(datasource);
		} else {
			switchToServerMode();
		}
	}

	public String getUrl() {
		return url;
	}

	@Override public Boolean startEditing(final int row, final int column, final boolean suppressfocus) {
		return super.startEditing(row, firsteditablecolumn, suppressfocus);
	}

	@Override protected void onDraw() {
		drawLayout();
	}

	@Override protected MenuItem[] getHeaderContextMenuItems(final Integer index) {
		final MenuItem[] currentitems = super.getHeaderContextMenuItems(index);
		final MenuItem[] menuitems = new MenuItem[currentitems.length + 2];
		for (int i = 0; i < currentitems.length; i++) {
			final MenuItem item = currentitems[i];
			menuitems[i] = item;
		}
		MenuItem withorwithoutfilter;
		if (!clientonly) {
			if (inclientmode) {
				withorwithoutfilter = new MenuItem("No Filtering (switch to server-side mode)",
						"[SKIN]/../RecordEditor/filter_Disabled.png");
				withorwithoutfilter.addClickHandler(new ClickHandler() {
					@Override public void onClick(final MenuItemClickEvent event) {
						switchToServerMode();
						onDraw();
					}
				});
			} else {
				withorwithoutfilter = new MenuItem("Advanced Filtering (switch to client-side mode)",
						"[SKIN]/../RecordEditor/filter.png");
				withorwithoutfilter.addClickHandler(new ClickHandler() {
					@Override public void onClick(final MenuItemClickEvent event) {
						if (RestDataSourceGrid.MAXSHOWALLRECORDS < total) {
							Dialogs.error("Too many rows loaded to switch to 'client-side mode'.<br><br>(" + total
									+ " rows)");
						} else {
							switchToClientMode(new DataSource());
							onDraw();
						}
					}
				});
			}
			menuitems[currentitems.length + 0] = new MenuItemSeparator();
			menuitems[currentitems.length + 1] = withorwithoutfilter;
		}
		return menuitems;
	}

	protected void drawLayout() {
		try {
			setVisible(false);
			setShowHeader(false);
			for (final ListGridField field : getFields()) {
				final String name = field.getName();
				if (-1 < indexOf(name)) {
					field.setCanFilter(true);
					final JSONObject fieldlayout = layout.get(indexOf(name)).isObject();
					boolean islink = false;
					if (fieldlayout.containsKey("islink")) {
						islink = fieldlayout.get("islink").isBoolean().booleanValue();
						if (islink) {
							field.setTarget("javascript");
							String linktype = null;
							if (fieldlayout.containsKey("linktype")) {
								linktype = fieldlayout.get("linktype").isString().stringValue();
								field.setAttribute("linktype", linktype);
							}
						}
					}
					field.setAttribute("islink", islink);
					if (fieldlayout.containsKey("width")) {
						field.setWidth(fieldlayout.get("width").isString().stringValue());
					}
					if (fieldlayout.containsKey("align")) {
						final Alignment align = Alignment.valueOf(fieldlayout.get("align").isString().stringValue()
								.toUpperCase());
						field.setAlign(align);
						field.setCellAlign(align);
					}
					if (fieldlayout.containsKey("title")) {
						setFieldTitle(name, fieldlayout.get("title").isString().stringValue());
					}
					boolean cansort = true;
					if (fieldlayout.containsKey("cansort")) {
						cansort = fieldlayout.get("cansort").isBoolean().booleanValue();
					}
					field.setCanSort(cansort);
					boolean cangroupby = false;
					if (fieldlayout.containsKey("cangroupby")) {
						cangroupby = fieldlayout.get("cangroupby").isBoolean().booleanValue();
					}
					field.setCanGroupBy(cangroupby);
					if (fieldlayout.containsKey("iconurl")) {
						field.setIcon(fieldlayout.get("iconurl").isString().stringValue());
						field.setIconOrientation("right");
					}
				}
			}
		} catch (final Exception e) {
			BaseContext.get().getLogger().info("drawLayout " + e);
		} finally {
			setVisible(true);
			setShowHeader(true);
		}
	}

	private void switchToServerMode() {
		if (null != clientdatasource) {
			clientdatasource.destroy();
			clientdatasource = null;
		}
		setShowAllRecords(false);
		unsort();
		ungroup();
		setCanSort(false);
		setCanGroupBy(false);
		setShowFilterEditor(false);
		setShowFilterExpressionLegendMenuItem(false);
		setAllowFilterExpressions(false);
		setDrawAheadRatio(1.0f);
		setQuickDrawAheadRatio(1.0f);
		setDataSource(serverdatasource);
		invalidateCache();
		fetchData();
		inclientmode = false;
	}

	private void switchToClientMode(final DataSource datasource) {
		clientdatasource = datasource;
		if (!clientonly) {
			clientdatasource.setClientOnly(true);
			clientdatasource.setInheritsFrom(serverdatasource);
			final ResultSet records = getResultSet();
			if (null != records) {
				while (0 < from && records.rowIsLoaded(from - 1)) {
					from--;
				}
				while (to < records.getLength() && records.rowIsLoaded(to)) {
					to++;
				}
				for (int i = from; i < to; ++i) {
					clientdatasource.addData(records.get(i));
				}
			}
		}
		setDataSource(clientdatasource);
		setShowAllRecords(true);
		setCanSort(true);
		setCanGroupBy(true);
		setGroupByMaxRecords(RestDataSourceGrid.MAXSHOWALLRECORDS);
		setGroupStartOpen(GroupStartOpen.ALL);
		setShowFilterEditor(true);
		setShowFilterExpressionLegendMenuItem(true);
		setAllowFilterExpressions(true);
		fetchData();
		inclientmode = true;
	}

	private String convertRPCStatus(final int code) {
		Status status;
		if (RPCResponse.STATUS_SERVER_TIMEOUT == code) {
			status = Status.SERVER_ERROR_GATEWAY_TIMEOUT;
		} else if (RPCResponse.STATUS_TRANSPORT_ERROR == code) {
			status = Status.SERVER_ERROR_SERVICE_UNAVAILABLE;
		} else {
			status = Status.SERVER_ERROR_INTERNAL;
		}
		return status.getCode() + " - Internal server error.<p><p>" + serverdatasource.getFetchDataURL()
				+ RestDataSourceGrid.HINT;
	}

	private int indexOf(final String name) {
		for (int i = 0; i < layout.size(); ++i) {
			final JSONObject fieldlayout = layout.get(i).isObject();
			if (fieldlayout.containsKey("name")) {
				if (name.equals(fieldlayout.get("name").isString().stringValue())) {
					return i;
				}
			}
		}
		return -1;
	}

	public static void noCache(final long nocacheuntil) {
		RestDataSourceGrid.nocacheuntil = nocacheuntil;
	}

	private final String url;
	private final JSONArray layout;
	private final RestDataSource serverdatasource;
	private DataSource clientdatasource = null;
	private boolean inclientmode = false;
	private final boolean clientonly;
	private int total = 0;
	private int from = 0;
	private int to = 0;
	private int firsteditablecolumn = -1;
	private static long nocacheuntil = DateUtil.now().getTime();
	private static final int MAXSHOWALLRECORDS = 2000;
	private static final String HINT = "<p><p>Please try again in a few seconds.<p>";

};
