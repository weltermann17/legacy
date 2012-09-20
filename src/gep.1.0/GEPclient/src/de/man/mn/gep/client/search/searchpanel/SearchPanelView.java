package de.man.mn.gep.client.search.searchpanel;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ChangedGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ClickGesture;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ItemKeyPressEvent;
import com.smartgwt.client.widgets.form.events.ItemKeyPressHandler;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.DateItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;
import com.smartgwt.client.widgets.form.fields.events.HasFormItemClickHandlers;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;

import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.event.RemoteSearch;
import de.man.mn.gep.client.shared.event.SignedOn;

public abstract class SearchPanelView extends CanvasView implements SearchPanelPresenter.View {

	void setData(final DataType datatype, final String[] criteria, final LinkedHashMap<String, String> searchtypes,
			final Map<String, String> searchicons) {
		this.datatype = datatype;
		final HLayout hlayout = new HLayout();
		hlayout.setSize("100%", "100%");

		criterionform = new DynamicForm();
		criterionform.setWidth("*");
		criterionform.setHeight(SearchPanelView.HEIGHT);
		criterionform.setPadding(4);
		criterionform.setNumCols(7);
		criterionform.setColWidths(200, 60, 100, 40, 200, 200);
		criterionform.setItemLayout(FormLayoutType.TABLE);
		criterionform.setSelectOnFocus(true);
		criterionform.setHoverWrap(false);
		criterionform.setDisabled(true);

		searchtype = new SelectItem();
		searchtype.setWidth(150);
		searchtype.setHeight(SearchPanelView.HEIGHT);
		searchtype.setShowTitle(false);
		searchtype.setShowFocused(false);
		searchtype.setDefaultToFirstOption(false);
		searchtype.setValueMap(searchtypes);
		searchtype.setValueIcons(searchicons);

		criterion = new SelectItem();
		criterion.setWidth(180);
		criterion.setHeight(SearchPanelView.HEIGHT);
		criterion.setShowTitle(false);
		criterion.setShowFocused(false);
		criterion.setDefaultToFirstOption(false);

		combobox = new ComboBoxItem("combobox");
		combobox.setWidth(240);
		combobox.setShowTitle(false);
		combobox.setCanFocus(true);
		combobox.setShowFocused(false);
		combobox.setHideEmptyPickList(true);
		combobox.setCompleteOnTab(false);
		combobox.setShowPickListOnKeypress(false);
		combobox.setDefaultToFirstOption(false);
		combobox.setBrowserSpellCheck(false);
		combobox.setVisible(false);

		picker = new SelectItem("picker");
		picker.setWidth(240);
		picker.setHeight(SearchPanelView.HEIGHT);
		picker.setShowTitle(false);
		picker.setCanFocus(false);
		picker.setShowFocused(false);
		picker.setVisible(false);

		from = formatDate("from", "oldest", 0);
		to = formatDate("to", "newest", 0);

		hint = new FormItemIcon();
		hint.setSrc("[SKIN]/actions/help.png");
		hint.setShowFocused(false);

		back = new FormItemIcon();
		back.setSrc("[SKIN]/actions/back.png");
		back.setShowFocused(false);
		back.setPrompt("Execute the previous search");

		next = new FormItemIcon();
		next.setSrc("[SKIN]/actions/forward.png");
		next.setShowFocused(false);
		next.setPrompt("Execute the next search");

		icons = new StaticTextItem();
		icons.setShowTitle(false);
		icons.setValue("");
		icons.setIcons(back, hint, next);
		icons.setWidth(60);
		icons.setVisible(false);
		criterionform.setFields(searchtype, criterion, combobox, picker, from, to, icons);

		searchbutton = new Button("Search");
		searchbutton.setCanFocus(true);
		searchbutton.setCanFocus(true);
		searchbutton.setPrompt("You need to sign on to search.");
		searchbutton.setDisabled(true);
		final VLayout searchlayout = new VLayout();
		searchlayout.setWidth(120);
		searchlayout.setDefaultLayoutAlign(Alignment.CENTER);
		searchlayout.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		searchlayout.addMember(new LayoutSpacer());
		searchlayout.addMember(searchbutton);
		searchlayout.addMember(new LayoutSpacer());

		hlayout.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		hlayout.addMember(criterionform);
		hlayout.addMember(searchlayout);

		final Canvas canvas = asCanvas();
		canvas.setHeight(58);
		canvas.addChild(hlayout);
	}

	@Override public HasChangedHandlers criterion() {
		return criterion;
	}

	@Override public HasChangedHandlers searchtype() {
		return searchtype;
	}

	@Override public HasFormItemClickHandlers back() {
		return back;
	}

	@Override public HasFormItemClickHandlers next() {
		return next;
	}

	@Override public HasFormItemClickHandlers hint() {
		return hint;
	}

	@Override public HasChangedHandlers[] input() {
		final HasChangedHandlers[] result = { combobox, picker, from, to };
		return result;
	}

	@Override public HasClickHandlers searchbutton() {
		return searchbutton;
	}

