package com.ibm.de.ebs.plm.gwt.client.ui.jit;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;

public abstract class SpaceTreeCanvas extends Canvas implements HasClickLabelHandlers, HasRightClickLabelHandlers,
		HasMouseOverLabelHandlers, HasMouseOutLabelHandlers {

	public SpaceTreeCanvas(final int width, final int height, final double dx, final double dy) {
		this.dx = dx;
		this.dy = dy;
		final int x = -32 + (int) (width * dx);
		final int y = -32 + (int) (height * dx);
		setOverflow(Overflow.SCROLL);
		final String name = "STC_" + ++SpaceTreeCanvas.counter;
		spacetree = new SpaceTree(name, getConfig(name, 0), x, y) {
			@Override protected String getTooltipHtml(final String node) {
				return SpaceTreeCanvas.this.getTooltipHtml(node);
			}

			@Override protected void onLoadSubset(final JSONValue data) {
				SpaceTreeCanvas.this.onLoadSubset(data);
			}
		};
		addChild(spacetree);
	}

	public void setData(final JavaScriptObject jsondata, final String nodeid, final int width, final int height) {
		spacetree.setCallback(new AsyncCallback<Object>() {

			@Override public void onFailure(final Throwable caught) {
			}

			@Override public void onSuccess(final Object result) {
				initJitWidget(getName(), jsondata);
				Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
					@Override public boolean execute() {
						if (0 < retries--) {
							return display(getName(), nodeid, 0);
						} else {
							Dialogs.error("Display problem: Please drag the mouse after closing this dialog to make the SpaceTree visible.");
							return false;
						}
					}

					private int retries = 100;
				});
				addResizedHandler(new ResizedHandler() {
					@Override public void onResized(final ResizedEvent event) {
						if (oldx != getWidth() || oldy != getHeight()) {
							spacetree.setPixelSize(getWidth(), getHeight());
						}
						oldx = getWidth();
						oldy = getHeight();
						scrollToPercent(60, 50);
					}
				});
				scrollToPercent(60, 50);
			}
		});
		resizeTo(getWidth(), getHeight());
	}

	protected abstract String getTooltipHtml(final String node);

	protected abstract void onLoadSubset(JSONValue data);

	public String getName() {
		return spacetree.getName();
	}

	public JavaScriptObject getCurrentNode() {
		return spacetree.getCurrentNode();
	}

	public JavaScriptObject getCurrentLabel() {
		return spacetree.getCurrentLabel();
	}

	@Override protected void onDestroy() {
		spacetree.removeFromParent();
		super.onDestroy();
	}

	@Override public Integer getWidth() {
		return new Integer(-32 + (int) (super.getWidth() * dx));
	}

	@Override public Integer getHeight() {
		return new Integer(-32 + (int) (super.getHeight() * dy));
	}

	@Override public HandlerRegistration addClickLabelHandler(final ClickLabelHandler handler) {
		return spacetree.addClickLabelHandler(handler);
	}

	@Override public HandlerRegistration addRightClickLabelHandler(final RightClickLabelHandler handler) {
		return spacetree.addRightClickLabelHandler(handler);
	}

	@Override public HandlerRegistration addMouseOverLabelHandler(final MouseOverLabelHandler handler) {
		return spacetree.addMouseOverLabelHandler(handler);
	}

	@Override public HandlerRegistration addMouseOutLabelHandler(final MouseOutLabelHandler handler) {
		return spacetree.addMouseOutLabelHandler(handler);
	}

	private native JavaScriptObject getConfig(final String name, final int offsetx)
	/*-{
		return {
		injectInto: name,
		multitree: true,
		duration: -1,
		fps: -1,
		transition: $jit.Trans.linear,
		levelDistance: 38,
		levelsToShow: 11,
		offsetX: offsetx,
		Navigation: { enable: true, panning: 'avoid nodes', zooming: 32 },  		
		Node: {
		width: 160,
		align: 'left',
		type: 'rectangle',
		color: '#6faed0',
		overridable: true },
		Edge: {
		type: 'bezier',
		overridable: true },
		Tips: {  
		enable: true,  
		type: 'auto',  
		offsetX: 10,  
		offsetY: 10,  
		onShow: function(tip, node) { 
		tip.innerHTML = jitWrapperToolTip(name, node.name); }},
		Events: {
		enable: true,
		type: 'auto',
		onClick: function(node, eventInfo, e) { 
			node = jitWrapperCurrentNode(name);
			if (null != node) if (jitWrappedObject(name)) {  
			if (node.data.isassembly && !node.data.firstlevelcomplete) {
				jitWrapperLoadSubset(name, node.data.subtree); 
				node.data.firstlevelcomplete = true; }
			jitWrappedObject(name).onClick(node.id); jitWrapperForwardEvent(name, e || $wnd.event, null, node); } },
		onMouseEnter: function(node, eventInfo, e) { if (node) if (!node.selected) if (jitWrappedObject(name)) jitWrappedObject(name).addNodeInPath(node.id); jitWrapperForwardEvent(name, e || $wnd.event, null, node); },
		onMouseLeave: function(node, eventInfo, e) { if (node) if (jitWrappedObject(name)) jitWrappedObject(name).clearNodesInPath(); jitWrapperForwardEvent(name, e || $wnd.event, null, node); },
		onMouseWheel: function(node, eventInfo, e) { if (node) if (jitWrappedObject(name)) jitWrapperForwardEvent(name, e || $wnd.event, null, node); },
		onRightClick: function(node, eventInfo, e) { if (node) if (jitWrappedObject(name)) jitWrapperForwardEvent(name, e || $wnd.event, null, node); }
		},
		onCreateLabel: function(label, node) {
		label.id = node.id;
		label.innerHTML = node.data.description; 
		var style = label.style;
		style.display = 'table-cell';
		style.overflow = 'hidden';
		style.wordWrap = 'break-word';
		style.verticalAlign = 'top';
		style.textAlign = 'left';
		style.width = '152px';
		style.height = node.data.$height - 4;
		style.cursor = 'pointer';
		style.color = '#202020';
		style.paddingLeft = '4px';
		style.paddingRight = '4px';
		style.paddingTop = '2px';
		style.paddingBottom = '2px';
		style.fontSize = '11px';
		style.fontFamily = 'Arial "Bitstream Vera Sans" sans-serif';
		},
		onBeforePlotNode: function(node){
		if (node.id == jitWrappedObject(name).root) {
		node.data.$color = '#bcbcbc';
		} else if (node.selected) {
		node.data.$color = '#f0d700';
		} else if ('product' == node.data.type) {
		node.data.$color = '#00d7a0';
		} else if (node.data.isparent) {
		node.data.$color = '#dceedc';
		} else if (node.data.isassembly) {
		node.data.$color = '#dcdcee';
		} else {
		delete node.data.$color;
		}
		},
		onBeforePlotLine: function(adj){
		if (adj.nodeFrom.selected && adj.nodeTo.selected) {
		adj.data.$color = '#44d';
		adj.data.$lineWidth = 1.2;
		} else {
		delete adj.data.$color;
		delete adj.data.$lineWidth;
		}
		},
		}
	}-*/;

	private native void initJitWidget(String name, JavaScriptObject jsondata)
	/*-{
		try {
			var jit = jitWrappedObject(name);
			if (jit) {
				jit.loadJSON(jsondata);
				jit.compute();
			} else {
				console.log('SEVERE: setData() jit null');
				return false;
			}
		} catch (err) {
			console.log('SEVERE: setData() ' + err);
			return false;
		}
	}-*/;

	private native boolean display(String name, String nodeid, int offsetx)
	/*-{
		try {
			var jit = jitWrappedObject(name);
			if (jit) {
				jit.onClick(null == nodeid ? jit.root : nodeid, {
					Move : {
						offsetX : offsetx
					}
				});
				console.log('INFO: Successfully displayed Spacetree: ' + name);
			} else {
				console.log('SEVERE: display() jit null');
				return true;
			}
		} catch (err) {
			console.log('SEVERE: display()' + err);
			return true;
		}
		return false;
	}-*/;

	private SpaceTree spacetree = null;
	private final double dx;
	private final double dy;
	private int oldx;
	private int oldy;
	private static int counter = 0;
}
