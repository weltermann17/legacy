package de.man.mn.gep.client.workspace.millertree;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class AttributeChanged extends BusEvent<Object> {

	public String attributename;

	public String id;

}
