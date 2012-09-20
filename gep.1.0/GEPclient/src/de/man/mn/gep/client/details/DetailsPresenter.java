package de.man.mn.gep.client.details;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.TabCloseClickGesture;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.HasCloseClickHandlers;

import de.man.mn.gep.client.details.detail.DetailModel;
import de.man.mn.gep.client.details.detail.DetailPresenter;
import de.man.mn.gep.client.details.detail.DetailView;
import de.man.mn.gep.client.details.user.UserModel;
import de.man.mn.gep.client.details.user.UserPresenter;
import de.man.mn.gep.client.details.user.UserView;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class DetailsPresenter extends BasePresenter<DetailsModel, DetailsView> {

	interface View {

		@RequestSender("UserClosed") HasCloseClickHandlers tabset();

		@RequestGesture(request = "UserClosed", gesture = TabCloseClickGesture.class) void userClosed();

		void setUser(final Tab user);

		void setDetails(final Canvas details, final String displayname);

	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		if (event.isSuccess()) {
			final DetailView view = GWT.create(DetailView.class);
			final DetailModel model = GWT.create(DetailModel.class);
			final DetailPresenter presenter = GWT.create(DetailPresenter.class);
			view().setDetails(MVP.create(model, view, presenter, event).asCanvas(), event.detail.getDisplayName());
		} else {
			view().closeAllTabs();
		}
	}

	@EventReceiver void userSelected(final UserSelected event) {
		if (event.isSuccess()) {
			final UserView view = GWT.create(UserView.class);
			final UserModel model = GWT.create(UserModel.class);
			final UserPresenter presenter = GWT.create(UserPresenter.class);
			view().setUser(MVP.create(model, view, presenter, event).asTab());
		}
	}

}
