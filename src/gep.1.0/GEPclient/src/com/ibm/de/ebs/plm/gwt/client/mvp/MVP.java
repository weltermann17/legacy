package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.logging.Logger;

public final class MVP {

	public static final <M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>> V create(
			final M model, final V view, final P presenter) {
		return MVP.create(model, view, presenter, DefaultEventBus.get());
	}

	public static final <M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>> V create(
			final M model, final V view, final P presenter, final EventBus eventbus) {
		presenter.setModel(model);
		presenter.setEventBus(eventbus);
		return presenter.bind(view);
	}

	public static final <PV extends BaseView, M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>> V createDeferred(
			final PV parent, final M model, final V view, final P presenter) {
		return MVP.createDeferred(parent, model, view, presenter, DefaultEventBus.get());
	}

	public static final <PV extends BaseView, M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>> V createDeferred(
			final PV parent, final M model, final V view, final P presenter, final EventBus eventbus) {
		presenter.setModel(model);
		presenter.setEventBus(eventbus);
		parent.addDeferredBinding(presenter, view);
		return view;
	}

	public static final <M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>, D, E extends BusEvent<D>> V create(
			final M model, final V view, final P presenter, final E event) {
		return MVP.create(model, view, presenter, DefaultEventBus.get(), event);
	}

	public static final <M extends BaseModel, V extends BaseView, P extends BasePresenter<M, V>, D, E extends BusEvent<D>> V create(
			final M model, final V view, final P presenter, final EventBus eventbus, final E event) {
		presenter.setModel(model);
		presenter.setEventBus(eventbus);
		model.onInit(event);
		view.onInit(event);
		return presenter.bind(view);

	}

	public final static Logger getLogger() {
		return MVP.logger;
	}

	static private final Logger logger = Logger.getLogger("MVP");

}
