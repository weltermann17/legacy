package de.man.mn.gep.client.details.user;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;

import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class UserModel extends BaseModel {

	@RequestReceiver("ManagerSelected") @EventSender(UserSelected.class) void managerSelected(
			final Request<ClickEvent> request, final Response<JSONValue, UserSelected> response) {
		final String manager = request.gesture().getItem().getValue().toString().toLowerCase();
		if (null != manager && 0 < manager.length()) {
			response.event().user = manager;
			response.event().datasource = new RestletDataSource("/users/details/" + manager + "/", response, "",
					manager + " ot found.");
		}
	}

}
