package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.shared.GwtEvent;

public class MouseOverLabelEvent extends GwtEvent<MouseOverLabelHandler> implements HasNativeEvent {

	protected MouseOverLabelEvent(final NativeEvent nativeevent, final String id) {
		this.nativeevent = nativeevent;
		this.id = id;
	}

	public static Type<MouseOverLabelHandler> getType() {
		return MouseOverLabelEvent.TYPE;
	}

	public static void fire(final HasMouseOverLabelHandlers source, final NativeEvent nativeevent, final String id) {
		source.fireEvent(new MouseOverLabelEvent(nativeevent, id));
	}

	public String getId() {
		return id;
	}

	@Override protected void dispatch(final MouseOverLabelHandler handler) {
		handler.onMouseOverLabel(this);
	}

	@Override public final Type<MouseOverLabelHandler> getAssociatedType() {
		return MouseOverLabelEvent.TYPE;
	}

	@Override public NativeEvent getNativeEvent() {
		return nativeevent;
	}

	private final NativeEvent nativeevent;
	private final String id;
	private static Type<MouseOverLabelHandler> TYPE = new Type<MouseOverLabelHandler>();

}
