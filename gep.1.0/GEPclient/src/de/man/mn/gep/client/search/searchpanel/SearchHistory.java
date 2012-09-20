package de.man.mn.gep.client.search.searchpanel;

import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.util.History;

final class SearchHistory extends History<SearchValue> {

	@Override protected String asString(final SearchValue value) {
		return value.criterion;
	}

	interface Reader extends JsonReader<SearchHistory> {
	}

	interface Writer extends JsonWriter<SearchHistory> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

}
