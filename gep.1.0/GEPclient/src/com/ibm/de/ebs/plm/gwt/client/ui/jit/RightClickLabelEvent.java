package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.shared.GwtEvent;

public class RightClickLabelEvent extends GwtEvent<RightClickLabelHandler> implements HasNativeEvent {

	protected RightClickLabelEvent(final NativeEvent nativeevent, final String id) {
		this.nativeevent = nativeevent;
		this.id = id;
	}

	public static Type<RightClickLabelHandler> getType() {
		return RightClickLabelEvent.TYPE;
	}

	public static void fire(final HasRightClickLabelHandlers source, final NativeEvent nativeevent, final String id) {
		source.fireEvent(new RightClickLabelEvent(nativeevent, id));
	}

	public String getId() {
		return id;
	}

	@Override public NativeEvent getNativeEvent() {
		return nativeevent;
	}

	@Override protected void dispatch(final RightClickLabelHandler handler) {
		handler.onRightClickLabel(this);
	}

	@Override public final Type<RightClickLabelHandler> getAssociatedType() {
		return RightClickLabelEvent.TYPE;
	}

	private final NativeEvent nativeevent;
	private final String id;
	private static Type<RightClickLabelHandler> TYPE = new Type<RightClickLabelHandler>();
}
