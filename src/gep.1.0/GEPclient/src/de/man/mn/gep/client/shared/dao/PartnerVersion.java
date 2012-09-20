package de.man.mn.gep.client.shared.dao;

import com.google.gwt.json.client.JSONObject;
import com.smartgwt.client.data.Record;

public class PartnerVersion extends Detail {

	public PartnerVersion(final Record record) {
		super(DataType.partnerversions, record, record.getAttributeAsString("name"), Detail.getLink(record,
				"partnerversion"));
	}

	public PartnerVersion(final String displayname, final String versionurl) {
		super(DataType.partnerversions, displayname, versionurl);
	}

	public PartnerVersion(final JSONObject json) {
		this(new Record(json.getJavaScriptObject()));
	}

}
