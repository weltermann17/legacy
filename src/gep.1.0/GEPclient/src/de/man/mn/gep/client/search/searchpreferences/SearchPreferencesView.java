package de.man.mn.gep.client.search.searchpreferences;

import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;

import de.man.mn.gep.client.shared.dao.Location;

public abstract class SearchPreferencesView extends CanvasView implements SearchPreferencesPresenter.View {

	@Override public HasChangedHandlers[] locations() {
		return checkboxes;
	}

	void initLocations(final Location[] locations) {
		checkboxes = new CheckboxItem[locations.length];
		for (int i = 0; i < locations.length; ++i) {
			final Location location = locations[i];
			final CheckboxItem checkbox = new CheckboxItem(location.shortname);
			checkbox.setTitle("\u00a0" + location.title);
			checkbox.setLabelAsTitle(false);
			checkbox.setShowTitle(false);
			checkbox.setDisabled(!location.enabled);
			checkboxes[i] = checkbox;
		}
		form = new DynamicForm();
		form.setItemLayout(FormLayoutType.TABLE);
		form.setWidth(800);
		form.setHeight100();
		form.setPadding(4);
		form.setNumCols(4);
		form.setColWidths(200, 200, 200, 200);
		form.setFields(checkboxes);
		final Canvas canvas = asCanvas();
		canvas.addChild(form);
	}

	private FormItem[] checkboxes;
	private DynamicForm form;
}
