package de.man.mn.gep.client.workspace.searchresult.menu;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.menu.events.HasItemClickHandlers;

public abstract class MenuPresenter extends BasePresenter<MenuModel, MenuView> {

	public interface View {

		@RequestSender("ItemClicked") HasItemClickHandlers menu();
	}

	@Override protected void onInit() {
		view().setData(model().getDetail(), model().getLayout());
	}

}
