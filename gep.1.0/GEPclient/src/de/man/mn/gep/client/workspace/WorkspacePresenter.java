package de.man.mn.gep.client.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.TabCloseClickGesture;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.HasCloseClickHandlers;

import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.OpenMillerTree;
import de.man.mn.gep.client.shared.event.OpenSpaceTree;
import de.man.mn.gep.client.shared.event.SearchResult;
import de.man.mn.gep.client.shared.event.TabSetChanged;
import de.man.mn.gep.client.workspace.millertree.MillerTreeModel;
import de.man.mn.gep.client.workspace.millertree.MillerTreePresenter;
import de.man.mn.gep.client.workspace.millertree.MillerTreeView;
import de.man.mn.gep.client.workspace.searchresult.SearchResultModel;
import de.man.mn.gep.client.workspace.searchresult.SearchResultPresenter;
import de.man.mn.gep.client.workspace.searchresult.SearchResultView;
import de.man.mn.gep.client.workspace.spacetree.SpaceTreeModel;
import de.man.mn.gep.client.workspace.spacetree.SpaceTreePresenter;
import de.man.mn.gep.client.workspace.spacetree.SpaceTreeView;

public abstract class WorkspacePresenter extends BasePresenter<WorkspaceModel, WorkspaceView> {

	interface View {

		@RequestSender("HorizontalSnap") HasClickHandlers horizontalSnap();

		@RequestSender("VerticalSnap") HasClickHandlers verticalSnap();

		@RequestSender("CloseTab") HasCloseClickHandlers workspacetabset();

		@RequestGesture(request = "CloseTab", gesture = TabCloseClickGesture.class) void tabClosed();

		Tab addTab(final Tab tab, final String title, final String delimiter, final boolean recycle);

		TabSet tabset();
	}

	@Override protected void onBound() {
		tabCreated();
	}

	@EventReceiver protected void searchResult(final SearchResult event) {
		if (event.isSuccess()) {
			final SearchResultModel model = GWT.create(SearchResultModel.class);
			final SearchResultView view = GWT.create(SearchResultView.class);
			final SearchResultPresenter presenter = GWT.create(SearchResultPresenter.class);
			view().addTab(MVP.create(model, view, presenter, event).asTab(), event.alias, event.delimiter,
					event.recycle);
			tabCreated();

			Scheduler.get().scheduleFinally(new Scheduler.RepeatingCommand() {
				@Override public boolean execute() {
					return view.startEditing(model.getStartEditing(), model.getEditingRecord());
				}
			});
		}
	}

	@EventReceiver protected void openSpaceTree(final OpenSpaceTree event) {
		final SpaceTreeView view = GWT.create(SpaceTreeView.class);
		final SpaceTreeModel model = GWT.create(SpaceTreeModel.class);
		final SpaceTreePresenter presenter = GWT.create(SpaceTreePresenter.class);
		view.setDetailsSelected(lastselecteddetails, true);
		view().addTab(MVP.create(model, view, presenter, event).asTab(), event.displayname, event.delimiter, true);
		tabCreated();
	}

	@EventReceiver protected void openMillerTree(final OpenMillerTree event) {
		if (event.isSuccess()) {
			final MillerTreeView view = GWT.create(MillerTreeView.class);
			final MillerTreeModel model = GWT.create(MillerTreeModel.class);
			final MillerTreePresenter presenter = GWT.create(MillerTreePresenter.class);
			view.setDetailsSelected(lastselecteddetails, true);
			view().addTab(MVP.create(model, view, presenter, event).asTab(), event.displayname, event.delimiter, true);
			tabCreated();
		}
	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		lastselecteddetails = event;
	}

	private void tabCreated() {
		final TabSetChanged tabcreated = GWT.create(TabSetChanged.class);
		tabcreated.tabset = view().tabset();
		tabcreated.fire();
	}

	JSONValueEvent lastselecteddetails;

}
