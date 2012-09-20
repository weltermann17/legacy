package de.man.mn.gep.client.shared.dao;

import java.util.Map;

import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.smartgwt.client.data.Record;

import de.man.mn.gep.client.shared.Context;

public class Detail {

	protected Detail(final DataType datatype, final Record record, final String displayname, final String detailurl) {
		this.datatype = datatype;
		this.record = record;
		id = record.getAttribute("id");
		this.displayname = displayname;
		this.detailurl = detailurl;
		formatsurl = getLink("formatssummary");
	}

	protected Detail(final DataType datatype, final String displayname, final String detailurl) {
		this.datatype = datatype;
		record = null;
		final String d = "/" + datatype.name() + "/";
		final int b = detailurl.indexOf(d) + d.length();
		final int e = detailurl.indexOf("/", b);
		if (32 != e - b) {
			Dialogs.warn("Invalid id, length = " + (e - b) + d + " " + detailurl);
			id = "";
		} else {
			id = detailurl.substring(b, e);
		}
		this.displayname = displayname;
		this.detailurl = detailurl;
		formatsurl = detailurl + "formats/summary/";
	}

	@Override public boolean equals(final Object b) {
		if (b instanceof Detail) {
			return getDetailUrl().equals(((Detail) b).getDetailUrl());
		}
		return false;
	}

	public DataType getDataType() {
		return datatype;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayname;
	}

	public String getFileName() {
		return getAttribute("name") + "_G3D_0001_" + getAttribute("versionstring");
	}

	public String getAlias() {
		return getDisplayName();
	}

	public String getDetailUrl() {
		return detailurl;
	}

	public String getFormatsUrl() {
		return formatsurl;
	}

	public String getAttribute(final String name) {
		if (null == record.getAttribute(name)) {
			Context.get().getLogger().info("Detail.getAttribute() : attribute not found : " + name);
		}
		return record.getAttributeAsString(name);
	}

	public boolean isAssembly() {
		return getRecord().getAttributeAsBoolean("isassembly");
	}

	@Override public String toString() {
		return toJson().toString();
	}

	public JSONObject toJson() {
		return new JSONObject(record.getJsObj());
	}

	public String getLink(final String name) {
		@SuppressWarnings("unchecked")
		final Map<String, String> links = record.getAttributeAsMap("links");
		return links.get(name);
	}

	public Record getRecord() {
		return record;
	}

	protected static String getLink(final Record record, final String name) {
		@SuppressWarnings("unchecked")
		final Map<String, String> links = record.getAttributeAsMap("links");

		return links.get(name);
	}

	public static final Detail create(final Record record) {
		final DataType datatype = DataType.valueOf(record.getAttribute("datatype"));
		switch (datatype) {
		case versions:
		case bom:
		case whereused:
			return new Version(record);
		case instances:
			return new Instance(record);
		case products:
			return new Product(record);
		case snapshots:
			return new Snapshot(record);
		case partnerversions:
			return new PartnerVersion(record);
		default:
			return null;
		}
	}

	private final Record record;
	private final String id;
	private final String displayname;
	private final String detailurl;
	private final String formatsurl;
	private final DataType datatype;

}
