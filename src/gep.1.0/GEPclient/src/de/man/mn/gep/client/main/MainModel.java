package de.man.mn.gep.client.main;

import org.restlet.client.data.MediaType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.util.WindowUtil;

import de.man.mn.gep.client.shared.event.ReloadApplication;
import de.man.mn.gep.client.shared.event.SignedOff;

public abstract class MainModel extends BaseModel {

	@Override protected void onInit(final JSONValue data) {
		MediaType.register("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				"Microsoft EXCEL 2007+ File / OOXML");
	}

	@EventReceiver protected void finallySignOff(final SignedOff event) {
		final ReloadApplication reload = GWT.create(ReloadApplication.class);
		final Response<Object, ReloadApplication> response = new Response<Object, ReloadApplication>(reload);
		response.success();
	}

	@EventReceiver protected void reloadApplication(final ReloadApplication event) {
		WindowUtil.reloadWithoutRefresh();
	}

}
