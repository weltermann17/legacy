package de.man.mn.gep.client.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.TabbedSectionHandler;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.HasCloseClickHandlers;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import de.man.mn.gep.client.shared.event.CloseAllTabs;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.ExportableSelected;
import de.man.mn.gep.client.shared.event.SetSelectedTab;
import de.man.mn.gep.client.shared.event.SignedOnUser;

public abstract class WorkspaceView extends SectionStackSectionView implements WorkspacePresenter.View {

	public WorkspaceView() {
		getSectionStackSection().setID("workspacesection");
		getSectionStackSection().setTitle("Dashboard");
		getSectionStackSection().setExpanded(true);
		getSectionStackSection().setCanCollapse(false);
		createImgButton(horizontalsnapbutton, "[SKIN]headerIcons/arrow_up.png");
		createImgButton(verticalsnapbutton, "[SKIN]headerIcons/arrow_right.png");
		createImgButton(closetab, "[SKIN]headerIcons/close_Over.png");

		closetab.setHoverWrap(false);
		closetab.setPrompt("Close selected tab.");
		closetab.addClickHandler(new ClickHandler() {
			@Override public void onClick(final ClickEvent event) {
				removeTab(workspacetabset.getSelectedTab());
			}
		});

		horizontalsnapbutton.addClickHandler(new ClickHandler() {
			@Override public void onClick(final ClickEvent event) {
				if (visible) {
					horizontalsnapbutton.setSrc("[SKIN]headerIcons/arrow_down.png");
				} else {
					horizontalsnapbutton.setSrc("[SKIN]headerIcons/arrow_up.png");
				}
				visible = !visible;
			}

			private boolean visible = true;
		});
		horizontalsnapbutton.setHoverWrap(false);
		horizontalsnapbutton.setPrompt("Press F11 to toggle 'full-screen'.");
		verticalsnapbutton.addClickHandler(new ClickHandler() {
			@Override public void onClick(final ClickEvent event) {
				if (visible) {
					verticalsnapbutton.setSrc("[SKIN]headerIcons/arrow_left.png");
				} else {
					verticalsnapbutton.setSrc("[SKIN]headerIcons/arrow_right.png");
				}
				visible = !visible;
			}

			private boolean visible = true;
		});
		verticalsnapbutton.setHoverWrap(false);
		verticalsnapbutton.setPrompt("Press F11 to toggle 'full-screen'.");
		workspacetabset = new TabSet();
		workspacetabset.setPaneContainerOverflow(Overflow.HIDDEN);
		workspacetabset.setWidth100();
		workspacetabset.setHeight("*");
		workspacetabset.setTabBarPosition(Side.BOTTOM);
		workspacetabset.setCanCloseTabs(true);
		workspacetabset.setMoreTabCount(6);
		dashboardtab = dashboardTab();
		workspacetabset.addTab(dashboardtab);
		workspacetabset.setExtraSpace(4);
		workspacetabset.addKeyPressHandler(new KeyPressHandler() {
			@Override public void onKeyPress(final KeyPressEvent event) {
				if ("Escape".equals(event.getKeyName())) {
					removeTab(workspacetabset.getSelectedTab());
				}
			}
		});
		workspacetabset.addTabSelectedHandler(new TabSelectedHandler() {
			@Override public void onTabSelected(final TabSelectedEvent event) {
				tabSelected(event.getTab());
				if (event.getTabNum() == 0) {
					final ExportableSelected exportableSelected = GWT.create(ExportableSelected.class);
					exportableSelected.exportable = false;
					exportableSelected.fire();
				}
			}
		});
		toolbar.setPixelSize(180, 25);
		final Canvas spacer = new Canvas();
		spacer.setWidth(32);
		getSectionStackSection().setControls(toolbar, spacer, horizontalsnapbutton, verticalsnapbutton, closetab);
		getSectionStackSection().addItem(workspacetabset);
	}

	@Override public void setSectionStack(final SectionStack sectionstack) {
		super.setSectionStack(sectionstack);
		workspacetabset.addTabSelectedHandler(new TabbedSectionHandler(getSectionStack(), getSectionStackSection()
				.getID()));
	}

	@Override public Canvas asCanvas() {
		return workspacetabset;
	}

	@Override public HasClickHandlers verticalSnap() {
		return verticalsnapbutton;
	}

	@Override public HasClickHandlers horizontalSnap() {
		return horizontalsnapbutton;
	}

