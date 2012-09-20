package de.man.mn.gep.client.workspace.searchresult.menu;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.smartgwt.client.widgets.menu.events.ItemClickEvent;

import de.man.mn.gep.client.shared.dao.DataType;

public class MenuAction {

	enum Type {
		search, millertree, spacetree, enovia5, lock, unlock, edit, delete, create, invalid
	};

	public MenuAction(final Request<ItemClickEvent> request, final JSONArray layout) {
		final JSONObject itemlayout = layout.get(request.gesture().getItem().getAttributeAsInt("index")).isObject();
		if (itemlayout.containsKey("action")) {
			final JSONObject action = itemlayout.get("action").isObject();
			if (action.containsKey("type")) {
				type = Type.valueOf(action.get("type").isString().stringValue());
			}
			if (action.containsKey("title")) {
				title = action.get("title").isString().stringValue();
			}
			if (action.containsKey("datatype")) {
				datatype = DataType.valueOf(action.get("datatype").isString().stringValue());
			}
		}
	}

	public Type getType() {
		return type;
	}

	public DataType getDataType() {
		return datatype;
	}

	public String getTitle() {
		return title;
	}

	public boolean isValid() {
		switch (type) {
		case create:
		case search:
			return null != datatype;
		default:
			return true;
		}
	}

	@Override public String toString() {
		return type.name() + datatype + title;
	}

	private Type type = Type.invalid;
	private DataType datatype;
	private String title = "No title";

}
