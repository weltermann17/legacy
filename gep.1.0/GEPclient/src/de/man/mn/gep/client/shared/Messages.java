package de.man.mn.gep.client.shared;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public enum Messages {

	SEARCH_NO_LOCATION, SEARCH_INVALID_VALUE, SEARCH_INVALID_PAIR, NOT_SIGNED_ON;

	@Override public String toString() {
		return json.containsKey(name()) ? json.get(name()).isString().stringValue() : name();
	}

	private final native JavaScriptObject messages()
	/*-{
		return { 
		"SEARCH_NO_LOCATION" : "You need to select at least one location to search in.",
		"SEARCH_INVALID_VALUE" : "Invalid or missing value.",
		"SEARCH_INVALID_PAIR" : "First value must be smaller than second value.",
		"NOT_SIGNED_ON" : "You need to sign on to enable this function.",
		"" : ""
		};
	}-*/;

	private final JSONObject json = new JSONObject(messages());
}
