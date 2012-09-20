package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;

import de.man.mn.gep.client.shared.dao.Detail;

public abstract class Documents extends JSONValueEvent {

	public Detail detail;

	public String getUrl(final String extension, final String geometrytype, final String pagenumber) {
		if (isSuccess()) {
			final JSONArray formats = getDataArray();
			for (int i = 0; i < formats.size(); ++i) {
				final JSONObject format = formats.get(i).isObject();
				final String fileextension = format.get("extension").isString().stringValue().toLowerCase();
				if (extension.equalsIgnoreCase(fileextension)) {
					final boolean istype = format.get("name").isString().stringValue().toLowerCase()
							.contains(geometrytype.toLowerCase());
					final String page = format.get("page").isString().stringValue();
					if (istype && page.equals(pagenumber)) {
						return format.get("url").isString().stringValue();
					}
				}
			}
		}
		return null;
	}

	public long getFileSize(final String extension, final String geometrytype, final String pagenumber) {
		if (isSuccess()) {
			final JSONArray formats = getDataArray();
			for (int i = 0; i < formats.size(); ++i) {
				final JSONObject format = formats.get(i).isObject();
				final String fileextension = format.get("extension").isString().stringValue();
				if (extension.equalsIgnoreCase(fileextension)) {
					final boolean istype = format.get("name").isString().stringValue().toLowerCase()
							.contains(geometrytype.toLowerCase());
					final String page = format.get("page").isString().stringValue();
					if (istype && page.equals(pagenumber)) {
						return (long) format.get("filesize").isNumber().doubleValue();
					}
				}
			}
		}
		return -1;
	}

}
