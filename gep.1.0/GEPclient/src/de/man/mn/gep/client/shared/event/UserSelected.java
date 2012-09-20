package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;

public abstract class UserSelected extends JSONValueEvent {

	public String user;

	public RestletDataSource datasource;

}
