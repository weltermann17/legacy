package de.man.mn.gep.client.search.searchpanel;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ChangedGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.FormItemIconClickGesture;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;
import com.smartgwt.client.widgets.form.fields.events.HasFormItemClickHandlers;

import de.man.mn.gep.client.shared.event.NextSearch;
import de.man.mn.gep.client.shared.event.PreviousSearch;

public abstract class SearchPanelPresenter extends BasePresenter<SearchPanelModel, SearchPanelView> {

	interface View {

		@RequestSender("CriterionChanged") HasChangedHandlers criterion();

		@RequestSender("SearchTypeChanged") HasChangedHandlers searchtype();

		@RequestSender("InputChanged") HasChangedHandlers[] input();

		@RequestSender("Back") HasFormItemClickHandlers back();

		@RequestSender("Next") HasFormItemClickHandlers next();

		@RequestSender("Hint") HasFormItemClickHandlers hint();

		@RequestSender("Search") HasClickHandlers searchbutton();

		@RequestGesture(request = "CriterionChanged", gesture = ChangedGesture.class) void criterionChanged(
				final String criterion);

		@RequestGesture(request = "Back", gesture = FormItemIconClickGesture.class) void doBack();

		@RequestGesture(request = "Next", gesture = FormItemIconClickGesture.class) void doNext();

	}

	@Override protected void onInit() {
		view().setData(model().getDataType(), model().getCriteria(), model().getSearchType(),
				model().getSearchTypeIcons());
	}

	@Override protected void onBound() {
		view().criterionChanged(model().getSelectedCriterion().getName());
	}

	@EventReceiver void previousSearch(final PreviousSearch event) {
		view().doBack();
	}

	@EventReceiver void nextSearch(final NextSearch event) {
		view().doNext();
	}

}
