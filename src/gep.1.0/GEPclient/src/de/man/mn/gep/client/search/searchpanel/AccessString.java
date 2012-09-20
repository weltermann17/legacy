package de.man.mn.gep.client.search.searchpanel;

import name.pehl.piriti.client.json.Json;
import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONParser;

public class AccessString {

	interface Reader extends JsonReader<AccessString> {
	}

	interface Writer extends JsonWriter<AccessString> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

	public AccessString(final String access) {
		this.access = access;
	}

	public AccessString() {
		access = null;
	}

	@Override public boolean equals(final Object other) {
		try {
			return toString().equals(other.toString());
		} catch (final Exception e) {
			return false;
		}
	}

	@Override public String toString() {
		return JSONParser.parseStrict(access).isObject().get("id").isString().stringValue();
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	public String asString() {
		return access;
	}

	@Json protected String access;

}
