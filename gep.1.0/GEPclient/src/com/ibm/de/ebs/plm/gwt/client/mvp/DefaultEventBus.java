package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.logging.Level;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.UmbrellaException;

public class DefaultEventBus extends HandlerManager implements EventBus {

	private DefaultEventBus() {
		super(null);
	}

	public static DefaultEventBus get() {
		if (null == DefaultEventBus.instance) {
			DefaultEventBus.instance = new DefaultEventBus();
		}
		return DefaultEventBus.instance;
	}

	@Override public <H extends EventHandler> HandlerRegistration addHandler(final Type<H> type, final H handler) {
		for (int i = 0; i < getHandlerCount(type); ++i) {
			if (handler.equals(getHandler(type, i))) {
				return null;
			}
		}
		return super.addHandler(type, handler);
	}

	@Override public void fireEvent(final GwtEvent<?> event) {
		if (MVP.getLogger().isLoggable(Level.FINE)) {
			MVP.getLogger().fine("DefaultBus.fireEvent() : " + event);
		}
		try {
			DefaultEventBus.super.fireEvent(event);
		} catch (final UmbrellaException e) {
			for (final Throwable caught : e.getCauses()) {
				MVP.getLogger().severe("DefaultEventBus.fireEvent() : " + event + "\n" + caught.toString());
			}
		} catch (final Exception e) {
			MVP.getLogger().severe("DefaultEventBus.fireEvent() : " + event + "\n" + e.toString());
		}
	}

	private static DefaultEventBus instance = null;

}
