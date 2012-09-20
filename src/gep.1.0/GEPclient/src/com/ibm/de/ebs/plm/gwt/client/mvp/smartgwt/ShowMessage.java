package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class ShowMessage extends BusEvent<Object> {

	public String message;
	public String title;
	public boolean error;

}
