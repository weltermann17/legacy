package com.ibm.de.ebs.plm.gwt.client.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.util.StringUtil;

public abstract class ZoomOver extends BusEvent<Object> {

	public String imageurl;

	public String getName() {
		String name = imageurl.substring(0, imageurl.length() - 1);
		name = name.substring(name.lastIndexOf("/") + 1);
		return StringUtil.fromHexString(name);
	}

}
