package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.util.SecurityUtil;

public abstract class SignedOn extends JSONValueEvent {

	public void setSecret(final String secret) {
		this.secret = secret;
	}

	@Override public boolean isSuccess() {
		return super.isSuccess() && getLocalSecret().equals(getRemoteSecret());
	}

	public String getLocalSecret() {
		return SecurityUtil.crypt(getRemoteSecret(), secret);
	}

	private String getRemoteSecret() {
		if (super.isSuccess()) {
			final JSONObject data = getDataArray().get(0).isObject();
			return data.get("password").isString().stringValue().replace("{CRYPT}", "");
		} else {
			return null;
		}
	}

	private String secret;

}
