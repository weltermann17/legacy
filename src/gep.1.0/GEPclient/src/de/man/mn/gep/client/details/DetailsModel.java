package de.man.mn.gep.client.details;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class DetailsModel extends BaseModel {

	@RequestReceiver("UserClosed") @EventSender(UserSelected.class) void userDetailsClosed(
			final Request<TabCloseClickEvent> request, final Response<JSONValue, UserSelected> response) {
		final String user = popUser();
		if (null != user) {
			response.event().user = user;
			response.event().datasource = new RestletDataSource("/users/details/" + user + "/", response, "", user
					+ " not found.");
			response.success();
		}
	}

	@EventReceiver void userSelected(final UserSelected event) {
		pushUser(event.user);
	}

	private void pushUser(final String user) {
		userstack.remove(user);
		userstack.add(user);
	}

	private String popUser() {
		if (1 < userstack.size()) {
			userstack.remove(userstack.size() - 1);
			return userstack.remove(userstack.size() - 1);
		} else {
			userstack.clear();
			return null;
		}
	}

	private final List<String> userstack = new LinkedList<String>();

}
