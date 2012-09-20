package com.ibm.de.ebs.plm.gwt.client.restlet;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Status;
import org.restlet.client.engine.header.Header;
import org.restlet.client.resource.ClientResource;
import org.restlet.client.util.Series;

import com.google.gwt.user.client.Timer;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.ibm.de.ebs.plm.gwt.client.util.WindowUtil;
import com.smartgwt.client.util.SC;

public class FileDownload implements Uniform {

	public static void get(final String url, final MediaType mediatype) {
		final ClientResource resource = new AuthenticatingResource(new FileDownload(url), url, true);
		resource.head(mediatype);
	}

	private FileDownload(final String url) {
		this.url = url;
		show();
	}

	@Override public void handle(final Request request, final Response response) {
		clear();
		try {
			final Status status = response.getStatus();
			if (status.isSuccess() || 0 == status.getCode()) {
				final @SuppressWarnings("unchecked")
				Series<Header> headers = (Series<Header>) response.getAttributes().get(FileDownload.headersconstant);
				final String authorizationtoken = headers.getFirstValue("X-Authorization-Token");
				final String geturl = url.contains("?") ? url + "&token=" + authorizationtoken : url + "?" + authorizationtoken;
				WindowUtil.openUrl(geturl);
			} else {
				Dialogs.error("Download unavailable. Please try again in a moment.");
			}
		} catch (final Throwable e) {
		}
	}

	private final void show() {
		timer = new Timer() {
			@Override public void run() {
				timer = null;
				SC.showPrompt(FileDownload.prompt);
				FileDownload.prompts++;
				new Timer() {
					@Override public void run() {
						clear();
					}
				}.schedule(BaseContext.get().longTimeout());
			}
		};
		timer.schedule(BaseContext.get().shortUiTimeout());
	}

	private final void clear() {
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		if (0 < FileDownload.prompts) {
			FileDownload.prompts--;
			if (0 == FileDownload.prompts) {
				SC.clearPrompt();
			}
		}
	}

	private final static String headersconstant = org.restlet.client.engine.header.HeaderConstants.ATTRIBUTE_HEADERS;
	private final String url;
	private Timer timer = null;
	private static int prompts = 0;
	private final static String prompt = "Starting download...";

}
