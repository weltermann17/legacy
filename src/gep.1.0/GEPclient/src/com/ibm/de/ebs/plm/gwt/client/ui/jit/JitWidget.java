package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;

public abstract class JitWidget extends Widget implements HasClickLabelHandlers, HasRightClickLabelHandlers,
		HasMouseOverLabelHandlers, HasMouseOutLabelHandlers {

	public JitWidget(final String name, final JavaScriptObject config, final int width, final int height) {
		this.name = name;
		this.config = config;
		this.width = width;
		this.height = height;
		final Element domelement = DOM.createElement("div");
		DOM.setElementProperty(domelement, "id", name);
		setElement(domelement);
		backgroundColor = 0 < DOM.getStyleAttribute(domelement, "backgroundColor").length() ? DOM.getStyleAttribute(
				getElement(), "backgroundColor") : JitWidget.defaultBackgroundColor;
		DOM.setStyleAttribute(domelement, "backgroundColor", backgroundColor);
		JitWidget.jitgwtwrappers.put(name, this);
	}

	public void setCallback(final AsyncCallback<Object> callback) {
		this.callback = callback;
	}

	public String getName() {
		return name;
	}

	public JavaScriptObject getCurrentNode() {
		return node;
	}

	public JavaScriptObject getCurrentLabel() {
		return label;
	}

	public AsyncCallback<Object> getCallback() {
		return callback;
	}

	@Override protected void onAttach() {
		setSize(width + "px", height + "px");
		final JavaScriptObject jit = init(config);
		JitWidget.jitwrappedobjects.put(name, jit);
		super.onAttach();
		Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
			@Override public void execute() {
				callback.onSuccess(null);
			}
		});
	}

	@Override public void onDetach() {
		super.onDetach();
		cleanup(name);
		Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
			@Override public void execute() {
				JitWidget.jitwrappedobjects.remove(name);
				JitWidget.jitgwtwrappers.remove(name);
			}
		});
	}

	@Override public void setPixelSize(final int width, final int height) {
		if (width != getOffsetWidth() || height != getOffsetHeight()) {
			super.setPixelSize(width, height);
			resize(name, width, height);
		}
	}

	protected abstract JavaScriptObject init(final JavaScriptObject config);

	protected abstract void loadSubset(final String url);

	protected abstract String getTooltipHtml(final String node);

	private final native void resize(String name, double w, double h)
	/*-{
		try {
			var jit = jitWrappedObject(name);
			if (jit) {
				jit.canvas.resize(w, h);
				jit.refresh();
				jit.controller.onAfterCompute();
			} else {
				console.log('SEVERE: resize() jit null');
				return false;
			}
		} catch (err) {
			console.log('SEVERE: resize() ' + err);
			return false;
		}
	}-*/;

	private native void cleanup(String name)
	/*-{
		try {
			var jit = jitWrappedObject(name);
			if (jit) {
				jit.labels.clearLabels(true);
			} else {
				console.log('SEVERE: cleanup() jit null');
				return false;
			}
		} catch (err) {
			console.log('SEVERE: cleanup() ' + err);
			return false;
		}
	}-*/;

	@Override public HandlerRegistration addClickLabelHandler(final ClickLabelHandler handler) {
		return addHandler(handler, ClickLabelEvent.getType());
	}

	@Override public HandlerRegistration addRightClickLabelHandler(final RightClickLabelHandler handler) {
		return addHandler(handler, RightClickLabelEvent.getType());
	}

	@Override public HandlerRegistration addMouseOverLabelHandler(final MouseOverLabelHandler handler) {
		return addHandler(handler, MouseOverLabelEvent.getType());
	}

	@Override public HandlerRegistration addMouseOutLabelHandler(final MouseOutLabelHandler handler) {
		return addHandler(handler, MouseOutLabelEvent.getType());
	}

	private final void setCurrentNode(final JavaScriptObject node) {
		this.node = node;
	}

	private final void setCurrentLabel(final JavaScriptObject label) {
		this.label = label;
	}

	protected final static JavaScriptObject getWrappedObject(final String name) {
		if (JitWidget.jitwrappedobjects.containsKey(name)) {
			return JitWidget.jitwrappedobjects.get(name);
		} else {
			final StringBuffer buf = new StringBuffer();
			for (final String n : JitWidget.jitwrappedobjects.keySet()) {
				buf.append(n + ", ");
			}
			Dialogs.warn("JitWidget.getWrappedObject() " + name + " not in " + buf);
			return null;
		}
	}

	protected final static String createToolTip(final String name, final String node) {
		if (JitWidget.jitgwtwrappers.containsKey(name)) {
			return JitWidget.jitgwtwrappers.get(name).getTooltipHtml(node);
		} else {
			return null;
		}
	}

	protected final static void forwardEvent(final String name, final JavaScriptObject event,
			final JavaScriptObject label, final JavaScriptObject node) {
		final NativeEvent nativeevent = (NativeEvent) (null == event ? null : event.cast());
		final JitWidget jit = JitWidget.jitgwtwrappers.get(name);
		jit.setCurrentLabel(label);
		jit.setCurrentNode(node);
		final String type = null == nativeevent ? "mouseup" : nativeevent.getType();
		final JSONObject n = new JSONObject(node);
		final String id = n.get("id").isString().stringValue();

		if ("mouseover".equals(type)) {
			MouseOverLabelEvent.fire(jit, nativeevent, id);
		} else if ("mouseout".equalsIgnoreCase(type)) {
			MouseOutLabelEvent.fire(jit, nativeevent, id);
			jit.setCurrentLabel(null);
			jit.setCurrentNode(null);
		} else if ("mouseup".equalsIgnoreCase(type)) {
			ClickLabelEvent.fire(jit, nativeevent, id);
		} else if ("mousewheel".equalsIgnoreCase(type)) {
			nativeevent.preventDefault();
			nativeevent.stopPropagation();
		} else if ("contextmenu".equalsIgnoreCase(type)) {
			RightClickLabelEvent.fire(jit, nativeevent, id);
		} else {
			Dialogs.warn("JitWidget.forwardEvent() : not handled: " + type);
		}
	}

	protected final static void loadSubset(final String name, final String url) {
		if (JitWidget.jitgwtwrappers.containsKey(name)) {
			JitWidget.jitgwtwrappers.get(name).loadSubset(url);
		}
	}

	protected final static JavaScriptObject currentNode(final String name) {
		if (JitWidget.jitgwtwrappers.containsKey(name)) {
			return JitWidget.jitgwtwrappers.get(name).getCurrentNode();
		} else {
			return null;
		}
	}

	private final static native void exportToJavaScript()
	/*-{
		jitWrappedObject = @com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget::getWrappedObject(Ljava/lang/String;);
		jitWrapperForwardEvent = @com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget::forwardEvent(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;);
		jitWrapperLoadSubset = @com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget::loadSubset(Ljava/lang/String;Ljava/lang/String;);
		jitWrapperToolTip = @com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget::createToolTip(Ljava/lang/String;Ljava/lang/String;);
		jitWrapperCurrentNode = @com.ibm.de.ebs.plm.gwt.client.ui.jit.JitWidget::currentNode(Ljava/lang/String;);
	}-*/;

	static {
		JitWidget.exportToJavaScript();
	}

	private final String name;
	private final JavaScriptObject config;
	private final int width;
	private final int height;
	private final String backgroundColor;
	private AsyncCallback<Object> callback;
	private JavaScriptObject node;
	private JavaScriptObject label;
	private static final String defaultBackgroundColor = "#ffffff";
	private static final Map<String, JavaScriptObject> jitwrappedobjects = new LinkedHashMap<String, JavaScriptObject>();
	private static final Map<String, JitWidget> jitgwtwrappers = new LinkedHashMap<String, JitWidget>();
}
