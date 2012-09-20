package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

public class SectionStack extends com.smartgwt.client.widgets.layout.SectionStack {

	public void addSection(final SectionStackSectionView view) {
		view.setSectionStack(this);
		super.addSection(view.getSectionStackSection());
	}

}
