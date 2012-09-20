package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.shared.GwtEvent;

public class MouseOutLabelEvent extends GwtEvent<MouseOutLabelHandler> implements HasNativeEvent {

	protected MouseOutLabelEvent(final NativeEvent nativeevent, final String id) {
		this.nativeevent = nativeevent;
		this.id = id;
	}

	public static Type<MouseOutLabelHandler> getType() {
		return MouseOutLabelEvent.TYPE;
	}

	public static void fire(final HasMouseOutLabelHandlers source, final NativeEvent nativeevent, final String id) {
		source.fireEvent(new MouseOutLabelEvent(nativeevent, id));
	}

	public String getId() {
		return id;
	}

	@Override protected void dispatch(final MouseOutLabelHandler handler) {
		handler.onMouseOutLabel(this);
	}

	@Override public final Type<MouseOutLabelHandler> getAssociatedType() {
		return MouseOutLabelEvent.TYPE;
	}

	@Override public NativeEvent getNativeEvent() {
		return nativeevent;
	}

	private static Type<MouseOutLabelHandler> TYPE = new Type<MouseOutLabelHandler>();
	private final NativeEvent nativeevent;
	private final String id;

}
