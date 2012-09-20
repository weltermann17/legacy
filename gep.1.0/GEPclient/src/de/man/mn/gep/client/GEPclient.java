package de.man.mn.gep.client;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.util.DateDisplayFormatter;
import com.smartgwt.client.util.DateInputFormatter;
import com.smartgwt.client.util.DateParser;
import com.smartgwt.client.util.DateUtil;
import com.smartgwt.client.util.KeyCallback;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.SC;

import de.man.mn.gep.client.main.MainModel;
import de.man.mn.gep.client.main.MainPresenter;
import de.man.mn.gep.client.main.MainView;
import de.man.mn.gep.client.main.chooser.ServerChooserModel;
import de.man.mn.gep.client.main.chooser.ServerChooserPresenter;
import de.man.mn.gep.client.main.chooser.ServerChooserView;
import de.man.mn.gep.client.shared.Context;

public class GEPclient implements EntryPoint {

	@Override public void onModuleLoad() {
		initializeGWT();
	}

	private void initializeGWT() {
		cleanupLoadingIndicator();

		Window.enableScrolling(false);
		Window.setMargin("0px 0px 0px 0px");
		final int e = GWT.getHostPageBaseURL().length();
		final String port = GWT.getHostPageBaseURL().substring(e - 5, e - 1);
		Window.setTitle(Window.getTitle() + "  -  " + port + "  -  (" + Context.get().buildNumber() + ")");
		initializeSmartGWT(false);

		if ("?redirectToBestServer".equalsIgnoreCase(Window.Location.getQueryString())) {
			final ServerChooserModel model = GWT.create(ServerChooserModel.class);
			final ServerChooserView view = GWT.create(ServerChooserView.class);
			final ServerChooserPresenter presenter = GWT.create(ServerChooserPresenter.class);
			MVP.create(model, view, presenter);
			RootPanel.get().add(view.asCanvas());
		} else {
			final MainModel model = GWT.create(MainModel.class);
			final MainView view = GWT.create(MainView.class);
			final MainPresenter presenter = GWT.create(MainPresenter.class);
			MVP.create(model, view, presenter);
			RootPanel.get().add(view.asCanvas());
		}
	}

	@SuppressWarnings("deprecation") private void initializeSmartGWT(final boolean withconsole) {
		if (withconsole) {
			final KeyIdentifier debugKey = new KeyIdentifier();
			debugKey.setCtrlKey(true);
			debugKey.setShiftKey(true);
			debugKey.setKeyName("D");
			Page.registerKey(debugKey, new KeyCallback() {

				@Override public void execute(final String keyName) {
					SC.showConsole();
				}
			});
		}
		DateUtil.setDefaultDisplayTimezone("+02:00");
		DateUtil.setNormalDateDisplayFormatter(new DateDisplayFormatter() {

			@Override public String format(final Date date) {
				final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
				return dateFormatter.format(date);
			}
		});
		DateUtil.setShortDateDisplayFormatter(new DateDisplayFormatter() {

			@Override public String format(final Date date) {
				final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd");
				return dateFormatter.format(date);
			}
		});
		DateUtil.setDateParser(new DateParser() {
			@Override public Date parse(final String dateString) {
				try {
					final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd");
					final Date date = dateFormatter.parse(dateString);
					return date;
				} catch (final IllegalArgumentException e) {
					return null;
				}
			}
		});
		DateUtil.setDateInputFormatter(new DateInputFormatter() {
			@Override public Date parse(final String dateString) {
				try {
					final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd");
					final Date date = dateFormatter.parse(dateString);
					return date;
				} catch (final IllegalArgumentException e) {
					return null;
				}
			}
		});
		setFirstDayOfWeek();
	}

	private native void setFirstDayOfWeek()
	/*-{
		$wnd.isc.DateChooser.addProperties({
			firstDayOfWeek : 1
		});
	}-*/;

	private native void cleanupLoadingIndicator()
	/*-{
		var wrapper = $wnd.document.getElementById('loadingWrapper');
		var indicator = $wnd.document.getElementById('loading');
		wrapper.removeChild(indicator);
	}-*/;
}
