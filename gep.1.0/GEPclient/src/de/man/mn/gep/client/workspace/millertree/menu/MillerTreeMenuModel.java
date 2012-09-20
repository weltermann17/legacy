package de.man.mn.gep.client.workspace.millertree.menu;

import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;

import de.man.mn.gep.client.workspace.millertree.AttributeChanged;

public abstract class MillerTreeMenuModel extends BaseModel {

	public void setId(final String id) {
		this.id = id;
	}

	@RequestReceiver("AttributeChanged") @EventSender(AttributeChanged.class) void attributeChanged(
			final Request<ChangedEvent> request, final Response<Object, AttributeChanged> response) {
		response.event().attributename = request.gesture().getValue().toString();
		response.event().id = id;
		response.success();
	}

	private String id;
}
