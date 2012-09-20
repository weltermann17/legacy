package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.DefaultEventBus;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;

public abstract class SpaceTree extends JitWidget {

	public SpaceTree(final String name, final JavaScriptObject config, final int width, final int height) {
		super(name, config, width, height);
	}

	abstract protected void onLoadSubset(final JSONValue data);

	@Override protected native JavaScriptObject init(final JavaScriptObject config)
	/*-{
		var st = null;
		try {
			st = new $jit.ST(config);
		} catch (err) {
			console.log('SEVERE: init() ' + err);
		}
		return st;
	}-*/;

	@Override protected void loadSubset(final String url) {
		final Response<JSONValue, BusEvent<JSONValue>> response = new Response<JSONValue, BusEvent<JSONValue>>(
				DefaultEventBus.get(), null) {
			@Override public void onSuccess(final JSONValue data) {
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override public void execute() {
						final int alllevels = (int) data.isObject().get("data").isObject().get("alllevels").isNumber()
								.doubleValue();
						if (0 < alllevels) {
							addSubtree(getName(), data.isObject().getJavaScriptObject());
							SpaceTree.this.onLoadSubset(data);
						}
					}
				});
			}
		};

		JsonResource.get(url, response, true);
	}

	private native void addSubtree(final String name, final JavaScriptObject subtree)
	/*-{
		try {
			var jit = jitWrappedObject(name);
			if (jit) {
				jit.addSubtree(subtree, 'replot');
			} else {
				console.log('SEVERE: addSubtree() jit null');
				return false;
			}
		} catch (err) {
			console.log('SEVERE: addSubtree() ' + err);
			return false;
		}
	}-*/;

}
