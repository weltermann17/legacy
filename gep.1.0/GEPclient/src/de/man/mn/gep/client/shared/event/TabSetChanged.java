package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.smartgwt.client.widgets.tab.TabSet;

public abstract class TabSetChanged extends BusEvent<Object> {

	public TabSet tabset;

}
