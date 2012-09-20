package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

/**
 * Set the focus on a specific tab
 * 
 * @author Mario Heisig
 * 
 */
public abstract class SetSelectedTab extends BusEvent<Object> {

	/**
	 * Selected tab, 0 indexed
	 */
	public int index;

}
