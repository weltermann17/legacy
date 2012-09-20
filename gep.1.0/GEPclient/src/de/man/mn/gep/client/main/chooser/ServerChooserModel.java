package de.man.mn.gep.client.main.chooser;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.util.Uuid;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.tile.TileRecord;

public abstract class ServerChooserModel extends BaseModel {

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/configuration/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {
		try {
			final JSONArray serverconfig = data.isObject().get("de.man.mn.gep.enovia5.vault.configuration").isArray();
			for (int i = 0; i < serverconfig.size(); ++i) {
				final String server = serverconfig.get(i).isArray().get(3).isString().stringValue();
				final int e = server.lastIndexOf(":");
				if (5 == server.length() - e && server.endsWith("89")) {
					servers.add(server.substring(0, e + 1) + server.substring(e + 1).replace("89", "88"));
				}
			}
		} catch (final Exception e) {
			getLogger().severe("SearchModel.onInit() : " + e);
		}
	}

	Record[] getConnectionUrls() {
		class ConnectionUrl extends TileRecord {
			ConnectionUrl(final String url, final String server, final String location, final String loadtime) {
				super(JavaScriptObject.createObject());
				setAttribute("spacer", "");
				setAttribute("url", url);
				setAttribute("server", server);
				setAttribute("location", location);
				setAttribute("loadtime", loadtime);
			}
		}

		final Record[] result = new Record[servers.size()];
		int i = 0;
		for (final String server : servers) {
			final String imagename = server.substring(0, server.lastIndexOf(":")).replace("http://", "") + ".png";
			final String url = server + "/content/locations/" + imagename + "?" + Uuid.uuid();
			result[i++] = new ConnectionUrl(url, server, getLocation(server), "");
		}
		return result;
	}

	private String getLocation(final String server) {
		if (server.contains("10.132.74.108")) {
			return "M\u00fcnchen";
		} else if (server.contains("10.76.6.37")) {
			return "N\u00fcrnberg";
		} else if (server.contains("10.140.6.29")) {
			return "Salzgitter";
		} else if (server.contains("10.131.93.57")) {
			return "Posen";
		} else if (server.contains("10.131.0.223")) {
			return "Wien";
		} else if (server.contains("10.220.6.63")) {
			return "Steyr";
		} else {
			return "Unknown";
		}
	}

	final Set<String> servers = new HashSet<String>();

}
