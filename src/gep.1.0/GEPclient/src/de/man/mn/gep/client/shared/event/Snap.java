package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class Snap extends BusEvent<Object> {

	public boolean vertical;
	public boolean horizontal;

}
