package de.man.mn.gep.client.workspace.millertree;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellOverEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.Version;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.OpenMillerTree;
import de.man.mn.gep.client.workspace.WorkspaceTabModel;

public abstract class MillerTreeModel extends WorkspaceTabModel {

	@Override protected void onInit(final BusEvent<?> event) {
		final OpenMillerTree e = (OpenMillerTree) event;
		clickedversion = new Version(e.displayname, e.detailurl);
		previousversion = clickedversion;
		super.onInit(event);
	}

	@RequestReceiver("DetailSelected") @EventSender(DetailSelected.class) void versionSelected(final int i,
			final Request<CellClickEvent> request, final Response<JSONValue, DetailSelected> response) {
		final CellClickEvent gesture = request.gesture();
		final Record record = gesture.getRecord();
		final String displayname = record.getAttributeAsString("displayname");
		final String url = record.getAttributeAsString("url");
		clickedversion = new Version(displayname, url);
		handleRequest(response, clickedversion);
	}

	@RequestReceiver("DetailOver") @EventSender(DetailSelected.class) void versionOver(final int i,
			final Request<CellOverEvent> request, final Response<JSONValue, DetailSelected> response) {
		final CellOverEvent gesture = request.gesture();
		final Record record = gesture.getRecord();
		final String displayname = record.getAttributeAsString("displayname");
		final String url = record.getAttributeAsString("url");
		handleRequest(response, new Version(displayname, url));
	}

	@RequestReceiver("DetailOut") @EventSender(DetailSelected.class) <E extends GwtEvent<?>> void versionOut(
			final int i, final Request<E> request, final Response<JSONValue, DetailSelected> response) {
		handleRequest(response, clickedversion);
	}

	private void handleRequest(final Response<JSONValue, DetailSelected> response, final Version version) {
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		if (!version.equals(previousversion)) {
			timer = new Timer() {
				@Override public void run() {
					response.event().detail = version;
					response.event().datasource = new RestletDataSource(version.getDetailUrl(), response, "",
							"Not available.");
					previousversion = version;
				}
			};
			timer.schedule(clickedversion.equals(version) ? 20 : Context.get().longUiTimeout());
		}
	}

	final Map<String, Version> versions = new LinkedHashMap<String, Version>();
	private Version clickedversion;
	private Version previousversion;
	private Timer timer = null;

}
