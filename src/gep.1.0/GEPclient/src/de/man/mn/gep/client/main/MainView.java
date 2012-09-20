package de.man.mn.gep.client.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.ibm.de.ebs.plm.gwt.client.event.ZoomOut;
import com.ibm.de.ebs.plm.gwt.client.event.ZoomOver;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStack;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.TabbedSectionHandler;
import com.ibm.de.ebs.plm.gwt.client.util.StringUtil;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import de.man.mn.gep.client.details.DetailsModel;
import de.man.mn.gep.client.details.DetailsPresenter;
import de.man.mn.gep.client.details.DetailsView;
import de.man.mn.gep.client.details.documents.DocumentsModel;
import de.man.mn.gep.client.details.documents.DocumentsPresenter;
import de.man.mn.gep.client.details.documents.DocumentsView;
import de.man.mn.gep.client.details.preview.PreviewModel;
import de.man.mn.gep.client.details.preview.PreviewPresenter;
import de.man.mn.gep.client.details.preview.PreviewView;
import de.man.mn.gep.client.main.menu.MenuModel;
import de.man.mn.gep.client.main.menu.MenuPresenter;
import de.man.mn.gep.client.main.menu.MenuView;
import de.man.mn.gep.client.main.signon.SignOnModel;
import de.man.mn.gep.client.main.signon.SignOnPresenter;
import de.man.mn.gep.client.main.signon.SignOnView;
import de.man.mn.gep.client.search.searchpanel.SearchPanelModel;
import de.man.mn.gep.client.search.searchpanel.SearchPanelPresenter;
import de.man.mn.gep.client.search.searchpanel.SearchPanelView;
import de.man.mn.gep.client.search.searchpanel.SetSelectedSearchTab;
import de.man.mn.gep.client.search.searchpreferences.SearchPreferencesModel;
import de.man.mn.gep.client.search.searchpreferences.SearchPreferencesPresenter;
import de.man.mn.gep.client.search.searchpreferences.SearchPreferencesView;
import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Location;
import de.man.mn.gep.client.shared.event.LocationsChanged;
import de.man.mn.gep.client.shared.event.Snap;
import de.man.mn.gep.client.workspace.WorkspaceModel;
import de.man.mn.gep.client.workspace.WorkspacePresenter;
import de.man.mn.gep.client.workspace.WorkspaceView;

public abstract class MainView extends CanvasView implements MainPresenter.View {

	public MainView() {
		mainlayout = new VLayout();
		mainlayout.setSize("100%", "100%");
		mainlayout.addMember(applicationMenu());
		stacks = new HLayout();
		stacks.setMargin(5);
		stacks.setMembersMargin(5);
		stacks.setSize("100%", "100%");
		stacks.addMember(leftStack());
		stacks.addMember(rightStack());
		mainlayout.addMember(stacks);
		canvas = asCanvas();
		canvas.setSize("100%", "100%");
		canvas.addChild(mainlayout);
	}

	@EventReceiver protected void zoomOver(final ZoomOver event) {
		beforezoom = workspacesection.getSectionHeader().getTitle();
		leftstack.setSectionTitle(workspacesection.getID(), event.getName());
	}

	@EventReceiver protected void zoomOut(final ZoomOut event) {
		if (null != beforezoom && 0 < beforezoom.length()) {
			leftstack.setSectionTitle(workspacesection.getID(), beforezoom);
			beforezoom = "";
		}
	}

	@EventReceiver protected void selectedLocations(final LocationsChanged event) {
		final Location[] selectedlocations = event.selectedlocations;
		final int count = selectedlocations.length;
		final StringBuilder buf = new StringBuilder();
		if (0 < count) {
			buf.append("  :  ");
			int i = 0;
			for (final Location location : selectedlocations) {
				if (0 < i++) {
					buf.append(", ");
				}
				buf.append(location.shortname);
			}
		}
		final String message = "Search in " + StringUtil.convertNumberToWord(count) + " location"
				+ (1 == count ? "" : "s") + buf.toString();
		if (searchtabset.getSelectedTab().equals(preferencestab)) {
			leftstack.setSectionTitle(searchsection.getID(), message);
		}
		searchtabset.setTabTitle(preferencestab, message);
	}

	@EventReceiver protected void setSelectedSearchTab(final SetSelectedSearchTab event) {
		searchtabset.selectTab(event.index);
	}

	@EventReceiver protected void snap(final Snap event) {
		if (event.horizontal) {
			if (searchsection.getAttributeAsBoolean("visible")) {
				leftstack.hideSection(searchsection.getID());
				searchsection.setAttribute("visible", false);
			} else {
				leftstack.showSection(searchsection.getID());
				searchsection.setAttribute("visible", true);
			}
		} else if (event.vertical) {
			if (rightstack.isVisible()) {
				rightstack.hide();
			} else {
				rightstack.show();
			}
		}
	}

	private Canvas applicationMenu() {
		final MenuModel model = GWT.create(MenuModel.class);
		final MenuView view = GWT.create(MenuView.class);
		final MenuPresenter presenter = GWT.create(MenuPresenter.class);
		return MVP.createDeferred(this, model, view, presenter).asCanvas();
	}

