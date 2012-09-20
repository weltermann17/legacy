package de.man.mn.gep.client.main.menu;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.widgets.menu.events.HasClickHandlers;
import com.smartgwt.client.widgets.menu.events.HasItemClickHandlers;

public abstract class MenuPresenter extends BasePresenter<MenuModel, MenuView> {

	interface View {

		@RequestSender("Restart") HasClickHandlers restart();

		@RequestSender("SignOn") HasClickHandlers signon();

		@RequestSender("SignOff") HasClickHandlers signoff();

		@RequestSender("Exit") HasClickHandlers exit();

		@RequestSender("About") HasClickHandlers about();

		@RequestSender("Fullscreen") HasClickHandlers fullscreen();

		@RequestSender("ZoomIn") HasClickHandlers zoomin();

		@RequestSender("ZoomOut") HasClickHandlers zoomout();

		@RequestSender("ZoomReset") HasClickHandlers zoomreset();

		@RequestSender("CloseAllTabs") HasClickHandlers closealltabs();

		@RequestSender("RecycleTabs") HasClickHandlers recycletabs();

		@RequestSender("RecentlyAccessedParts") HasClickHandlers recentlyaccessedparts();

		@RequestSender("RecentlyAccessedProducts") HasClickHandlers recentlyaccessedproducts();

		@RequestSender("PreviousSearch") HasClickHandlers previoussearch();

		@RequestSender("NextSearch") HasClickHandlers nextsearch();

		@RequestSender("ResetHistory") HasClickHandlers resethistory();

		@RequestSender("TabSelected") HasItemClickHandlers tabSelected();

		@RequestSender("SaveAs") HasClickHandlers saveas();

		@RequestSender("ChangeSearchType") HasClickHandlers[] changeSearchType();
	}

	@Override protected void onBound() {
		view().showMenu();
	}

}
