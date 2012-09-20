package de.man.mn.gep.client.search.searchpanel;

import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.util.History;

final class CriterionHistory extends History<CriterionValue> {

	@Override protected String asString(final CriterionValue value) {
		return value.text;
	}

	interface Reader extends JsonReader<CriterionHistory> {
	}

	interface Writer extends JsonWriter<CriterionHistory> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

}
