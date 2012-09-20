package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasClickLabelHandlers extends HasHandlers {

	HandlerRegistration addClickLabelHandler(ClickLabelHandler handler);

}
