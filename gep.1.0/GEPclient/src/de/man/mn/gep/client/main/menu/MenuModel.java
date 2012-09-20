package de.man.mn.gep.client.main.menu;

import org.restlet.client.data.MediaType;

import com.google.gwt.core.client.GWT;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.WindowUtil;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ItemClickEvent;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import de.man.mn.gep.client.search.searchpanel.SetSelectedSearchTab;
import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.event.ChangeSearchType;
import de.man.mn.gep.client.shared.event.CloseAllTabs;
import de.man.mn.gep.client.shared.event.FocusSignOn;
import de.man.mn.gep.client.shared.event.NextSearch;
import de.man.mn.gep.client.shared.event.PreviousSearch;
import de.man.mn.gep.client.shared.event.RecycleTabs;
import de.man.mn.gep.client.shared.event.RemoteSearch;
import de.man.mn.gep.client.shared.event.SaveFile;
import de.man.mn.gep.client.shared.event.SetSelectedTab;
import de.man.mn.gep.client.shared.event.SignedOff;

public class MenuModel extends BaseModel {

	@RequestReceiver("Restart") void restart(final Request<MenuItemClickEvent> request) {
		WindowUtil.reloadWithoutRefresh();
	}

	@RequestReceiver("SignOn") @EventSender(FocusSignOn.class) void signon(final Request<MenuItemClickEvent> request,
			final Response<Object, FocusSignOn> response) {
		response.success();
	}

	@RequestReceiver("SignOff") @EventSender(SignedOff.class) void signoff(final Request<MenuItemClickEvent> request,
			final Response<Object, SignedOff> response) {
		response.success();
	}

	@RequestReceiver("Exit") void exit(final Request<MenuItemClickEvent> request) {
		WindowUtil.closeWindow();
	}

	@RequestReceiver("About") void about(final Request<MenuItemClickEvent> request) {
		final int e = GWT.getHostPageBaseURL().length();
		final String port = GWT.getHostPageBaseURL().substring(e - 5, e - 1);
		String environment = "Unknown";
		if (port.startsWith("8") || port.startsWith("18")) {
			environment = "Production";
		} else if (port.startsWith("7") || port.startsWith("17")) {
			environment = "Test";
		} else if (port.startsWith("6") || port.startsWith("16")) {
			environment = "Integration";
		}
		Dialogs.info("This is version " + Context.get().versionString() + ".<br><br>(" + environment + " environment)");
	}

	@RequestReceiver("Fullscreen") void fullscreen(final Request<MenuItemClickEvent> request) {
		Dialogs.info("Press F11 to toggle fullscreen mode on or off.");
	}

	@RequestReceiver("ZoomIn") void zoomin(final Request<MenuItemClickEvent> request) {
		Dialogs.info("Press Ctrl + to zoom in.");
	}

	@RequestReceiver("ZoomOut") void zoomout(final Request<MenuItemClickEvent> request) {
		Dialogs.info("Press Ctrl - to zoom in.");
	}

	@RequestReceiver("ZoomReset") void zoomreset(final Request<MenuItemClickEvent> request) {
		Dialogs.info("Press Ctrl 0 to reset zooming to 100%.");
	}

	@RequestReceiver("RecentlyAccessedParts") @EventSender(RemoteSearch.class) void recentlyaccessedParts(
			final Request<MenuItemClickEvent> request, final Response<Object, RemoteSearch> response) {
		recentlyAccessed(DataType.versions, response);
	}

	@RequestReceiver("RecentlyAccessedProducts") @EventSender(RemoteSearch.class) void recentlyaccessedProducts(
			final Request<MenuItemClickEvent> request, final Response<Object, RemoteSearch> response) {
		recentlyAccessed(DataType.products, response);
	}

	private void recentlyAccessed(final DataType datatype, final Response<Object, RemoteSearch> response) {
		response.event().datatype = datatype;
		response.event().criterion = "Recently accessed";
		response.success();
	}

	@RequestReceiver("PreviousSearch") @EventSender(PreviousSearch.class) void previoussearch(
			final Request<MenuItemClickEvent> request, final Response<Object, PreviousSearch> response) {
		response.success();
	}

	@RequestReceiver("NextSearch") @EventSender(NextSearch.class) void nextsearch(
			final Request<MenuItemClickEvent> request, final Response<Object, NextSearch> response) {
		response.success();
	}

	@RequestReceiver("ResetHistory") @EventSender(SignedOff.class) void resetAll(
			final Request<MenuItemClickEvent> request, final Response<Object, SignedOff> response) {
		SC.confirm("Confirm", "Do you really want to reset all history and restart the application?",
				new BooleanCallback() {
					@Override public void execute(final Boolean yes) {
						if (yes) {
							response.event().withresethistory = true;
							response.success();
						}
					}
				});
	}

	@RequestReceiver("CloseAllTabs") @EventSender(CloseAllTabs.class) void closealltabs(
			final Request<MenuItemClickEvent> request, final Response<Object, CloseAllTabs> response) {
		response.success();
	}

	@RequestReceiver("RecycleTabs") @EventSender(RecycleTabs.class) void recycletabs(
			final Request<MenuItemClickEvent> request, final Response<Object, RecycleTabs> response) {
		response.event().recycle = !((MenuItem) request.gesture().getSource()).getChecked();
		response.success();
	}

	@RequestReceiver("TabSelected") @EventSender(SetSelectedTab.class) void tabSelected(
			final Request<ItemClickEvent> request, final Response<Object, SetSelectedTab> response) {
		final Menu windowMenu = (Menu) request.gesture().getSource();
		final MenuItem clickedMenuItem = request.gesture().getItem();
		int index = windowMenu.getItemNum(clickedMenuItem) - 3;
		if (index < 0) {
			return;
		}
		final int countMenuItems = windowMenu.getItems().length;
		if (clickedMenuItem.getTitle().equalsIgnoreCase("Dashboard")) {
			index = 0;
		} else if (countMenuItems > 4) {
			// Remove 2nd separator from count to match tab index
			index -= 1;
		}
		response.event().index = index;
		response.success();
	}

	@RequestReceiver("SaveAs") @EventSender(SaveFile.class) void saveas(final Request<MenuItemClickEvent> request,
			final Response<Object, SaveFile> response) {
		response.event().mediatype = MediaType
				.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.success();
	}

	@RequestReceiver("ChangeSearchType") @EventSender(ChangeSearchType.class) void changeSearchType(final int index,
			final Request<MenuItemClickEvent> request, final Response<Object, ChangeSearchType> response) {
		final SetSelectedSearchTab setSelectedTab = GWT.create(SetSelectedSearchTab.class);
		try {
			ChangeSearchType.AcceptedTypes.valueOf(request.gesture().getItem().getTitle());
			setSelectedTab.index = 0;
			response.event().type = request.gesture().getItem().getTitle();
			response.success();
		} catch (final IllegalArgumentException e) {
			setSelectedTab.index = 1;
		}
		setSelectedTab.fire();
	}
}
