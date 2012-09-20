package de.man.mn.gep.client.details.detail;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.form.fields.events.HasClickHandlers;

public abstract class DetailPresenter extends BasePresenter<DetailModel, DetailView> {

	interface View {

		@RequestSender("Links") HasClickHandlers[] links();

	}

	@Override protected void onInit() {
		view().setLayout(model().getLayout());
	}

}
