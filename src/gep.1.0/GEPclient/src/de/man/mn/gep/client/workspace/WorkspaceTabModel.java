package de.man.mn.gep.client.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;

import de.man.mn.gep.client.shared.event.ExportableSelected;

public abstract class WorkspaceTabModel extends BaseModel {

	public boolean isExportable() {
		return false;
	}

	@Override protected void onInit(final BusEvent<?> event) {
		onInit((JSONValue) null);
	}

	@Override protected void onInit(final JSONValue data) {
		final ExportableSelected exportableSelected = GWT.create(ExportableSelected.class);
		exportableSelected.exportable = isExportable();
		exportableSelected.fire();
	}

	@RequestReceiver("ExportableSelected") @EventSender(ExportableSelected.class) protected void exportableSelected(
			final Request<TabSelectedEvent> request, final Response<Object, ExportableSelected> response) {
		response.event().exportable = isExportable();
		response.success();
	}
}
