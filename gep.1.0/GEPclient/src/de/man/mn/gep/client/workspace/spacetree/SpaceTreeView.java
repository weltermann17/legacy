package de.man.mn.gep.client.workspace.spacetree;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasClickLabelHandlers;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasMouseOutLabelHandlers;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.HasMouseOverLabelHandlers;
import com.ibm.de.ebs.plm.gwt.client.ui.jit.SpaceTreeCanvas;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.MouseWheelEvent;
import com.smartgwt.client.widgets.events.MouseWheelHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

import de.man.mn.gep.client.shared.event.OpenSpaceTree;
import de.man.mn.gep.client.workspace.WorkspaceTabView;

public abstract class SpaceTreeView extends WorkspaceTabView implements SpaceTreePresenter.View {

	public SpaceTreeView() {
		final Canvas canvas = asCanvas();
		canvas.addResizedHandler(new ResizedHandler() {
			@Override public void onResized(final ResizedEvent event) {
				spacetreecanvas.setPixelSize(canvas.getWidth(), canvas.getHeight());
			}
		});
		spacetreecanvas = new SpaceTreeCanvas(asCanvas().getOffsetWidth(), asCanvas().getOffsetHeight(), 1.33, 2.) {

			@Override protected String getTooltipHtml(final String node) {
				final Label label = new Label(node);
				label.setBackgroundColor("lightgray");
				label.setPixelSize(120, 24);
				label.setAlign(Alignment.CENTER);
				label.setValign(VerticalAlignment.CENTER);
				return label.getInnerHTML();
			}

			@Override protected void onLoadSubset(final JSONValue data) {
				final SpaceTreeModel model = (SpaceTreeModel) SpaceTreeView.this.getPresenter().model();
				model.onLoadSubset(data);
			}

		};
		spacetreecanvas.setSize("100%", "100%");
		spacetreecanvas.addMouseWheelHandler(new MouseWheelHandler() {
			@Override public void onMouseWheel(final MouseWheelEvent event) {
				event.cancel();
			}
		});
		canvas.addChild(spacetreecanvas);
	}

	@Override protected void onInit(final BusEvent<?> event) {
		final OpenSpaceTree e = (OpenSpaceTree) event;
		spacetreecanvas.setData(e.getData().isObject().getJavaScriptObject(), null, asCanvas().getWidth(), asCanvas()
				.getHeight());
	}

	@Override public HasMouseOutLabelHandlers mouseOut() {
		return spacetreecanvas;
	}

	@Override public HasMouseOverLabelHandlers mouseOver() {
		return spacetreecanvas;
	}

	@Override public HasClickLabelHandlers labelClicked() {
		return spacetreecanvas;
	}

	@Override public HasTabSelectedHandlers exportableSelected() {
		return asTab();
	}

	private final SpaceTreeCanvas spacetreecanvas;
}
