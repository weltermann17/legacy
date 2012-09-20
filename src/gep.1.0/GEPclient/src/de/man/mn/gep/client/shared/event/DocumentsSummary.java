package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;

import de.man.mn.gep.client.shared.dao.Detail;

public abstract class DocumentsSummary extends JSONValueEvent {

	public Detail detail;

	public long getFileSize(final String extension) {
		return (long) getTotal(extension, "filesize");
	}

	public long getFileSizeKb(final String extension) {
		return (long) getTotal(extension, "filesize_kb");
	}

	public double getFileSizeMb(final String extension) {
		return (long) getTotal(extension, "filesize_mb");
	}

	public String getVaults(final String extension) {
		StringBuilder buf = new StringBuilder();
		if (isSuccess()) {
			final long total = getFileSize(extension);
			final JSONArray data = getSummary();
			int j = 0;
			for (int i = 0; i < data.size(); ++i) {
				final JSONObject pervault = data.get(i).isObject();
				if (extension.equalsIgnoreCase(pervault.get("extension").isString().stringValue())) {
					final long filesize = (long) pervault.get("filesize").isNumber().doubleValue();
					final long percentage = Math.round(100. * filesize / (1. * total));
					final String vault = pervault.get("vault").isString().stringValue();
					if (0 < j++) {
						buf.append(", ");
					}
					buf.append(vault.substring(0, 1).toUpperCase()).append(vault.substring(1)).append(" (")
							.append(percentage).append("%)");
				}
			}
		}
		if (!buf.toString().contains(", ")) {
			buf = new StringBuilder(buf.toString().replace(" (100%)", ""));
		}
		return buf.toString();
	}

	private double getTotal(final String extension, final String fieldname) {
		if (isSuccess()) {
			double total = 0;

			final JSONArray data = getSummary();
			for (int i = 0; i < data.size(); ++i) {
				final JSONObject format = data.get(i).isObject();
				if (extension.equalsIgnoreCase(format.get("extension").isString().stringValue())) {
					total += format.get(fieldname).isNumber().doubleValue();
				}
			}
			return total;
		}
		return -1;
	}

	public String getUrl(final String extension, final String geometrytype, final String pagenumber) {
		if (isSuccess()) {
			final JSONArray formats = getFormats();
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
			final JSONArray formats = getFormats();
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

	public JSONArray getFormats() {
		return getResponse().get("data").isObject().get("formats").isArray();
	}

	public JSONArray getSummary() {
		return getResponse().get("data").isObject().get("summary").isArray();
	}

}
