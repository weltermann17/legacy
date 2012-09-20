package de.man.mn.gep.client.search.searchpanel;

import name.pehl.piriti.client.json.JsonReader;
import name.pehl.piriti.client.json.JsonWriter;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.util.History;

final class AccessHistory extends History<AccessString> {

	interface Reader extends JsonReader<AccessHistory> {
	}

	interface Writer extends JsonWriter<AccessHistory> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	static final Writer Writer = GWT.create(Writer.class);

	@Override protected String asString(final AccessString access) {
		return access.asString();
	}

}
