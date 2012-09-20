package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;

public abstract class SignedOnUser extends JSONValueEvent {

	public String getFullName() {
		if (null == fullname) {
			if (isSuccess()) {
				final String first = getDataArray().get(0).isObject().get("firstname").isString().stringValue();
				final String last = getDataArray().isArray().get(0).isObject().get("lastname").isString().stringValue();
				fullname = first + " " + last;
			} else {
				fullname = "NN";
			}
		}
		return fullname;
	}

	private String fullname = null;

}
