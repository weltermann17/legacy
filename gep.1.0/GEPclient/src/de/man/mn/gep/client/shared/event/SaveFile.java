package de.man.mn.gep.client.shared.event;

import org.restlet.client.data.MediaType;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public abstract class SaveFile extends BusEvent<Object> {

	/**
	 * Save as file type
	 */
	public MediaType mediatype;
}
