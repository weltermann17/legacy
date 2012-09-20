package de.man.mn.gep.client.details.user;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.CanvasView;
import com.ibm.de.ebs.plm.gwt.client.ui.smartgwt.ImageWithZoom;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.HasClickHandlers;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class UserView extends CanvasView implements UserPresenter.View {

	public UserView() {
		final VLayout vlayout = new VLayout();
		vlayout.setPadding(4);
		vlayout.setSize("100%", "100%");
		final Canvas icanvas = new Canvas();
		icanvas.setPadding(4);
		icanvas.setPixelSize(128, 128);
		image = new ImageWithZoom(true, 120, 120);
		image.setSize("100%", "100%");
		image.addResizedHandler(new ResizedHandler() {
			@Override public void onResized(final ResizedEvent event) {
				final Canvas i = (Canvas) event.getSource();
				icanvas.setHeight(i.getHeight());
			}
		});
		icanvas.addChild(image);
		vlayout.addMember(icanvas);
		form = new DynamicForm();
		form.setSize("100%", "*");
		form.setNumCols(2);
		form.setColWidths("40%", "*");
		form.setItemLayout(FormLayoutType.TABLE);
		form.setFixedColWidths(false);
		form.setAutoFetchData(false);
		manager = new LinkItem("manager");
		manager.setTitle("Manager");
		manager.setTarget("javascript");
		vlayout.addMember(form);
		asCanvas().addChild(vlayout);
		tab = new Tab("userdetail" + ++UserView.counter);
		tab.setCanClose(true);
		tab.setPane(asCanvas());
	}

	@Override protected void onInit(final BusEvent<?> event) {
		final UserSelected e = (UserSelected) event;
		form.setUseAllDataSourceFields(true);
		form.setDataSource(e.datasource);
		form.fetchData();
		final StaticTextItem id = new StaticTextItem("id", "User id");
		id.setTextBoxStyle(Context.get().boldStyle());
		final StaticTextItem lastname = new StaticTextItem("lastname", "Last name");
		lastname.setTextBoxStyle(Context.get().boldStyle());
		final StaticTextItem firstname = new StaticTextItem("firstname", "First name");
		firstname.setTextBoxStyle(Context.get().boldStyle());
		final StaticTextItem phone = new StaticTextItem("phone", "Phone");
		final StaticTextItem mail = new StaticTextItem("mailto", "eMail");
		final StaticTextItem department = new StaticTextItem("department", "Department");
		final StaticTextItem managerlastname = new StaticTextItem("managerlastname", "Last name");
		final StaticTextItem managerfirstname = new StaticTextItem("managerfirstname", "First name");
		final StaticTextItem building = new StaticTextItem("building", "Building");
		final StaticTextItem room = new StaticTextItem("room", "Room");
		final StaticTextItem company = new StaticTextItem("company", "Company");
		final StaticTextItem street = new StaticTextItem("street", "Street");
		final StaticTextItem zipcode = new StaticTextItem("zipcode", "Zipcode");
		final StaticTextItem city = new StaticTextItem("city", "City");
		final SpacerItem spacer = new SpacerItem();
		form.setFields(spacer, lastname, firstname, spacer, phone, mail, department, building, room, company, street,
				zipcode, city, spacer, id, spacer, spacer, manager, managerlastname, managerfirstname);
		for (final FormItem item : form.getFields()) {
			item.setTitleAlign(Alignment.LEFT);
		}
		image.setUrl("/redirect/images/users/" + e.user + "/");
		tab.setTitle("User: " + e.user.toUpperCase());
	}

	public Tab asTab() {
		return tab;
	}

	@Override public HasClickHandlers manager() {
		return manager;
	}

	private static int counter = 0;
	private final Tab tab;
	private final DynamicForm form;
	private final ImageWithZoom image;
	private final LinkItem manager;

}
