package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;

public abstract class BasePresenter<M extends BaseModel, V extends BaseView> implements Presenter<M, V> {

	void setModel(final M model) {
		this.model = model;
		this.model.setPresenter(this);
	}

	void setEventBus(final EventBus eventbus) {
		this.eventbus = eventbus;
	}

	protected void onInit() {
	}

	protected void onBindBusEventHandlers() {
	}

	protected void onBind() {
	}

	protected void onUnbind() {
	}

	protected void onBound() {
	}

	@Override public V bind(final V view) {
		this.view = view;
		try {
			view.setPresenter(this);
			view.onInitBusEventHandlers();
			onBindBusEventHandlers();
			onBind();
			initializeModel();
		} catch (final Exception e) {
			getLogger().severe(getClass().getName().replace("Impl", "") + ".bind() : " + e);
		}
		return view;
	}

	@SuppressWarnings("unchecked") public V bind(final BaseView view, final boolean dummy) {
		return bind((V) view);
	}

	@Override public void unbind() {
		try {
			onUnbind();
			for (final HandlerRegistration registration : registrations) {
				registration.removeHandler();
			}
			final String viewname = view.getClass().getName().replace("Impl", "");
			view.setPresenter(null);
			view = null;
			model.setPresenter(null);
			model = null;
			getLogger().info("MVP finalized : " + viewname);
		} catch (final Exception e) {
			getLogger().severe(getClass().getName().replace("Impl", "") + ".unbind() : " + e);
		}
	}

	@Override public V view() {
		return view;
	}

	@Override public M model() {
		return model;
	}

	@Override public EventBus eventBus() {
		return eventbus;
	}

	public <D> void addBusEventHandler(final GwtEvent.Type<BusEventHandler<D>> eventtype,
			final BusEventHandler<D> handler) {
		if (null != handler) {
			final HandlerRegistration registration = eventbus.addHandler(eventtype, handler);
			if (null != registration) {
				registrations.add(registration);
			}
		}
	}

	@Override public Logger getLogger() {
		return MVP.getLogger();
	}

	@RequestSender("Initialize") abstract protected void initializeModel();

	@EventReceiver protected void initialized(final InitializedModel event) {
		try {
			if (event.model == model) {
				model.onInit(event.getData());
				onInit();
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override public void execute() {
						view.doDeferredBinding();
						onBound();
						getLogger().info("MVP initialized : " + view.getClass().getName().replace("Impl", ""));
					}
				});
			}
		} catch (final Exception e) {
			getLogger().severe(getClass().getName().replace("Impl", "") + ".initialized() failed : " + e);
		}
	}

	protected BasePresenter<? extends BaseModel, ? extends BaseView> getPresenter() {
		return this;
	}

	private V view;
	private M model;
	private EventBus eventbus;
	private final List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

}
