package de.man.mn.gep.client.details.user;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.form.fields.events.HasClickHandlers;

public abstract class UserPresenter extends BasePresenter<UserModel, UserView> {

	interface View {
		@RequestSender("ManagerSelected") HasClickHandlers manager();
	}

}
