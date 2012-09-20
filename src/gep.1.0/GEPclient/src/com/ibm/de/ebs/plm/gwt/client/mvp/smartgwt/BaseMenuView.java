package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.ibm.de.ebs.plm.gwt.client.mvp.BaseView;
import com.smartgwt.client.widgets.menu.Menu;

public abstract class BaseMenuView extends BaseView {

	protected BaseMenuView() {
		menu = new MenuWithDestroy();
	}

	@SuppressWarnings("unchecked") @Override public <T> T asUIObject() {
		return (T) asMenu();
	}

	public Menu asMenu() {
		return menu;
	}

	private class MenuWithDestroy extends Menu {
		@Override protected void onDestroy() {
			super.onDestroy();
			getPresenter().unbind();
		}
	}

	private final Menu menu;
}
