package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.google.gwt.event.shared.HasHandlers;
import com.ibm.de.ebs.plm.gwt.client.mvp.Gesture;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;

public class ChangedGesture extends ChangedEvent implements Gesture {

	public ChangedGesture() {
		super(null);
	}

	@Override public void setHasHandlers(final HasHandlers hashandlers) {
		formitem = (FormItem) hashandlers;
	}

	@Override public void setValue(final Object value) {
		if (value instanceof String[]) {
			formitem.setValueMap((String[]) value);
		} else {
			formitem.setValue(value);
		}
	}

	@Override public Object getValue() {
		return formitem.getValue();
	}

	@Override public FormItem getItem() {
		return formitem;
	}

	@Override public DynamicForm getForm() {
		return formitem.getForm();
	}

	private FormItem formitem;

}
