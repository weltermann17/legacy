package de.man.mn.gep.client.shared.dao;

import com.google.gwt.json.client.JSONObject;
import com.smartgwt.client.data.Record;

public class Version extends Detail {

	public Version(final Record record) {
		super(DataType.versions, record, record.getAttributeAsString("name") + " "
				+ record.getAttributeAsString("versionstring"), Detail.getLink(record, "version"));
	}

	public Version(final String displayname, final String versionurl) {
		super(DataType.versions, displayname, versionurl);
	}

	public Version(final JSONObject json) {
		this(new Record(json.getJavaScriptObject()));
	}

}
