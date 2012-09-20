package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class ChangeSearchType extends BusEvent<Object> {

	public static enum AcceptedTypes {
		Parts, PartnerParts, Products, Snapshots
	}

	public String type;
}
