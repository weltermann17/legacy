package de.man.mn.gep.client.main.chooser;

import java.util.Date;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.ibm.de.ebs.plm.gwt.client.util.Uuid;
import com.ibm.de.ebs.plm.gwt.client.util.WindowUtil;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public abstract class ServerChooserView extends CanvasView implements ServerChooserPresenter.View {

	public ServerChooserView() {
		final String div = "<div id='loadingWrapper'><div id='loading'><div class='loadingIndicator'>"
				+ "<img src='/content/loading.gif' width='40' height='40' style='margin-right:8px;float:left;vertical-align:middle;'/>MAN Truck & Bus AG - Global Engineering Platform<br/>"
				+ "<span id='loadingMsg'>Choosing best server. Press F5 if this takes too long.</span></div></div></div>";
		final HTML html = new HTML();
		html.setSize("100%", "100%");
		html.setVisible(false);
		html.setHTML(div);
		tilegrid = new TileGrid();
		tilegrid.setVisible(false);
		tilegrid.setSize("100%", "100%");
		tilegrid.setTitle("Automatically choose the nearest GEPserver ...");
		tilegrid.setTileWidth(200);
		tilegrid.setTileHeight(200);
		tilegrid.setCanReorderTiles(false);
		tilegrid.setShowAllRecords(true);
		tilegrid.setAutoFetchData(false);
		final DetailViewerField imagefield = new DetailViewerField("url");
		imagefield.setType("image");
		imagefield.setImageWidth(190);
		imagefield.setImageHeight(120);
		final DetailViewerField spacer = new DetailViewerField("spacer");
		final DetailViewerField location = new DetailViewerField("location");
		final DetailViewerField loadtime = new DetailViewerField("loadtime");
		tilegrid.setFields(imagefield, spacer, location, loadtime);
		asCanvas().setSize("100%", "100%");
		asCanvas().addChild(html);
		asCanvas().addChild(tilegrid);
		shorttimer = new Timer() {
			@Override public void run() {
				if (onlyonce) {
					html.setVisible(true);
				}
			}
		};
		shorttimer.schedule(1000);
		new Timer() {
			@Override public void run() {
				if (onlyonce) {
					html.setVisible(false);
					tilegrid.show();
				}
			}
		}.schedule(5000);
	}

	public void setConnectionUrls(final Record[] connectionurls) {
		tilegrid.setData(connectionurls);

		class LoadTimer implements LoadHandler {

			LoadTimer(final Record server, final Date started) {
				this.server = server;
				this.started = started;
			}

			@Override public void onLoad(final LoadEvent event) {
				if (tilegrid.isVisible()) {
					final long delta = DateUtil.now().getTime() - started.getTime();
					getLogger().info(server + " -> " + delta + " ms");
					server.setAttribute("loadtime", delta + " ms");
					tilegrid.setData(connectionurls);
					tilegrid.redraw();
					if (onlyonce) {
						onlyonce = false;
						tilegrid.setTitle("Automatically choose the nearest GEPserver : "
								+ server.getAttribute("location") + " is the nearest with " + delta + "ms!");
						new Timer() {
							@Override public void run() {
								WindowUtil.openUrl(server.getAttribute("server"));
							}
						}.schedule(4000);
					}
				} else {
					shorttimer.cancel();
					WindowUtil.openUrl(server.getAttribute("server"));
				}
			}

			private final Record server;
			private final Date started;
		}

		for (int i = 0; i < connectionurls.length; ++i) {
			final String url = connectionurls[i].getAttribute("url");
			final Image image = new Image();
			image.setPixelSize(1, 1);
			final int b = url.indexOf("/locations");
			final String imageurl = url.substring(0, b) + "/mantruck.png?" + Uuid.uuid();
			image.setUrl(imageurl);
			image.addLoadHandler(new LoadTimer(connectionurls[i], DateUtil.now()));
			asCanvas().addChild(image);
		}
	}

	boolean onlyonce = true;
	final TileGrid tilegrid;
	Timer shorttimer;

}
