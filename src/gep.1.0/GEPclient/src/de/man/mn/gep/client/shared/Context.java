package de.man.mn.gep.client.shared;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;

public class Context extends BaseContext {

	public static Context get() {
		if (null == BaseContext.get()) {
			BaseContext.set(new Context());
		}
		return (Context) BaseContext.get();
	}

	public String buildNumber() {
		return "3911";
	}

	public String versionStringShort() {
		return "GEPbrowswer/1.2";
	}

	public String versionString() {
		return "GEPbrowswer/1.2 (build " + buildNumber() + ")";
	}

	@Override public int timeout() {
		return 90000;
	}

	@Override public int longTimeout() {
		return 180000;
	}

	public String boldStyle() {
		return "headerItem";
	}

	public String derivedLocation() {
		return derivedlocation;
	}

	private Context() {
		final Response<JSONValue, JSONValueEvent> response = new Response<JSONValue, JSONValueEvent>(null) {
			@Override public void onSuccess(final JSONValue data) {
				derivedlocation = data.isArray().get(0).isString().stringValue();
			}
		};
		JsonResource.get("/static/configuration/location/", response, false);
	}

	private String derivedlocation = null;

}
