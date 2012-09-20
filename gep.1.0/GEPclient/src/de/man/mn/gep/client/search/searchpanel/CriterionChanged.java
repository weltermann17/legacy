package de.man.mn.gep.client.search.searchpanel;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

abstract class CriterionChanged extends BusEvent<Criterion> {
	public Criterion selectedCriterion;
	public boolean refreshSearch = false;
}
