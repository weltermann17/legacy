package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.google.gwt.event.shared.HasHandlers;
import com.ibm.de.ebs.plm.gwt.client.mvp.Gesture;
import com.smartgwt.client.widgets.events.ClickEvent;

public class ClickGesture extends ClickEvent implements Gesture {

	public ClickGesture() {
		super(null);
	}

	@Override public void setHasHandlers(final HasHandlers hashandlers) {
		this.hashandlers = hashandlers;
	}

	@Override public void setValue(final Object value) {
	}

	@Override public Object getSource() {
		return hashandlers;
	}

	private HasHandlers hashandlers;

}
