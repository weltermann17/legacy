package com.ibm.de.ebs.plm.gwt.client.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class DomUtil {

	public static native void preventDefaultContextMenu(Element element)
	/*-{
		element.oncontextmenu = function() {
			return false;
		};
	}-*/;

	public static void removeParentPadding(final Widget widget) {
		final Element element = (com.google.gwt.user.client.Element) widget.getElement().getParentElement();
		DOM.setStyleAttribute(element, "paddingTop", "0px");
		DOM.setStyleAttribute(element, "paddingRight", "0px");
		DOM.setStyleAttribute(element, "paddingBottom", "0px");
		DOM.setStyleAttribute(element, "paddingLeft", "0px");
	}

	public static void noBorder(final Widget widget) {
		DOM.setStyleAttribute(widget.getElement(), "border", "0px");
	}

}
