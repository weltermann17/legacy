package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.shared.GwtEvent;

public class ClickLabelEvent extends GwtEvent<ClickLabelHandler> implements HasNativeEvent {

	protected ClickLabelEvent(final NativeEvent nativeevent, final String id) {
		this.nativeevent = nativeevent;
		this.id = id;
	}

	public static Type<ClickLabelHandler> getType() {
		return ClickLabelEvent.TYPE;
	}

	public static void fire(final HasClickLabelHandlers source, final NativeEvent nativeevent, final String id) {
		source.fireEvent(new ClickLabelEvent(nativeevent, id));
	}

	public String getId() {
		return id;
	}

	@Override public NativeEvent getNativeEvent() {
		return nativeevent;
	}

	@Override protected void dispatch(final ClickLabelHandler handler) {
		handler.onClickLabel(this);
	}

	@Override public final Type<ClickLabelHandler> getAssociatedType() {
		return ClickLabelEvent.TYPE;
	}

	private final String id;
	private final NativeEvent nativeevent;

	private static Type<ClickLabelHandler> TYPE = new Type<ClickLabelHandler>();
}
