package de.man.mn.gep.client.shared.dao;

import com.google.gwt.json.client.JSONObject;
import com.smartgwt.client.data.Record;

public class Product extends Detail {

	public Product(final Record record) {
		super(DataType.products, record, record.getAttributeAsString("name"), Detail.getLink(record, "product"));
	}

	public Product(final String displayname, final String producturl) {
		super(DataType.products, displayname, producturl);
	}

	public Product(final JSONObject json) {
		this(new Record(json.getJavaScriptObject()));
	}

	@Override public String getFileName() {
		return getAttribute("name").replace(" ", "_");
	}

}
