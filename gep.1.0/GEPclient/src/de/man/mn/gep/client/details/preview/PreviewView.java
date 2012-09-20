package de.man.mn.gep.client.details.preview;

import com.google.gwt.user.client.ui.Widget;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.ibm.de.ebs.plm.gwt.client.ui.smartgwt.ImageWithZoom;
import com.ibm.de.ebs.plm.gwt.client.util.StringUtil;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;

import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.DocumentsSummary;
import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class PreviewView extends SectionStackSectionView implements PreviewPresenter.View {

	public PreviewView() {
		asCanvas().setOverflow(Overflow.HIDDEN);
		asCanvas().setSize("100%", "300px");
		image = new ImageWithZoom(true, 300, 300);
		image.addResizedHandler(new ResizedHandler() {
			@Override public void onResized(final ResizedEvent event) {
				final Canvas i = (Canvas) event.getSource();
				asCanvas().setHeight(i.getHeight() + 2);
			}
		});
		asCanvas().addChild(image);
		getSectionStackSection().setID(PreviewView.id);
		getSectionStackSection().setTitle("Preview");
		getSectionStackSection().setExpanded(false);
		getSectionStackSection().setCanCollapse(true);
		getSectionStackSection().setResizeable(true);
		getSectionStackSection().addItem(asCanvas());
	}

	@Override public void zoomOver(final Widget zoomover) {
		image.setZoomOver(zoomover);
	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		if (!event.isSuccess()) {
			collapse(null);
			loadImage(null);
			nodetails = true;
		} else {
			nodetails = false;
		}
	}

	@EventReceiver protected void collapse(final UserSelected event) {
		getSectionStack().collapseSection(PreviewView.id);
		expandneeded = true;
	}

	@EventReceiver protected void documentsLoaded(final DocumentsSummary event) {
		if (!nodetails) {
			if (!handlePreview("jpg", event)) {
				if (!handlePreview("png", event)) {
					collapse(null);
					loadImage(null);
				}
			}
		}
	}

	private boolean handlePreview(final String extension, final DocumentsSummary event) {
		/*
		 * 29295 is (in our world) the size of an empty jpg
		 */
		String type = "g3d";
		String page = "0001";
		String url = event.getUrl(extension, type, page);
		if (null == url) {
			type = "lht";
			url = event.getUrl(extension, type, page);
		}
		if (null == url) {
			type = "g2d";
			url = event.getUrl(extension, type, page);
		}
		if (null == url) {
			page = "0002";
			url = event.getUrl(extension, type, page);
		}
		if (null == url) {
			page = "0003";
			url = event.getUrl(extension, type, page);
		}
		if (null == url) {
			page = "0004";
			url = event.getUrl(extension, type, page);
		}
		if (null == url) {
			page = "0005";
			url = event.getUrl(extension, type, page);
		}
		final boolean validsize = !"jpg".equals(extension) || 29295 != event.getFileSize(extension, type, page);
		if (null != url && validsize) {
			loadImage(url);
			return true;
		}
		return false;
	}

	private void loadImage(final String url) {
		try {
			if (expandneeded && null != url) {
				getSectionStack().expandSection(PreviewView.id);
				expandneeded = false;
			}
			String title = "Preview";
			if (null != url) {
				String u = url;
				u = u.substring(0, u.length() - 1);
				u = u.substring(u.lastIndexOf("/") + 1);
				title = StringUtil.fromHexString(u);
			}
			getSectionStack().setSectionTitle(PreviewView.id, title);
			image.setUrl(null == url ? "" : url);
		} catch (final Exception e) {
			getLogger().severe("loadImage " + e);
		}
	}

	private final ImageWithZoom image;
	private boolean expandneeded = true;
	private boolean nodetails;
	private static final String id = "previewsection";

}
