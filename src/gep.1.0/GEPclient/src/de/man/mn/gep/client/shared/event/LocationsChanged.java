package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

import de.man.mn.gep.client.shared.dao.Location;

public abstract class LocationsChanged extends BusEvent<Object> {

	public Location[] locations;
	public Location[] selectedlocations;
	public Boolean[] selection;

}
