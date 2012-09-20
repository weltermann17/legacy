package de.man.mn.gep.client.details.documents;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasRowOverHandlers;

import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.DocumentsSummary;

public abstract class DocumentsPresenter extends BasePresenter<DocumentsModel, DocumentsView> {
	interface View {

		@RequestSender("FormatSelected") HasRowOverHandlers rowover();

		@RequestSender("ExtensionClicked") HasCellClickHandlers extension();

	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		view().setDocuments(new Record[0], false);
	}

	@EventReceiver void documentsSummary(final DocumentsSummary event) {
		view().setDocuments(model().getDocumentsSummary(event), true);
	}

}
