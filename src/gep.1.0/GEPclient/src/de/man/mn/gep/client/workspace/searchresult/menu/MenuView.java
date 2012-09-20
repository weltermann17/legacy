package de.man.mn.gep.client.workspace.searchresult.menu;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.BaseMenuView;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.HasItemClickHandlers;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.Detail;

public abstract class MenuView extends BaseMenuView implements MenuPresenter.View {

	@Override public HasItemClickHandlers menu() {
		return asMenu();
	}

	void setData(final Detail detail, final JSONArray layout) {

		final List<MenuItem> menuitems = new LinkedList<MenuItem>();
		for (int i = 0; i < layout.size(); ++i) {
			final JSONObject itemlayout = layout.get(i).isObject();
			String type = "item";
			String title = "no title";
			String icon = null;
			JSONObject conditions = null;
			if (itemlayout.containsKey("type")) {
				type = itemlayout.get("type").isString().stringValue();
			}
			if (itemlayout.containsKey("title")) {
				title = formatTitle(itemlayout.get("title").isString().stringValue(), detail);
			}
			if (itemlayout.containsKey("icon")) {
				icon = itemlayout.get("icon").isString().stringValue();
			}
			if (itemlayout.containsKey("conditions")) {
				conditions = itemlayout.get("conditions").isObject();
			}
			boolean include = null == conditions;
			if (!include) {
				for (final String condition : conditions.keySet()) {
					if (null != conditions.get(condition).isBoolean()) {
						if ("lockowner".equals(condition)) {
							final boolean musthavelock = conditions.get(condition).isBoolean().booleanValue();
							final String loginuser = Context.get().getChallengeLogin();
							final String lockowner = detail.getRecord().getAttributeAsString("lockowner");
							if (null == lockowner) {
								include = musthavelock ? false : true;
							} else {
								include = musthavelock ? lockowner.equalsIgnoreCase(loginuser) : false;
							}
						} else {
							include = detail.getRecord().getAttributeAsBoolean(condition)
									.equals(conditions.get(condition).isBoolean().booleanValue());
						}
					} else if (null != conditions.get(condition).isString()) {
						include = detail.getRecord().getAttributeAsString(condition)
								.equals(conditions.get(condition).isString().stringValue());
					} else if (null != conditions.get(condition).isNumber()) {
						include = detail.getRecord().getAttributeAsInt(condition)
								.equals((int) conditions.get(condition).isNumber().doubleValue());
					} // else not handled yet
				}
			}
			if (include) {
				MenuItem item = null;
				if ("item".equalsIgnoreCase(type)) {
					if (null != icon) {
						item = new MenuItem(title, icon);
					} else {
						item = new MenuItem(title);
					}
				} else if ("spacer".equalsIgnoreCase(type)) {
					item = new MenuItemSeparator();
				}
				if (null != item) {
					item.setAttribute("index", i);
					menuitems.add(item);
				}
			}
		}
		asMenu().setItems(menuitems.toArray(new MenuItem[menuitems.size()]));
	}

	private String formatTitle(final String title, final Detail detail) {
		return title.replace("#displayname", "'" + detail.getDisplayName() + "'");
	}

}
