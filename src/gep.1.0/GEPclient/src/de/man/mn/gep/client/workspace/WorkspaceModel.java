package de.man.mn.gep.client.workspace;

import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

import de.man.mn.gep.client.shared.event.OpenFromEnovia5;
import de.man.mn.gep.client.shared.event.Snap;
import de.man.mn.gep.client.shared.event.TabSetChanged;

public abstract class WorkspaceModel extends BaseModel {

	@RequestReceiver("HorizontalSnap") @EventSender(Snap.class) void horizontalSnap(final Request<ClickEvent> request,
			final Response<Object, Snap> response) {
		response.event().horizontal = true;
		response.event().vertical = false;
		response.success();
	}

	@RequestReceiver("VerticalSnap") @EventSender(Snap.class) void verticalSnap(final Request<ClickEvent> request,
			final Response<Object, Snap> response) {
		response.event().horizontal = false;
		response.event().vertical = true;
		response.success();
	}

	@RequestReceiver("CloseTab") @EventSender(TabSetChanged.class) void closeTab(
			final Request<TabCloseClickEvent> request, final Response<Object, TabSetChanged> response) {
		response.event().tabset = (TabSet) request.gesture().getSource();
		response.success();
	}

	@EventReceiver void openFromEnovia5(final OpenFromEnovia5 event) {
		Dialogs.info("Not yet implemented");
	}

}
