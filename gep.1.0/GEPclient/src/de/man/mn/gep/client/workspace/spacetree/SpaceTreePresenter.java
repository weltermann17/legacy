package de.man.mn.gep.client.workspace.spacetree;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasClickLabelHandlers;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasMouseOutLabelHandlers;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasMouseOverLabelHandlers;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

public abstract class SpaceTreePresenter extends BasePresenter<SpaceTreeModel, SpaceTreeView> {

	public interface View {

		@RequestSender("DetailOver") HasMouseOverLabelHandlers mouseOver();

		@RequestSender("DetailOut") HasMouseOutLabelHandlers mouseOut();

		@RequestSender("DetailSelected") HasClickLabelHandlers labelClicked();

		@RequestSender("ExportableSelected") HasTabSelectedHandlers exportableSelected();
	}

}
