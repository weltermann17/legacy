package de.man.mn.gep.client.main.signon;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;

import de.man.mn.gep.client.shared.event.FocusSignOn;

public abstract class SignOnPresenter extends BasePresenter<SignOnModel, SignOnView> {

	interface View {

		@RequestSender("SignOn") HasClickHandlers signonButton();

		@RequestSender("SignOn") HasClickHandlers userImage();

		@RequestSender("PasswordChanged") HasChangedHandlers passwordChanged();

		@RequestSender("UsernameChanged") HasChangedHandlers usernameChanged();

	}

	@Override protected void onBound() {
		view().setPlmServers(model().getPlmServers());
		view().focus();
	}

	@EventReceiver void focus(final FocusSignOn event) {
		onBound();
	}

}
