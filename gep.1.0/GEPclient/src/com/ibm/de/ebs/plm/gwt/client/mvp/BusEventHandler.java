package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.event.shared.EventHandler;

public interface BusEventHandler<D> extends EventHandler {

	void onBusEvent(final BusEvent<D> event);

}
