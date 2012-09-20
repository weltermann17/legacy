package de.man.mn.gep.client.details.detail;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemIfFunction;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.fields.events.IconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.IconClickHandler;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.event.DetailSelected;

public abstract class DetailView extends CanvasView implements DetailPresenter.View {

	@Override protected void onInit(final BusEvent<?> event) {
		form = new DynamicForm();
		form.setSize("100%", "100%");
		form.setPadding(4);
		form.setNumCols(2);
		form.setColWidths("40%", "*");
		form.setItemLayout(FormLayoutType.TABLE);
		form.setFixedColWidths(false);
		form.setCanHover(true);
		form.setShowHover(true);
		form.setHoverWrap(false);
		form.setAutoFetchData(false);
		form.setUseAllDataSourceFields(true);
		form.setDataSource(((DetailSelected) event).datasource);
		form.fetchData();
	}

	@Override public HasClickHandlers[] links() {
		return linkitems.toArray(new HasClickHandlers[linkitems.size()]);
	}

	public void setLayout(final JSONArray layout) {
		try {
			final FormItemIfFunction ifBoolean = new FormItemIfFunction() {
				@Override public boolean execute(final FormItem item, final Object value, final DynamicForm form) {
					final Boolean b = (Boolean) value;
					return b.booleanValue();
				}
			};
			final FormItemIfFunction ifString = new FormItemIfFunction() {
				@Override public boolean execute(final FormItem item, final Object value, final DynamicForm form) {
					final String s = (String) value;
					return 0 < s.length();
				}
			};
			final List<FormItem> formitems = new LinkedList<FormItem>();
			for (int i = 0; i < layout.size(); ++i) {
				final JSONObject itemlayout = layout.get(i).isObject();
				String name = "no name";
				String type = "text";
				boolean span = false;
				String title = "no title";
				String hint = null;
				String prompt = null;
				String category = null;
				String datatype = null;
				boolean bold = false;
				boolean hidden = false;
				if (itemlayout.containsKey("name")) {
					name = itemlayout.get("name").isString().stringValue();
				}
				if (itemlayout.containsKey("type")) {
					type = itemlayout.get("type").isString().stringValue();
				}
				if (itemlayout.containsKey("islink")) {
					if (itemlayout.get("islink").isBoolean().booleanValue()) {
						type = "link";
					}
				}
				if (itemlayout.containsKey("span")) {
					span = itemlayout.get("span").isBoolean().booleanValue();
				}
				if (itemlayout.containsKey("title")) {
					title = itemlayout.get("title").isString().stringValue();
				}
				if (itemlayout.containsKey("prompt")) {
					prompt = itemlayout.get("prompt").isString().stringValue();
				}
				if (itemlayout.containsKey("hint")) {
					hint = itemlayout.get("hint").isString().stringValue();
				}
				if (itemlayout.containsKey("category")) {
					category = itemlayout.get("category").isString().stringValue();
				}
				if (itemlayout.containsKey("datatype")) {
					datatype = itemlayout.get("datatype").isString().stringValue();
				}
				if (itemlayout.containsKey("bold")) {
					bold = itemlayout.get("bold").isBoolean().booleanValue();
				}
				if (itemlayout.containsKey("hidden")) {
					hidden = itemlayout.get("hidden").isBoolean().booleanValue();
				}
				FormItem item = null;
				if ("text".equalsIgnoreCase(type)) {
					item = new StaticTextItem(name, title);
					item.setShowIfCondition(ifString);
				} else if ("description".equalsIgnoreCase(type)) {
					setDescriptions(formitems);
				} else if ("boolean".equalsIgnoreCase(type)) {
					item = new CheckboxItem(name, title);
					item.setShowIfCondition(ifBoolean);
					((CheckboxItem) item).setLabelAsTitle(true);
				} else if ("date".equalsIgnoreCase(type)) {
					item = new StaticTextItem(name, title);
				} else if ("link".equalsIgnoreCase(type)) {
					item = new LinkItem(name);
					item.setShowIfCondition(ifString);
					item.setTitle(title);
					item.setHint(hint);
					((LinkItem) item).setTarget("javascript");
					linkitems.add(item);
				} else if ("spacer".equalsIgnoreCase(type)) {
					item = new SpacerItem();
				}
				if (null != item && !hidden) {
					item.setAttribute("category", null != category ? category : title);
					item.setAttribute("datatype", datatype);
					item.setTitleAlign(Alignment.LEFT);
					if (span) {
						final FormItem titleonly = new StaticTextItem(name, title);
						titleonly.setShowIfCondition(ifString);
						titleonly.setShowValueIconOnly(true);
						titleonly.setTitleAlign(Alignment.LEFT);
						formitems.add(titleonly);
						item.setShowTitle(false);
						item.setColSpan(2);
					}
					if (null != hint) {
						item.setHint(hint);
					}
					if (null != prompt) {
						item.setPrompt(prompt);
					}
					if (bold) {
						item.setTextBoxStyle(Context.get().boldStyle());
					}
					formitems.add(item);
				}
			}
			form.setFields(formitems.toArray(new FormItem[formitems.size()]));
			asCanvas().addChild(form);
		} catch (final Exception e) {
			getLogger().severe(getClass().getName() + " failed : " + e.toString());
		}
	}

