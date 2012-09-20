package com.ibm.de.ebs.plm.gwt.client.ui.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ShowMessage;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;

public enum Dialogs {

	SERVER_ERROR;

	public static void error(final Object object) {
		Dialogs.showMessage(null, object.toString(), true);
	}

	public static void info(final Object object) {
		Dialogs.showMessage(null, object.toString(), false);
	}

	public static void error(final String message) {
		Dialogs.showMessage(null, message, true);
	}

	public static void info(final String message) {
		Dialogs.showMessage(null, message, false);
	}

	public static void error(final String title, final String message) {
		Dialogs.showMessage(title, message, true);
	}

	public static void info(final String title, final String message) {
		Dialogs.showMessage(title, message, false);
	}

	public static void warn(final String message) {
		BaseContext.get().getLogger().warning(message);
	}

	@Override public String toString() {
		return json.get(name()).isString().stringValue();
	}

	private static void showMessage(final String title, final String message, final boolean error) {
		final ShowMessage event = GWT.create(ShowMessage.class);
		event.message = message;
		event.title = null != title ? title : error ? "Error" : "Information";
		event.error = error;
		event.fire();
	}

	private final native JavaScriptObject messages()
	/*-{
		return {
			"SERVER_ERROR" : "Server error.",
			"" : ""
		};
	}-*/;

	private final JSONObject json = new JSONObject(messages());
}
