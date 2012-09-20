package com.ibm.de.ebs.plm.gwt.client.ui.smartgwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.ibm.de.ebs.plm.gwt.client.event.ZoomOut;
import com.ibm.de.ebs.plm.gwt.client.event.ZoomOver;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.ibm.de.ebs.plm.gwt.client.util.DomUtil;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.KeyDownEvent;
import com.smartgwt.client.widgets.events.KeyDownHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;

public class ImageWithZoom extends Canvas implements MouseOverHandler, MouseOutHandler, KeyDownHandler, ClickHandler,
		LoadHandler, ErrorHandler {

	public ImageWithZoom(final boolean scalewidth, final int maxx, final int maxy) {
		this.scalewidth = scalewidth;
		this.maxx = maxx;
		this.maxy = maxy;
		setPixelSize(maxx, maxy);
		setOverflow(Overflow.HIDDEN);
		image = new Image();
		image.setUrl("");
		image.setPixelSize(maxx, maxy);
		image.addLoadHandler(this);
		image.addErrorHandler(this);
		image.setVisible(false);
		addChild(image);
		addMouseOverHandler(this);
		addMouseOutHandler(this);
		addKeyDownHandler(this);
		addClickHandler(this);
		DomUtil.preventDefaultContextMenu(image.getElement());
	}

	public void setUrl(final String url) {
		if (!this.url.equals(url)) {
			this.url = url;
			originalX = 0;
			originalY = 0;
			loaded = false;
			image.setVisible(false);
			image.setUrl(null == url ? "" : url);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setZoomOver(final Widget zoomover) {
		if (null != zoomover) {
			this.zoomover = zoomover;
			zoompanel = new PopupPanel(true);
			DOM.setStyleAttribute(zoompanel.getElement(), "zIndex", "1E9");
		}
	}

	public void setDelay(final int delay) {
		this.delay = delay;
	}

	public boolean isLoaded() {
		return loaded;
	}

	@Override public void onMouseOver(final MouseOverEvent event) {
		if (loaded && !zoomed) {
			timer = new Timer() {
				@Override public void run() {
					timer = null;
					zoom();
				}
			};
			timer.schedule(delay);
		}
	}

	@Override public void onMouseOut(final MouseOutEvent event) {
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		zoomOut();
	}

	@Override public void onKeyDown(final KeyDownEvent event) {
		onMouseOut(null);
		event.cancel();
	}

	@Override public void onClick(final ClickEvent event) {
		onMouseOut(null);
		event.cancel();
	}

	@Override public void onError(final ErrorEvent event) {
		setUrl("");
	}

	@Override public void onLoad(final LoadEvent event) {
		originalX = image.getWidth();
		originalY = image.getHeight();
		if (0 == originalX || 0 == originalY) {
			return;
		}
		final int maxxy = Math.max(maxx, maxy);
		final double x = maxx;
		final double y = scalewidth ? maxy : x;
		final double w = originalX;
		final double h = originalY;
		final double dx = Math.min(x / w, y / h);
		final int width = Math.min((int) Math.floor(w * dx), maxxy);
		final int height = Math.min((int) Math.floor(h * dx), maxxy);
		image.setPixelSize(width, height);
		image.setVisible(true);
		setPixelSize(width, height);
		loaded = true;
	}

	private void zoom() {
		zoomed = true;
		final double x = zoomover.getOffsetWidth() - 4;
		final double y = zoomover.getOffsetHeight() - 6;
		zoompanel.setPopupPosition(zoomover.getAbsoluteLeft() + 2, zoomover.getAbsoluteTop() + 3);
		zoompanel.setPixelSize((int) x, (int) y);
		final double w = originalX;
		final double h = originalY;
		final double dx = Math.min(x / w, y / h);
		zoomimage = new Image();
		zoomimage.setVisible(false);
		zoomimage.addLoadHandler(new LoadHandler() {
			@Override public void onLoad(final LoadEvent event) {
				if (!zoompanel.isVisible()) {
					zoomimage.setVisible(false);
					zoomimage = null;
				} else {
					zoomimage.setPixelSize((int) Math.floor(w * dx), (int) Math.floor(h * dx));
					zoomimage.setVisible(true);
				}
			}
		});
		zoomimage.addErrorHandler(new ErrorHandler() {
			@Override public void onError(final ErrorEvent event) {
				zoomimage.setVisible(false);
				zoomimage = null;
			}
		});
		zoomimage.setUrl(url);
		zoomover.setVisible(false);
		zoompanel.setWidget(zoomimage);
		zoompanel.show();
		zoomOver();
	}

	private void zoomOver() {
		if (null != zoomover) {
			final ZoomOver event = GWT.create(ZoomOver.class);
			event.imageurl = url;
			event.fire();
			image.setVisible(false);
		}
	}

	private void zoomOut() {
		if (zoomed && null != zoomover) {
			image.setVisible(true);
			zoompanel.hide();
			zoompanel.remove(zoomimage);
			zoomimage = null;
			zoomover.setVisible(true);
			final ZoomOut event = GWT.create(ZoomOut.class);
			event.fire();
			zoomed = false;
		}
	}

	private final boolean scalewidth;
	private final Image image;
	private Image zoomimage;
	private PopupPanel zoompanel;
	private String url = "";
	private int originalX = 0;
	private int originalY = 0;
	private final int maxx;
	private final int maxy;
	private boolean loaded = false;
	private boolean zoomed = false;
	private Widget zoomover = null;
	private Timer timer = null;
	private int delay = BaseContext.get().longUiTimeout();

}
