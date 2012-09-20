package de.man.mn.gep.client.details.detail;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.smartgwt.client.widgets.form.fields.FormItem;

import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.RemoteSearch;

public abstract class DetailModel extends BaseModel {

	@Override protected void onInit(final BusEvent<?> event) {
		final DetailSelected e = (DetailSelected) event;
		detail = e.detail;
	}

	@Override protected void onInit(final JSONValue data) {
		layout = data.isArray();
	}

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/layout/details/" + detail.getDataType().name().toLowerCase() + "/", response, false);
	}

	public JSONArray getLayout() {
		return layout;
	}

	@RequestReceiver("Links") @EventSender(RemoteSearch.class) <E extends GwtEvent<?>> void links(final int i,
			final Request<E> request, final Response<Object, RemoteSearch> response) {
		final FormItem item = (FormItem) request.gesture().getSource();
		final Object[] values = new Object[1];
		values[0] = detail.getAttribute(item.getName());
		response.event().datatype = null == item.getAttribute("datatype") ? detail.getDataType() : DataType
				.valueOf(item.getAttribute("datatype"));
		response.event().criterion = item.getAttribute("category");
		response.event().values = values;
		response.success();
	}

	private Detail detail;
	private JSONArray layout;

}
