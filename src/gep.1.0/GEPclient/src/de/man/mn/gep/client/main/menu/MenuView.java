package de.man.mn.gep.client.main.menu;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuBar;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemIfFunction;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.HasClickHandlers;
import com.smartgwt.client.widgets.menu.events.HasItemClickHandlers;
import com.smartgwt.client.widgets.tab.Tab;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.ExportableSelected;
import de.man.mn.gep.client.shared.event.RecycleTabs;
import de.man.mn.gep.client.shared.event.SignedOnUser;
import de.man.mn.gep.client.shared.event.TabSetChanged;

public abstract class MenuView extends CanvasView implements MenuPresenter.View {

	public MenuView() {
		menubar = new MenuBar();
		menubar.setMenus(new Menu[0]);
		menubar.setStyleName("gep-ApplicationMenu");
		menubar.setWidth("*");
		final HLayout hlayout = new HLayout();
		hlayout.setPadding(1);
		hlayout.setWidth("100%");
		hlayout.setHeight(menubar.getHeight() + 2);
		hlayout.setBackgroundColor("#d4d0c8");
		hlayout.addMember(menubar);
		final Canvas canvas = asCanvas();
		canvas.setWidth(hlayout.getWidthAsString());
		canvas.setHeight(hlayout.getHeight());
		canvas.addChild(hlayout);

		final MenuItemIfFunction ifsignedon = new MenuItemIfFunction() {
			@Override public boolean execute(final Canvas target, final Menu menu, final MenuItem item) {
				return isSignedOn();
			}
		};
		final MenuItemIfFunction ifsignedoff = new MenuItemIfFunction() {
			@Override public boolean execute(final Canvas target, final Menu menu, final MenuItem item) {
				return !isSignedOn();
			}
		};

		file = createMenu("File", 32);
		saveas = createItem("Save view as Microsoft Excel file...", "disk", null);
		saveas.setEnabled(false);
		restart = createItem("Restart", "arrow_refresh", "F5");
		signon = createItem("Sign on", "connect", null);
		signon.setEnableIfCondition(ifsignedoff);
		signoff = createItem("Sign off" + fill(0, 4), "disconnect", null);
		signoff.setEnableIfCondition(ifsignedon);
		exit = createItem("Exit" + fill(4, 20), "door_in", "Alt F4");
		file.addItem(saveas);
		file.addItem(separator);
		file.addItem(restart);
		file.addItem(separator);
		file.addItem(signon);
		file.addItem(signoff);
		file.addItem(separator);
		file.addItem(exit);

		search = createMenu("Search", 60);
		manpart = createItem("Parts", "brick", null);
		manproduct = createItem("Products", "lorry", null);
		mansnapshot = createItem("Snapshots", "pictures", null);
		partnerparts = createItem("PartnerParts", "package_green", null);
		searchpreferences = createItem("Preferences", "preferences", null);
		search.addItem(manpart);
		search.addItem(partnerparts);
		search.addItem(manproduct);
		search.addItem(mansnapshot);
		search.addItem(separator);
		search.addItem(searchpreferences);

		view = createMenu("View", 40);
		fullscreen = createItem("Full screen" + fill(10, 20), "monitor", "F11");
		zoomin = createItem("Zoom in", "zoom_in", "Ctrl +");
		zoomout = createItem("Zoom out", "zoom_out", "Ctrl -");
		zoomreset = createItem("Reset zoom", "zoom", "Ctrl 0");

		view.addItem(zoomin);
		view.addItem(zoomout);
		view.addItem(separator);
		view.addItem(zoomreset);
		view.addItem(separator);
		view.addItem(fullscreen);

		history = createMenu("History", 54);
		recentlyaccessedparts = createItem("Recently accessed parts" + fill(17, 40), "brick", "F4");
		recentlyaccessedparts.setEnableIfCondition(ifsignedon);
		recentlyaccessedproducts = createItem("Recently accessed products" + fill(17, 40), "lorry", null);
		recentlyaccessedproducts.setEnableIfCondition(ifsignedon);
		previoussearch = createItem("Execute previous search", "back", "F7");
		previoussearch.setEnableIfCondition(ifsignedon);
		nextsearch = createItem("Execute next search", "forward", "F8");
		nextsearch.setEnableIfCondition(ifsignedon);
		resethistory = createItem("Reset all history and restart", "exclamation", null);
		resethistory.setEnableIfCondition(ifsignedon);
		history.addItem(recentlyaccessedparts);
		history.addItem(recentlyaccessedproducts);
		history.addItem(separator);
		history.addItem(previoussearch);
		history.addItem(nextsearch);
		history.addItem(separator);
		history.addItem(resethistory);

		window = createMenu("Window", 60);
		closealltabs = createItem("Close all windows" + fill(13, 40), null, "F9");
		recycle = createItem("Recycle windows", null, null);
		recycle.setChecked(true);
		recycle.setCheckIfCondition(new MenuItemIfFunction() {
			@Override public boolean execute(final Canvas target, final Menu menu, final MenuItem item) {
				return recyclewindows;
			}
		});
		window.addItem(closealltabs);
		window.addItem(recycle);
		window.addItem(separator);

		help = createMenu("Help", 40);
		contents = createItem("Contents" + fill(8, 40), null, "F1");
		about = createItem("About " + Context.get().versionStringShort(), null, null);
		help.addItem(contents);
		help.addItem(separator);
		help.addItem(about);

		staticmenus.add(file);
		staticmenus.add(search);
		staticmenus.add(view);
		staticmenus.add(history);
		staticmenus.add(window);
		staticmenus.add(help);
	}

