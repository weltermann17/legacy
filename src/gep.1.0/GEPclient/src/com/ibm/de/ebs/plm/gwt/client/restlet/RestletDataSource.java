package com.ibm.de.ebs.plm.gwt.client.restlet;

import org.restlet.client.data.Method;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;

public class RestletDataSource extends DataSource implements Response.Intercept<JSONValue> {

	public RestletDataSource(final String url, final Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final String prompt, final String errormessage) {
		this(url, response, null, Method.GET, prompt, errormessage);
	}

	public RestletDataSource(final String url, final Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final JSONValue input, final Method method, final String prompt, final String errormessage) {
		this.errormessage = errormessage;
		setClientOnly(true);
		setAutoCacheAllData(true);
		response.setIntercept(this);

		if (Method.GET.equals(method)) {
			jsonresource = JsonResource.get(url, response, prompt, errormessage, true);
		} else if (Method.HEAD.equals(method)) {
			jsonresource = JsonResource.head(url, response, prompt, errormessage, true);
		} else if (Method.DELETE.equals(method)) {
			jsonresource = JsonResource.delete(url, response, prompt, errormessage, true);
		} else if (Method.POST.equals(method)) {
			jsonresource = JsonResource.post(url, response, input, prompt, errormessage, true);
		} else if (Method.PUT.equals(method)) {
			jsonresource = JsonResource.put(url, response, input, prompt, errormessage, true);
		} else {
			jsonresource = null;
		}
	}

	public void cancel() {
		jsonresource.cancel();
	}

	@Override public boolean onSuccess(final JSONValue data) {
		final JSONObject result = data.isObject().get("response").isObject();
		final int status = (int) result.get("status").isNumber().doubleValue();
		if (0 == status) {
			final Record[] records = Record.convertToRecordArray(result.get("data").isArray().getJavaScriptObject());
			setTestData(records);
			return true;
		}
		return false;
	}

	@Override public void onFailure(final Throwable caught) {
		Dialogs.error(errormessage);
	}

	private final JsonResource jsonresource;
	private final String errormessage;

}
