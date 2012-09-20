package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseView implements View {

	public void setPresenter(final BasePresenter<? extends BaseModel, ? extends BaseView> presenter) {
		this.presenter = presenter;
	}

	public BasePresenter<? extends BaseModel, ? extends BaseView> getPresenter() {
		return presenter;
	}

	@Override public <T> T asUIObject() {
		return null;
	}

	abstract protected void viewBindings();

	protected void onInit(final BusEvent<?> event) {
	}

	protected void onInitBusEventHandlers() {
	}

	public void doDeferredBinding() {
		try {
			viewBindings();
			if (null != presenters && null != views) {
				for (int i = 0; i < presenters.size(); ++i) {
					final BasePresenter<? extends BaseModel, ? extends BaseView> presenter = presenters.get(i);
					final BaseView view = views.get(i);
					presenter.bind(view, false);
					BaseView.deferredbindings--;
					if (0 == BaseView.deferredbindings) {
						getLogger().info("All MVPs successfully initialized.");
					}
				}
			}
		} catch (final Exception e) {
			getLogger().severe(getClass().getName() + ".doDeferredBinding() failed : " + e);
		}
	}

	public void addDeferredBinding(final BasePresenter<? extends BaseModel, ? extends BaseView> presenter,
			final BaseView view) {
		if (null == presenters) {
			presenters = new LinkedList<BasePresenter<? extends BaseModel, ? extends BaseView>>();
			views = new LinkedList<BaseView>();
		}
		presenters.add(presenter);
		views.add(view);
		BaseView.deferredbindings++;
	}

	@Override public Logger getLogger() {
		return MVP.getLogger();
	}

	private BasePresenter<? extends BaseModel, ? extends BaseView> presenter;
	private List<BasePresenter<? extends BaseModel, ? extends BaseView>> presenters = null;
	private List<BaseView> views = null;
	static int deferredbindings = 0;
}
