package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class TabbedSectionHandler implements TabSelectedHandler {

	public TabbedSectionHandler(final SectionStack sectionstack, final String id) {
		this.sectionstack = sectionstack;
		this.id = id;
	}

	@Override public void onTabSelected(final TabSelectedEvent event) {
		selectTab(event.getTab());
	}

	public void selectTab(final Tab tab) {
		sectionstack.setSectionTitle(id, tab.getTitle());
	}

	private final SectionStack sectionstack;
	private final String id;
}
