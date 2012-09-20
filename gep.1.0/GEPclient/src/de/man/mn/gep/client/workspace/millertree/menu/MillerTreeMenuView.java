package de.man.mn.gep.client.workspace.millertree.menu;

import com.ibm.de.ebs.plm.gwt.client.mvp.BaseView;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;

public abstract class MillerTreeMenuView extends BaseView implements MillerTreeMenuPresenter.View {

	public MillerTreeMenuView() {
		select = new SelectItem();
		select.setWidth(180);
		select.setHeight(25);
		select.setShowTitle(false);
		select.setShowFocused(false);
		select.setCanFocus(false);
		select.setValueMap("Partnumber", "Description DE", "Description EN", "Description FR", "Description PL",
				"Description TR");
		select.setDefaultValue("Description DE");
		form = new DynamicForm();
		form.setFields(select);
	}

	public Canvas asCanvas() {
		return form;
	}

	@Override public HasChangedHandlers select() {
		return select;
	}

	private final DynamicForm form;
	private final SelectItem select;

}
