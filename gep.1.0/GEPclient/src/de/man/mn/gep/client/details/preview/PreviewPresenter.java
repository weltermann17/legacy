package de.man.mn.gep.client.details.preview;

import com.google.gwt.user.client.ui.Widget;
import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;

public abstract class PreviewPresenter extends BasePresenter<PreviewModel, PreviewView> {

	interface View {

		void zoomOver(final Widget zoomover);

	}

}