	@Override public HasCloseClickHandlers workspacetabset() {
		return workspacetabset;
	}

	@Override public TabSet tabset() {
		return workspacetabset;
	}

	@EventReceiver void closeAllTabs(final CloseAllTabs event) {
		for (final Tab t : workspacetabset.getTabs()) {
			removeTab(t);
		}
	}

	@EventReceiver protected void signedOn(final SignedOnUser event) {
		if (event.isSuccess()) {
			final HLayout hlayout = new HLayout();
			final String div = "<div id='loadingWrapper'><div id='loading'><div class='loadingIndicator'>"
					+ "<img src='/content/gepicon.jpg' width='40' height='40' style='margin-right:8px;float:left;vertical-align:middle;'/>MAN Truck & Bus AG - Global Engineering Platform<br/>"
					+ "<span id='loadingMsg'>Welcome, " + event.getFullName() + "</span></div></div></div>";
			final HTML html = new HTML(div);
			html.setSize("100%", "100%");
			final Img image = new Img();
			image.setSize("100%", "100%");
			image.setSrc("/content/mantruck.png");
			hlayout.addChild(image);
			hlayout.addChild(html);
			workspacetabset.setTabPane(dashboardtab.getID(), hlayout);
		}
	}

	@Override public Tab addTab(final Tab tab, final String title, final String delimiter, final boolean recycle) {
		if (recycle && null != workspacetabset.getTabs()) {
			replaceTab(title);
		}
		tab.setTitle(title);
		workspacetabset.addTab(tab);
		workspacetabset.selectTab(tab);
		return tab;
	}

	public void selectTab(final Tab tab, final String title) {
		workspacetabset.setTabTitle(tab, title);
		if (!tab.equals(workspacetabset.getSelectedTab())) {
			workspacetabset.selectTab(tab);
		}
		getSectionStack().setSectionTitle(getSectionStackSection().getID(), title);
	}

	private void createImgButton(final ImgButton button, final String src) {
		button.setSrc(src);
		button.setSize(16);
		button.setShowFocused(false);
		button.setShowRollOver(false);
		button.setShowDown(false);
	}

	private Tab dashboardTab() {
		final Tab tab = new Tab("Dashboard");
		final HLayout hlayout = new HLayout();
		final String div = "<div id='loadingWrapper'><div id='loading'><div class='loadingIndicator'>"
				+ "<img src='/content/gepicon.jpg' width='40' height='40' style='margin-right:8px;float:left;vertical-align:middle;'/>MAN Truck & Bus AG - Global Engineering Platform<br/>"
				+ "<span id='loadingMsg'>Please sign on with your Windows username and password.</span></div></div></div>";
		final HTML html = new HTML(div);
		html.setSize("100%", "100%");
		hlayout.addChild(html);
		tab.setPane(hlayout);
		tab.setCanClose(false);
		return tab;
	}

	private void removeTab(final Tab tab) {
		if (tab.getCanClose()) {
			workspacetabset.removeTab(tab);
			tabClosed();
		}
	}

	private void replaceTab(final String title) {
		if (null != title && 0 < title.length()) {
			for (final Tab t : workspacetabset.getTabs()) {
				if (t.getCanClose() && t.getTitle().equalsIgnoreCase(title)) {
					removeTab(t);
				}
			}
		}
	}

	private void tabSelected(final Tab tab) {
		for (final Canvas child : toolbar.getChildren()) {
			toolbar.removeChild(child);
		}
		final Canvas child = null == tab.getAttributeAsObject("toolbar") ? null : (Canvas) tab
				.getAttributeAsObject("toolbar");
		if (null != child) {
			toolbar.addChild(child);
		}
		if (null != tab.getAttributeAsObject("detailsselected")) {
			final JSONValueEvent event = (JSONValueEvent) tab.getAttributeAsObject("detailsselected");
			event.refire();
		} else {
			final DetailSelected event = GWT.create(DetailSelected.class);
			event.fire();
		}
	}

	@EventReceiver void setSelectedTab(final SetSelectedTab event) {
		workspacetabset.selectTab(event.index);
	}

	private final TabSet workspacetabset;
	private final Tab dashboardtab;

	private final ImgButton verticalsnapbutton = new ImgButton();
	private final ImgButton horizontalsnapbutton = new ImgButton();
	private final ImgButton closetab = new ImgButton();
	private final Canvas toolbar = new Canvas();
}
