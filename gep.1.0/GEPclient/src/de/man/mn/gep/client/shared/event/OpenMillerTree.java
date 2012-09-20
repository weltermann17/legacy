package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.smartgwt.client.data.DataSource;

public abstract class OpenMillerTree extends JSONValueEvent {

	public String detailurl;

	public String displayname;

	public String delimiter;

	public DataSource datasource;

	public int maxLevelChildren() {
		return getMax("maxlevelchildren");
	}

	public int maxLevelParents() {
		return getMax("maxlevelparents");
	}

	private int getMax(final String what) {
		final JSONObject result = getData().isObject().get("response").isObject();
		final int status = (int) result.get("status").isNumber().doubleValue();
		if (0 == status) {
			return (int) result.get(what).isNumber().doubleValue();
		} else {
			return 0;
		}
	}

}
