package com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseView;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.fields.TextItem;

public abstract class CanvasView extends BaseView {

	protected CanvasView() {
		canvas = new CanvasWithDestroy();
		canvas.setOverflow(Overflow.HIDDEN);
	}

	@SuppressWarnings("unchecked") @Override public <T> T asUIObject() {
		return (T) asCanvas();
	}

	public Canvas asCanvas() {
		return canvas;
	}

	public void selectItem(final TextItem item) {
		item.selectValue();
		Scheduler.get().scheduleFinally(new ScheduledCommand() {
			@Override public void execute() {
				item.selectValue();
				item.focusInItem();
			}
		});
	}

	private class CanvasWithDestroy extends Canvas {
		@Override protected void onDestroy() {
			super.onDestroy();
			getPresenter().unbind();
		}
	}

	private final Canvas canvas;
}
