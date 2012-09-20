package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.logging.Logger;

import com.google.gwt.event.shared.GwtEvent;

public abstract class BusEvent<D> extends GwtEvent<BusEventHandler<D>> implements Generated {

	public BusEvent() {
		this.id = ++BusEvent.counter;
	}

	@Override protected void dispatch(final BusEventHandler<D> handler) {
		handler.onBusEvent(this);
	}

	@Override public String toString() {
		return getClass().getName().replace("Impl", "") + "@" + id;
	}

	public D getData() {
		return data;
	}

	protected void setData(final D data) {
		this.data = data;
	}

	public void fire() {
		refire();
	}

	public void refire() {
		refire(DefaultEventBus.get());
	}

	public void refire(final EventBus eventbus) {
		super.revive();
		eventbus.fireEvent(this);
	}

	@Override public Logger getLogger() {
		return MVP.getLogger();
	}

	@Override public boolean equals(final Object other) {
		if (other instanceof BusEvent<?>) {
			return id == ((BusEvent<?>) other).id;
		} else {
			return false;
		}
	}

	private D data;
	protected final int id;
	private static int counter = 0;
}
