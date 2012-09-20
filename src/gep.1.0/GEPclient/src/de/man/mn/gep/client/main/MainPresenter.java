package de.man.mn.gep.client.main;

import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ShowMessage;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ShowMessages;

public abstract class MainPresenter extends BasePresenter<MainModel, MainView> {

	interface View {
	}

	@EventReceiver protected void showMessage(final ShowMessage event) {
		ShowMessages.get().add(event);
	}

}
