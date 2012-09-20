package de.man.mn.gep.client.search.searchpanel;

import java.util.Date;

import name.pehl.piriti.client.json.Json;
import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;

final class CriterionValue {

	interface Reader extends JsonReader<CriterionValue> {
	}

	interface Writer extends JsonWriter<CriterionValue> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

	static CriterionValue text(final String text, final String type) {
		final CriterionValue value = new CriterionValue();
		value.text = text;
		value.type = type;
		return value;
	}

	static CriterionValue fromTo(final Date from, final Date to, final String type) {
		final CriterionValue value = new CriterionValue();
		value.from = from;
		value.to = to;
		value.type = type;
		return value;
	}

	@Override public String toString() {
		return text + type + from + to;
	}

	@Override public boolean equals(final Object other) {
		return toString().equals(other.toString());
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Json String text;
	@Json String type;
	@Json Date from;
	@Json Date to;
}
