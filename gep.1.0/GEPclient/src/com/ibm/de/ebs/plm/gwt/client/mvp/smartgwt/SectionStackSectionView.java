package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

public abstract class SectionStackSectionView extends CanvasView {

	public SectionStackSectionView() {
		sectionstacksection = new SectionStackSection();
		sectionstacksection.setCanReorder(false);
		sectionstacksection.setCanDropBefore(false);
	}

	public SectionStackSection getSectionStackSection() {
		return sectionstacksection;
	}

	public void setSectionStack(final SectionStack sectionstack) {
		this.sectionstack = sectionstack;
		sectionstack.setCanResizeSections(false);
		sectionstack.setCanReorderSections(false);
		sectionstack.setOverflow(Overflow.HIDDEN);
	}

	public SectionStack getSectionStack() {
		if (null == sectionstack) {
			getLogger().severe(getClass().getName() + " : sectionstack not initialized.");
		}
		return sectionstack;
	}

	private final SectionStackSection sectionstacksection;
	private SectionStack sectionstack;

}
