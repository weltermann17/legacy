package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class Request<G extends GwtEvent<? extends EventHandler>> {

	public Request(final String name) {
		this(name, null);
	}

	public Request(final String name, final G gesture) {
		this.name = name;
		this.gesture = gesture;
	}

	public String name() {
		return name;
	}

	public G gesture() {
		return gesture;
	}

	private final String name;
	private final G gesture;

}
