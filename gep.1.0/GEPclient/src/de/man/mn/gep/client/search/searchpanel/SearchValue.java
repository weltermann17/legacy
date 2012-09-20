package de.man.mn.gep.client.search.searchpanel;

import name.pehl.piriti.client.json.Json;
import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;

final class SearchValue {

	interface Reader extends JsonReader<SearchValue> {
	}

	interface Writer extends JsonWriter<SearchValue> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

	static SearchValue create(final String criterion, final CriterionValue value) {
		final SearchValue searchvalue = new SearchValue();
		searchvalue.criterion = criterion;
		searchvalue.value = value;
		return searchvalue;
	}

	@Override public String toString() {
		return criterion + value;
	}

	@Override public boolean equals(final Object other) {
		return toString().equals(other.toString());
	}

	@Json String criterion;
	@Json CriterionValue value;
}
