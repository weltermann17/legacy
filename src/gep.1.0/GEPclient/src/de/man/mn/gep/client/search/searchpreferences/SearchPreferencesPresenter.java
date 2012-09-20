package de.man.mn.gep.client.search.searchpreferences;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ChangedGesture;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;

public abstract class SearchPreferencesPresenter extends BasePresenter<SearchPreferencesModel, SearchPreferencesView> {

	interface View {

		@RequestSender("LocationChanged") HasChangedHandlers[] locations();

		@RequestGesture(request = "LocationChanged", gesture = ChangedGesture.class) void locationsChanged(
				final Object[] value);

	}

	@Override protected void onInit() {
		view().initLocations(model().getLocations());
	}

	@Override protected void onBound() {
		view().locationsChanged(model().getSelection());
	}

}
