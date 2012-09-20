package de.man.mn.gep.client.workspace.millertree.menu;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;

public abstract class MillerTreeMenuPresenter extends BasePresenter<MillerTreeMenuModel, MillerTreeMenuView> {

	interface View {

		@RequestSender("AttributeChanged") HasChangedHandlers select();

	}

}
