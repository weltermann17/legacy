package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.smartgwt.client.util.Offline;

public abstract class BaseModel implements Model, Storable {

	@RequestReceiver("Initialize") @EventSender(InitializedModel.class) protected void doInitialize(
			final Request<?> request, final Response<JSONValue, InitializedModel> response) {
		try {
			response.event().modelclass = this.getClass();
			response.event().model = this;
			onInitBusEventHandlers();
			initialize(response);
		} catch (final Exception e) {
			getLogger().severe(getClass().getName() + ".initialize() : " + e);
		}
	}

	protected void initialize(final Response<JSONValue, InitializedModel> response) {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override public void execute() {
				response.success();
			}
		});
	}

	protected void onInit(final BusEvent<?> event) {
	}

	protected void onInit(final JSONValue data) {
	}

	protected void onInitBusEventHandlers() {
	}

	public void setPresenter(final BasePresenter<? extends BaseModel, ? extends BaseView> presenter) {
		this.presenter = presenter;
	}

	public BasePresenter<? extends BaseModel, ? extends BaseView> getPresenter() {
		return presenter;
	}

	@Override public String storageKey() {
		return getClass().getName();
	}

	@Override public void store(final JSONValue data) {
		try {
			Offline.put(storageKey(), data.toString());
		} catch (final Exception e) {
			getLogger().warning("BaseModel.store() : " + e);
		}
	}

	@Override public JSONValue restore() {
		try {
			return JSONParser.parseStrict(Offline.get(storageKey()).toString());
		} catch (final Exception e) {
			return null;
		}
	}

	@Override public void reset() {
		try {
			Offline.remove(storageKey());
		} catch (final Exception e) {
			getLogger().fine("BaseModel.reset() : " + storageKey() + " already deleted.");
		}
	}

	@Override public Logger getLogger() {
		return MVP.getLogger();
	}

	private BasePresenter<? extends BaseModel, ? extends BaseView> presenter;

}
