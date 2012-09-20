package com.ibm.de.ebs.plm.gwt.client.mvp;

import java.util.LinkedList;
import java.util.List;

import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ShowMessage;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;

public abstract class ShowMessageModel extends BaseModel implements BooleanCallback {

	@EventReceiver protected void showMessage(final ShowMessage event) {
		messageevents.add(event);
		if (!modal) {
			nextMessage();
		}
	}

	@Override public void execute(final Boolean value) {
		nextMessage();
	}

	private void nextMessage() {
		if (0 < messageevents.size()) {
			modal = true;
			final ShowMessage event = messageevents.get(0);
			String message = event.message;
			final int MAXLEN = 1024;
			if (MAXLEN < message.length()) {
				BaseContext.get().getLogger().severe(message);
				message = message.substring(0, MAXLEN / 2) + " ... " + message.substring(message.length() - MAXLEN / 2);
			}
			if (event.error) {
				SC.warn(event.title, "<div class='errordialog'>" + message + "</div>", ShowMessageModel.this,
						new Dialog());
			} else {
				SC.say(event.title, "<div class='errordialog'>" + message + "</div>", ShowMessageModel.this);
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
}
