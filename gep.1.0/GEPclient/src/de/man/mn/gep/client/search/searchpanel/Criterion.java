package de.man.mn.gep.client.search.searchpanel;

import java.util.Date;

import name.pehl.piriti.client.json.Json;
import name.pehl.piriti.client.json.JsonReader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import de.man.mn.gep.client.shared.dao.DataType;

public final class Criterion {

	interface Reader extends JsonReader<Criterion> {
	}

	static final Reader Reader = GWT.create(Reader.class);

	public static Criterion readMe(final int index, final JSONObject criterionJSON) {

		final JSONArray elements = (criterionJSON.get("elements").isArray());
		final Criterion criterion = Criterion.Reader.read(elements.get(index).isObject());

		criterion.displaytype = criterionJSON.get("displaytype").isString().stringValue().trim();
		criterion.type = criterionJSON.get("type").isString().stringValue();
		criterion.icon = criterionJSON.get("icon").isString().stringValue();

		return criterion;
	}

	DataType getDataType() {
		return DataType.valueOf(type);
	}

	String getName() {
		return name;
	}

	String getDisplayName() {
		return displayname;
	}

	String getUi() {
		return ui;
	}

	String getKeyFilter() {
		return keyfilter;
	}

	boolean getUppercase() {
		return uppercase;
	}

	int getLength() {
		return length;
	}

	String getHint() {
		return hint;
	}

	String getType() {
		return type;
	}

	String getTemplate() {
		return template;
	}

	String getAlias() {
		return alias;
	}

	String[] getPickerTitles() {
		return titles;
	}

	String[] getPickerValues() {
		return values;
	}

	public String getPickerValue() {
		int i = 0;
		for (final String t : titles) {
			if (t.equals(getValue().text)) {
				return values[i];
			}
			++i;
		}
		return null;
	}

	public void setText(final String text) {
		setValue(CriterionValue.text(uppercase ? text.toUpperCase() : text, displaytype));
	}

	public void setFromTo(final Date from, final Date to) {
		setValue(CriterionValue.fromTo(from, to, displaytype));
	}

	public void setValue(final CriterionValue value) {
		history.setValue(value);
	}

	public String getText() {
		return getValue().text;
	}

	public Date getFrom() {
		return getValue().from;
	}

	public Date getTo() {
		return getValue().to;
	}

	public CriterionValue getValue() {
		if (null == history.getValue() && null != titles) {
			setValue(CriterionValue.text(titles[selectedtitle], displaytype));
		}
		return null != history.getValue() ? history.getValue() : new CriterionValue();
	}

	public void setHistory(final JSONObject data) {
		history = CriterionHistory.Reader.read(data);
	}

	public CriterionHistory getHistory() {
		return history;
	}

	@Override public String toString() {
		return "Criterion " + getDataType() + name + displayname + ui + keyfilter + uppercase + length + hint + type
				+ displaytype + icon + template + alias + titles + values + history;
	}

	@Json protected String name;
	@Json protected String displayname;
	@Json protected String ui;
	@Json protected String keyfilter;
	@Json protected boolean uppercase;
	@Json protected int length;
	@Json protected String[] titles;
	@Json protected int selectedtitle;
	@Json protected String[] values;
	@Json protected String hint;
	@Json protected String template;
	@Json protected String alias;

	protected String type;
	protected String displaytype;
	protected String icon;

	private CriterionHistory history = new CriterionHistory();

}
