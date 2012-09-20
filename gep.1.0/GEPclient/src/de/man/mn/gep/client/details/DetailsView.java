package de.man.mn.gep.client.details;

import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.TabbedSectionHandler;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.HasCloseClickHandlers;

public abstract class DetailsView extends SectionStackSectionView implements DetailsPresenter.View {

	public DetailsView() {
		getSectionStackSection().setID("detailsection");
		getSectionStackSection().setTitle("Details");
		getSectionStackSection().setExpanded(true);
		getSectionStackSection().setCanCollapse(false);
		detailtabset = new TabSet();
		detailtabset.setDestroyPanes(true);
		detailtabset.setPaneContainerOverflow(Overflow.HIDDEN);
		detailtabset.setWidth100();
		detailtabset.setHeight("*");
		detailtabset.setTabBarAlign(Side.LEFT);
		detailtabset.setTabBarPosition(Side.BOTTOM);
		versiontab = new Tab("Details");
		versiontab.setID("versiontab");
		versiontab.setCanClose(false);
		final VLayout vlayout = new VLayout();
		vlayout.setPadding(4);
		vlayout.setSize("100%", "100%");
		versionpane = new Canvas();
		versionpane.addChild(vlayout);
		versiontab.setPane(versionpane);
		detailtabset.addTab(versiontab);
		detailtabset.setExtraSpace(3);
		detailtabset.addKeyPressHandler(new KeyPressHandler() {
			@Override public void onKeyPress(final KeyPressEvent event) {
				if ("Escape".equals(event.getKeyName())) {
					closeTab();
					userClosed();
				}
			}
		});
		getSectionStackSection().addItem(detailtabset);
	}

	@Override public void setSectionStack(final SectionStack sectionstack) {
		super.setSectionStack(sectionstack);
		detailtabset
				.addTabSelectedHandler(new TabbedSectionHandler(getSectionStack(), getSectionStackSection().getID()));
	}

	@Override public HasCloseClickHandlers tabset() {
		return detailtabset;
	}

	@Override public void setUser(final Tab user) {
		final String prefix = "User: ";
		for (final Tab t : detailtabset.getTabs()) {
			final String ctitle = t.getTitle();
			if (null != ctitle && ctitle.startsWith(prefix)) {
				detailtabset.removeTab(t);
			}
		}
		detailtabset.addTab(user);
		detailtabset.selectTab(user);
	}

	@Override public void setDetails(final Canvas details, final String name) {
		final Canvas pane = detailtabset.getTab(versiontab.getID()).getPane();
		detailtabset.updateTab(versiontab, details);
		pane.destroy();
		detailtabset.setTabTitle(versiontab, name);
		getSectionStack().setSectionTitle(getSectionStackSection().getID(), name);
		detailtabset.selectTab(versiontab);
	}

	public void closeAllTabs() {
		while (closeTab()) {
		}
	}

	private boolean closeTab() {
		if (detailtabset.getSelectedTab().getCanClose()) {
			detailtabset.removeTab(detailtabset.getSelectedTab());
			return true;
		} else {
			final Canvas pane = detailtabset.getTab(versiontab.getID()).getPane();
			detailtabset.updateTab(detailtabset.getSelectedTab(), versionpane);
			pane.destroy();
			detailtabset.setTabTitle(versiontab, "Details");
			getSectionStack().setSectionTitle(getSectionStackSection().getID(), "Details");
			return false;
		}
	}

	private final TabSet detailtabset;
	private final Tab versiontab;
	private final Canvas versionpane;

}
