package de.man.mn.gep.client.shared.dao;

import com.google.gwt.json.client.JSONObject;
import com.smartgwt.client.data.Record;

public class Instance extends Detail {

	public Instance(final Record record) {
		super(DataType.instances, record, record.getAttributeAsString("instance") + " ("
				+ record.getAttributeAsString("level") + ")", Detail.getLink(record, "instance"));
	}

	public Instance(final JSONObject json) {
		this(new Record(json.getJavaScriptObject()));
	}

	@Override public boolean isAssembly() {
		return true;
	}

	@Override public String getAlias() {
		return getAttribute("name") + " " + getAttribute("versionstring");
	}

}