	private SectionStack leftStack() {
		leftstack = new SectionStack();
		leftstack.setVisibilityMode(VisibilityMode.MULTIPLE);
		leftstack.setCanResizeSections(false);
		leftstack.setWidth("*");
		leftstack.setHeight100();
		leftstack.addSection(searchSection());
		workspacesection = workspaceSection();
		leftstack.addSection(workspacesection);
		return leftstack;
	}

	private SectionStackSection searchSection() {
		searchsection = new SectionStackSection();
		searchsection.setID("searchsection");
		searchsection.setTitle("Search");
		searchsection.setExpanded(true);
		searchsection.setCanCollapse(false);
		searchsection.setAttribute("visible", true);
		final VLayout vlayout = new VLayout();
		vlayout.setWidth100();
		vlayout.setHeight(100);
		searchtabset = new TabSet();
		searchtabset.setTabBarPosition(Side.BOTTOM);
		searchtabset.setWidth100();
		searchtabset.setWidth100();
		searchtabset.setHeight(vlayout.getHeight() - 3);
		searchtabset.addTab(createSearchTab(DataType.versions));
		searchtabset.addTab(preferencesTab());
		searchtabset.addTabSelectedHandler(new TabbedSectionHandler(leftstack, searchsection.getID()));
		vlayout.addChild(searchtabset);
		searchsection.addItem(vlayout);
		return searchsection;
	}

	private Tab createSearchTab(final DataType datatype) {
		final Tab tab = new Tab("searchs" + datatype.name());
		tab.setTitle("Search");
		final SearchPanelModel model = GWT.create(SearchPanelModel.class);
		final SearchPanelView view = GWT.create(SearchPanelView.class);
		final SearchPanelPresenter presenter = GWT.create(SearchPanelPresenter.class);
		model.setDataType(datatype);
		tab.setPane(MVP.createDeferred(this, model, view, presenter).asCanvas());
		return tab;
	}

	private Tab preferencesTab() {
		preferencestab = new Tab("preferencestab");
		preferencestab.setTitle("");
		final SearchPreferencesModel model = GWT.create(SearchPreferencesModel.class);
		final SearchPreferencesView view = GWT.create(SearchPreferencesView.class);
		final SearchPreferencesPresenter presenter = GWT.create(SearchPreferencesPresenter.class);
		preferencestab.setPane(MVP.createDeferred(this, model, view, presenter).asCanvas());
		return preferencestab;
	}

	private SectionStackSection workspaceSection() {
		final WorkspaceModel model = GWT.create(WorkspaceModel.class);
		final WorkspacePresenter presenter = GWT.create(WorkspacePresenter.class);
		workspaceview = GWT.create(WorkspaceView.class);
		MVP.createDeferred(this, model, workspaceview, presenter);
		workspaceview.setSectionStack(leftstack);
		zoomover = workspaceview.asCanvas();
		return workspaceview.getSectionStackSection();
	}

	private SectionStack rightStack() {
		rightstack = new SectionStack();
		rightstack.setVisibilityMode(VisibilityMode.MULTIPLE);
		rightstack.setCanResizeSections(true);
		rightstack.setWidth(302);
		rightstack.setHeight100();
		rightstack.addSection(signonSection());
		rightstack.addSection(detailSection());
		rightstack.addSection(formatsSection());
		rightstack.addSection(previewSection());
		return rightstack;
	}

	private SectionStackSectionView signonSection() {
		final SignOnModel model = GWT.create(SignOnModel.class);
		final SignOnView view = GWT.create(SignOnView.class);
		final SignOnPresenter presenter = GWT.create(SignOnPresenter.class);
		return MVP.createDeferred(this, model, view, presenter);
	}

	private SectionStackSectionView previewSection() {
		final PreviewModel model = GWT.create(PreviewModel.class);
		final PreviewView view = GWT.create(PreviewView.class);
		final PreviewPresenter presenter = GWT.create(PreviewPresenter.class);
		view.zoomOver(zoomover);
		view.setSectionStack(rightstack);
		return MVP.createDeferred(this, model, view, presenter);
	}

	private SectionStackSectionView formatsSection() {
		final DocumentsModel model = GWT.create(DocumentsModel.class);
		final DocumentsView view = GWT.create(DocumentsView.class);
		final DocumentsPresenter presenter = GWT.create(DocumentsPresenter.class);
		view.setSectionStack(rightstack);
		return MVP.createDeferred(this, model, view, presenter);
	}

	private SectionStackSectionView detailSection() {
		final DetailsModel model = GWT.create(DetailsModel.class);
		final DetailsView view = GWT.create(DetailsView.class);
		final DetailsPresenter presenter = GWT.create(DetailsPresenter.class);
		return MVP.createDeferred(this, model, view, presenter);
	}

	private final VLayout mainlayout;
	private final HLayout stacks;
	private SectionStack leftstack;
	private SectionStackSection searchsection;
	private SectionStackSection workspacesection;
	private WorkspaceView workspaceview;
	private TabSet searchtabset;
	private Tab preferencestab;
	private SectionStack rightstack;
	private Widget zoomover;
	private String beforezoom;
	private final Canvas canvas;

}