	private void setDescriptions(final List<FormItem> formitems) {
		currentlanguage = Language.DE;
		final StaticTextItem descriptions = new StaticTextItem("", "Description");
		descriptions.setTitleAlign(Alignment.LEFT);
		final FormItemIcon de = new FormItemIcon();
		de.setSrc("/content/flags/24/GM.png");
		de.setName(Language.DE.name());
		final FormItemIcon en = new FormItemIcon();
		en.setSrc("/content/flags/24/UK.png");
		en.setName(Language.EN.name());
		final FormItemIcon fr = new FormItemIcon();
		fr.setSrc("/content/flags/24/FR.png");
		fr.setName(Language.FR.name());
		final FormItemIcon pl = new FormItemIcon();
		pl.setSrc("/content/flags/24/PL.png");
		pl.setName(Language.PL.name());
		final FormItemIcon tr = new FormItemIcon();
		tr.setSrc("/content/flags/24/TU.png");
		tr.setName(Language.TR.name());
		descriptions.setIconWidth(16);
		descriptions.setIconHeight(16);
		descriptions.setIconVAlign(VerticalAlignment.CENTER);
		descriptions.setShowIfCondition(new FormItemIfFunction() {
			@Override public boolean execute(final FormItem item, final Object value, final DynamicForm form) {
				final Record record = form.getValuesAsRecord();
				final List<FormItemIcon> visible = new LinkedList<FormItemIcon>();
				if (0 < record.getAttributeAsString("description_de").length()) {
					visible.add(de);
				}
				if (0 < record.getAttributeAsString("description_en").length()) {
					visible.add(en);
				}
				if (0 < record.getAttributeAsString("description_fr").length()) {
					visible.add(fr);
				}
				if (0 < record.getAttributeAsString("description_pl").length()) {
					visible.add(pl);
				}
				if (0 < record.getAttributeAsString("description_tr").length()) {
					visible.add(tr);
				}
				descriptions.setIcons(visible.toArray(new FormItemIcon[1]));
				return true;
			}
		});
		descriptions.setRedrawOnChange(true);
		descriptions.addIconClickHandler(new IconClickHandler() {
			@Override public void onIconClick(final IconClickEvent event) {
				currentlanguage = Language.valueOf(event.getIcon().getName());
				form.redraw();
			}
		});
		final FormItemIfFunction ifLanguage = new FormItemIfFunction() {
			@Override public boolean execute(final FormItem item, final Object value, final DynamicForm form) {
				return currentlanguage.name().equalsIgnoreCase(item.getName().substring(12));
			}
		};
		final StaticTextItem description_de = new StaticTextItem("description_de");
		description_de.setTextBoxStyle(Context.get().boldStyle());
		description_de.setShowTitle(false);
		description_de.setColSpan(2);
		description_de.setShowIfCondition(ifLanguage);
		final StaticTextItem description_en = new StaticTextItem("description_en");
		description_en.setTextBoxStyle(Context.get().boldStyle());
		description_en.setShowTitle(false);
		description_en.setColSpan(2);
		description_en.setShowIfCondition(ifLanguage);
		final StaticTextItem description_fr = new StaticTextItem("description_fr");
		description_fr.setTextBoxStyle(Context.get().boldStyle());
		description_fr.setShowTitle(false);
		description_fr.setColSpan(2);
		description_fr.setShowIfCondition(ifLanguage);
		final StaticTextItem description_pl = new StaticTextItem("description_pl");
		description_pl.setTextBoxStyle(Context.get().boldStyle());
		description_pl.setShowTitle(false);
		description_pl.setColSpan(2);
		description_pl.setShowIfCondition(ifLanguage);
		final StaticTextItem description_tr = new StaticTextItem("description_tr");
		description_tr.setTextBoxStyle(Context.get().boldStyle());
		description_tr.setShowTitle(false);
		description_tr.setColSpan(2);
		description_tr.setShowIfCondition(ifLanguage);
		final SpacerItem spacer = new SpacerItem();
		formitems.add(descriptions);
		formitems.add(spacer);
		formitems.add(description_de);
		formitems.add(description_en);
		formitems.add(description_fr);
		formitems.add(description_pl);
		formitems.add(description_tr);
	}

	private enum Language {
		DE, EN, FR, PL, TR, BR, CN, INTERNAL
	};

	private final List<HasClickHandlers> linkitems = new LinkedList<HasClickHandlers>();
	private Language currentlanguage;
	private DynamicForm form;

}
