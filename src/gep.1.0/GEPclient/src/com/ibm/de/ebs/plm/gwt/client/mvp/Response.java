package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Response<D, E extends BusEvent<D>> implements AsyncCallback<D> {

	public interface Intercept<D> {

		boolean onSuccess(final D data);

		void onFailure(final Throwable caught);
	}

	public Response(final E event) {
		this(DefaultEventBus.get(), event);
	}

	public Response(final EventBus eventbus, final E event) {
		this.eventbus = eventbus;
		this.event = event;
	}

	@Override public void onSuccess(final D data) {
		if (null != intercept) {
			if (!intercept.onSuccess(data)) {
				onFailure(new Exception(Response.this.getClass().getName() + ".onSuccess() failed."));
				return;
			}
		}
		if (null != event) {
			event.setData(data);
			eventbus.fireEvent(event);
		}
	}

	@Override public void onFailure(final Throwable caught) {
		if (null != intercept) {
			intercept.onFailure(caught);
		}
		final StringBuilder buf = new StringBuilder();
		buf.append("\n");
		for (final StackTraceElement element : caught.getStackTrace()) {
			buf.append(element).append("\n");
		}
		MVP.getLogger().severe("Response.onFailure() : " + caught.getMessage() + buf);
	}

	public void success() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override public void execute() {
				onSuccess(null);
			}
		});
	}

	public void onFailure(final Object object) {
		onFailure(new Exception(null != object ? object.toString() : "onFailure: null"));
	}

	public E event() {
		return event;
	}

	public void setIntercept(final Intercept<D> intercept) {
		this.intercept = intercept;
	}

	private Intercept<D> intercept;
	private final EventBus eventbus;
	private final E event;

}
