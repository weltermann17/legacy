package de.man.mn.gep.client.workspace.spacetree;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.ClickLabelEvent;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.MouseOverLabelEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.Version;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.OpenSpaceTree;
import de.man.mn.gep.client.workspace.WorkspaceTabModel;

public abstract class SpaceTreeModel extends WorkspaceTabModel {

	@Override protected void onInit(final BusEvent<?> event) {
		final OpenSpaceTree e = (OpenSpaceTree) event;
		parseTree(e.getData().isObject());
		final String id = e.getData().isObject().get("id").isString().stringValue();
		clickedversion = versions.get(id);
		previousversion = clickedversion;
		super.onInit(event);
	}

	@RequestReceiver("DetailSelected") @EventSender(DetailSelected.class) void DetailSelected(
			final Request<ClickLabelEvent> request, final Response<JSONValue, DetailSelected> response) {
		final ClickLabelEvent gesture = request.gesture();
		final JSONObject node = new JSONObject(((JitWidget) gesture.getSource()).getCurrentNode());
		clickedversion = versions.get(node.get("id").isString().stringValue());
		handleRequest(response, clickedversion);
	}

	@RequestReceiver("DetailOver") @EventSender(DetailSelected.class) void versionOver(
			final Request<MouseOverLabelEvent> request, final Response<JSONValue, DetailSelected> response) {
		final MouseOverLabelEvent gesture = request.gesture();
		handleRequest(response, versions.get(gesture.getId()));
	}

	@RequestReceiver("DetailOut") @EventSender(DetailSelected.class) <E extends GwtEvent<?>> void versionOut(
			final Request<E> request, final Response<JSONValue, DetailSelected> response) {
		handleRequest(response, clickedversion);
	}

	void onLoadSubset(final JSONValue data) {
		parseTree(data.isObject());
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

	private void parseTree(final JSONObject subtree) {
		final String displayname = subtree.get("name").isString().stringValue();
		final String versionurl = subtree.get("data").isObject().get("url").isString().stringValue();
		versions.put(subtree.get("id").isString().stringValue(), new Version(displayname, versionurl));
		if (subtree.containsKey("children")) {
			for (int i = 0; i < subtree.get("children").isArray().size(); ++i) {
				parseTree(subtree.get("children").isArray().get(i).isObject());
			}
		}
	}

	final Map<String, Version> versions = new LinkedHashMap<String, Version>();
	private Version clickedversion;
	private Version previousversion;
	Timer timer = null;

}