	@Override public HasClickHandlers restart() {
		return restart;
	}

	@Override public HasClickHandlers signon() {
		return signon;
	}

	@Override public HasClickHandlers signoff() {
		return signoff;
	}

	@Override public HasClickHandlers exit() {
		return exit;
	}

	@Override public HasClickHandlers about() {
		return about;
	}

	@Override public HasClickHandlers fullscreen() {
		return fullscreen;
	}

	@Override public HasClickHandlers zoomin() {
		return zoomin;
	}

	@Override public HasClickHandlers zoomout() {
		return zoomout;
	}

	@Override public HasClickHandlers zoomreset() {
		return zoomreset;
	}

	@Override public HasClickHandlers closealltabs() {
		return closealltabs;
	}

	@Override public HasClickHandlers recycletabs() {
		return recycle;
	}

	@Override public HasClickHandlers recentlyaccessedparts() {
		return recentlyaccessedparts;
	}

	@Override public HasClickHandlers recentlyaccessedproducts() {
		return recentlyaccessedproducts;
	}

	@Override public HasClickHandlers previoussearch() {
		return previoussearch;
	}

	@Override public HasClickHandlers nextsearch() {
		return nextsearch;
	}

	@Override public HasClickHandlers resethistory() {
		return resethistory;
	}

	@Override public HasItemClickHandlers tabSelected() {
		return window;
	}

	@Override public HasClickHandlers saveas() {
		return saveas;
	}

	@Override public HasClickHandlers[] changeSearchType() {
		final HasClickHandlers[] result = { manpart, partnerparts, manproduct, mansnapshot, searchpreferences };
		return result;
	}

	@EventReceiver protected void signedOn(final SignedOnUser event) {
		if (event.isSuccess()) {
			signoff.setTitle(signoff.getTitle() + " " + event.getFullName());
		}
	}

	@EventReceiver protected void recycleTabs(final RecycleTabs event) {
		recyclewindows = event.recycle;
	}

	@EventReceiver protected void exportableSelected(final ExportableSelected event) {
		saveas.setEnabled(event.exportable);
		file.redraw();
	}

	void disableItem(final String name) {
		if (items.containsKey(name)) {
			items.get(name).setEnabled(false);
		}
	}

	void enableItem(final String name) {
		if (items.containsKey(name)) {
			items.get(name).setEnabled(true);
		}
	}

	void showMenu() {
		menubar.setMenus(staticmenus.toArray(new Menu[staticmenus.size()]));
	}

	private Menu createMenu(final String title, final int width) {
		final Menu menu = new Menu();
		menu.setShowShadow(true);
		menu.setShadowDepth(6);
		menu.setTitle(title);
		menu.setWidth(width);
		menu.setShowIcons(true);
		menu.setShowKeys(true);
		return menu;
	}

