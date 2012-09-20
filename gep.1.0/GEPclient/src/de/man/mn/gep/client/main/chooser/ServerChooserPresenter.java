package de.man.mn.gep.client.main.chooser;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.ibm.de.ebs.plm.gwt.client.mvp.BasePresenter;

public abstract class ServerChooserPresenter extends BasePresenter<ServerChooserModel, ServerChooserView> {

	interface View {
	}

	@Override protected void onBound() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override public void execute() {
				view().setConnectionUrls(model().getConnectionUrls());
			}
		});
	}

}