	public void updateCriterion(final String[] criteria, final LinkedHashMap<String, String> searchTypes,
			final Map<String, String> searchTypeIcons, final Criterion selectedCriterion) {
		criterion.clearValue();
		criterion.setValueMap(criteria);
		searchtype.clearValue();
		searchtype.setValueMap(searchTypes);
		searchtype.setValueIcons(searchTypeIcons);
		datatype = DataType.valueOf(selectedCriterion.type);
		if (selectedCriterion != null) {
			criterion.setValue(selectedCriterion.name);
			searchtype.setValue(selectedCriterion.displaytype);
			final CriterionChanged changedEvent = GWT.create(CriterionChanged.class);
			changedEvent.selectedCriterion = selectedCriterion;
			this.criterionChanged(changedEvent);
		}
	}

	@EventReceiver protected void criterionChanged(final CriterionChanged event) {
		final Criterion criterion;
		if (event.getData() != null) {
			criterion = event.getData();
		} else {
			criterion = event.selectedCriterion;
		}
		if (datatype.equals(criterion.getDataType())) {
			this.criterion.setValue(criterion.getName());
			if (null == criterion.getUi()) {
				show(null);
			} else if ("combobox".equals(criterion.getUi())) {
				combobox.setLength(criterion.getLength());
				combobox.setKeyPressFilter(criterion.getKeyFilter());
				combobox.setValueMap(criterion.getHistory().asStringArray());
				if (null != criterion.getValue()) {
					combobox.setValue(criterion.getValue().text);
				}
				show(combobox);
			} else if ("picker".equals(criterion.getUi())) {
				picker.setValueMap(criterion.getPickerTitles());
				if (null != criterion.getValue()) {
					picker.setValue(criterion.getValue().text);
				}
				show(picker);
				searchOnChange(criterion);
			} else if ("fromto".equals(criterion.getUi())) {
				if (null != criterion.getValue()) {
					from.setValue(criterion.getValue().from);
					to.setValue(criterion.getValue().to);
				}
				show(from);
			}

			hint.setPrompt(criterion.getHint());
		}

		if (event.refreshSearch) {
			search();
		}
	}

	@RequestGesture(request = "InputChanged", gesture = ChangedGesture.class) abstract protected void inputChanged(
			final Object[] values);

	@RequestGesture(request = "Search", gesture = ClickGesture.class) abstract protected void search();

	@EventReceiver protected void signedOn(final SignedOn event) {
		if (event.isSuccess()) {
			criterionform.setDisabled(false);
			icons.show();
			searchbutton.setDisabled(false);
			searchbutton.setPrompt("");
			addFormHandlers(criterionform);

			show(null);
		}
	}

	@EventReceiver protected void remoteSearch(final RemoteSearch event) {
		if (datatype.equals(event.datatype)) {
			criterionChanged(event.criterion);
			if (null != event.values) {
				inputChanged(event.values);
			}
			search();
		}
	}

	private void searchOnChange(final Criterion criterion) {
		if (criterion.getName().equals(previousname)) {
			if (!criterion.getValue().text.equals(previousvalue)) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override public void execute() {
						search();
					}
				});
			}
		}
		previousname = criterion.getName();
		previousvalue = criterion.getValue().text;
	}

	private void show(final FormItem item) {
		if (null == item) {
			combobox.hide();
			picker.hide();
			from.hide();
			to.hide();
		} else if (!item.getVisible()) {
			if (combobox.equals(item)) {
				combobox.show();
				picker.hide();
				from.hide();
				to.hide();
				selectItem(combobox);
			} else if (picker.equals(item)) {
				combobox.hide();
				picker.show();
				from.hide();
				to.hide();
				picker.focusInItem();
			} else if (from.equals(item)) {
				combobox.hide();
				picker.hide();
				from.show();
				to.show();
				from.focusInItem();
			}
		}
	}

	void setDataType(final DataType datatype) {
		this.datatype = datatype;
	}

	DataType getDataType() {
		return datatype;
	}

	private void addFormHandlers(final DynamicForm form) {
		form.addItemKeyPressHandler(new ItemKeyPressHandler() {
			@Override public void onItemKeyPress(final ItemKeyPressEvent event) {
				if ("Enter".equals(event.getKeyName())) {
					search();
				}
			}
		});
	}

	private DateItem formatDate(final String name, final String title, final int extrawidth) {
		final DateItem date = new DateItem(name);
		date.setWidth(120 + extrawidth);
		date.setUseTextField(true);
		date.setShowTitle(true);
		date.setShowFocused(false);
		date.setTitle(title);
		date.setTitleOrientation(TitleOrientation.LEFT);
		date.setHeight(SearchPanelView.HEIGHT);
		date.setEnforceDate(true);
		date.setTextAlign(Alignment.LEFT);
		date.setVisible(false);
		return date;
	}

	SelectItem getSearchType() {
		return searchtype;
	}

	private DataType datatype;
	private DynamicForm criterionform;
	private SelectItem criterion;
	private ComboBoxItem combobox;
	private SelectItem searchtype;
	private SelectItem picker;
	private DateItem from;
	private DateItem to;
	private StaticTextItem icons;
	private FormItemIcon hint;
	private FormItemIcon back;
	private FormItemIcon next;
	private Button searchbutton;
	private String previousname;
	private String previousvalue;
	private static final int HEIGHT = 22;
}
