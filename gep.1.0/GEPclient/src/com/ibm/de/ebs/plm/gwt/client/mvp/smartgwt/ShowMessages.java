package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import java.util.LinkedList;
import java.util.List;

import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;

public final class ShowMessages implements BooleanCallback {

	public static ShowMessages get() {
		return ShowMessages.instance;
	}

	public void add(final ShowMessage event) {
		messageevents.add(event);
		if (!modal) {
			showMessage();
		}
	}

	@Override public void execute(final Boolean value) {
		showMessage();
	}

	private void showMessage() {
		if (0 < messageevents.size()) {
			modal = true;
			final ShowMessage event = messageevents.get(0);
			String message = event.message;
			final int maxlen = 1024;
			if (maxlen < message.length()) {
				BaseContext.get().getLogger().severe(message);
				message = message.substring(0, maxlen / 2) + " ... " + message.substring(message.length() - maxlen / 2);
			}
			if (event.error) {
				SC.warn(event.title, "<div class='errordialog'>" + message + "</div>", ShowMessages.this, new Dialog());
			} else {
				SC.say(event.title, "<div class='errordialog'>" + message + "</div>", ShowMessages.this);
			}
			messageevents.remove(event);
		} else {
			modal = false;
		}
	}

	public static native void say(String title, String message, BooleanCallback callback)
	/*-{
		$wnd.isc.say(message, callback, {
			title : title,
			width : 1000
		});
	}-*/;

	private boolean modal = false;
	private final List<ShowMessage> messageevents = new LinkedList<ShowMessage>();

	private static final ShowMessages instance = new ShowMessages();

}