	private MenuItem createItem(final String title, final String icon, final String keytitle) {
		final String prefixedicon = null != icon ? "/content/icons/" + icon + ".png" : null;
		final MenuItem menuitem = new MenuItem(title, prefixedicon, keytitle);
		if (null != keytitle && 2 == keytitle.length() && keytitle.startsWith("F")) {
			final KeyIdentifier functionkey = new KeyIdentifier();
			functionkey.setKeyName(keytitle.toLowerCase());
			final KeyIdentifier[] keys = { functionkey };
			menuitem.setKeys(keys);
		}
		if (null == keytitle) {
			menuitem.setKeyTitle("\u00a0");
		}
		items.put(title, menuitem);
		return menuitem;
	}

	private String fill(final int length, final int max) {
		return MenuView.nbsp.substring(0, max - length);
	}

	private boolean isSignedOn() {
		return 0 < Context.get().getChallengeSecret().length();

	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		showMenu();
		if (event.isSuccess()) {
			final Menu navigate = createNavigateMenu(event.rowdetail, event.delimiter, event.currentrow, event.listgrid);
			navigate.setTitle("Navigate");
			navigate.setWidth(64);
			final Menu[] dynamicmenus = { navigate };
			menubar.addMenus(dynamicmenus, 3);
		}
	}

	@EventReceiver void tabsetChanged(final TabSetChanged event) {
		int i = 0;
		for (final MenuItem item : window.getItems()) {
			if (3 < ++i) {
				window.removeItem(item);
			}
		}
		for (final Tab tab : event.tabset.getTabs()) {
			window.addItem(new MenuItem(tab.getTitle()));
			if ("Dashboard".equals(tab.getTitle()) && 1 < event.tabset.getTabs().length) {
				window.addItem(separator);
			}
		}
	}

	private Menu createNavigateMenu(final Detail detail, final String delimiter, final int currentrow,
			final ListGrid listgrid) {
		if (null != contextmenu) {
			contextmenu.asMenu().destroy();
		}
		final de.man.mn.gep.client.workspace.searchresult.menu.MenuModel model = GWT
				.create(de.man.mn.gep.client.workspace.searchresult.menu.MenuModel.class);
		final de.man.mn.gep.client.workspace.searchresult.menu.MenuPresenter presenter = GWT
				.create(de.man.mn.gep.client.workspace.searchresult.menu.MenuPresenter.class);
		contextmenu = GWT.create(de.man.mn.gep.client.workspace.searchresult.menu.MenuView.class);
		model.setDetail(detail);
		model.setDelimiter(delimiter);
		model.setCurrentRow(currentrow);
		model.setListGrid(listgrid);
		return MVP.create(model, contextmenu, presenter).asMenu();
	}

	private MenuBar menubar;
	private final List<Menu> staticmenus = new LinkedList<Menu>();
	private final MenuItem separator = new MenuItemSeparator();
	private final Menu file;
	private final MenuItem saveas;
	private final MenuItem restart;
	private final MenuItem signon;
	private final MenuItem signoff;
	private final MenuItem exit;
	private final Menu view;
	private final MenuItem fullscreen;
	private final MenuItem zoomin;
	private final MenuItem zoomout;
	private final MenuItem zoomreset;
	private final Menu search;
	private final MenuItem manpart;
	private final MenuItem manproduct;
	private final MenuItem mansnapshot;
	private final MenuItem partnerparts;
	private final MenuItem searchpreferences;
	private final Menu history;
	private final MenuItem recentlyaccessedparts;
	private final MenuItem recentlyaccessedproducts;
	private final MenuItem previoussearch;
	private final MenuItem nextsearch;
	private final MenuItem resethistory;
	private final Menu window;
	private final MenuItem closealltabs;
	private final Menu help;
	private final MenuItem contents;
	private final MenuItem about;

	private MenuItem recycle;
	private boolean recyclewindows = true;

	private final Map<String, MenuItem> items = new LinkedHashMap<String, MenuItem>();
	private de.man.mn.gep.client.workspace.searchresult.menu.MenuView contextmenu;

	private static final String nbsp = "\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0";
}
