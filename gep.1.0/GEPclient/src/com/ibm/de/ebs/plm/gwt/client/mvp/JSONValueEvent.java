package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public abstract class JSONValueEvent extends BusEvent<JSONValue> {

	public boolean isSuccess() {
		try {
			final JSONObject result = getData().isObject().get("response").isObject();
			final int status = (int) result.get("status").isNumber().doubleValue();
			return 0 == status;
		} catch (final Exception e) {
			return false;
		}
	}

	public JSONObject getResponse() {
		return getData().isObject().get("response").isObject();
	}

	public JSONArray getDataArray() {
		return getResponse().get("data").isArray();
	}

}
