package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class OpenSpaceTree extends BusEvent<JSONValue> {

	public String detailurl;

	public String displayname;

	public String delimiter;

}
