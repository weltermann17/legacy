package de.man.mn.gep.client.main.signon;

import com.google.gwt.user.client.Window;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.ClickGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.ui.smartgwt.ImageWithZoom;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ItemKeyPressEvent;
import com.smartgwt.client.widgets.form.events.ItemKeyPressHandler;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.HasChangedHandlers;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import de.man.mn.gep.client.shared.event.SignedOn;

public abstract class SignOnView extends SectionStackSectionView implements SignOnPresenter.View {

	public SignOnView() {
		getSectionStackSection().setID("signonsection");
		getSectionStackSection().setTitle("Global Engineering Platform");
		getSectionStackSection().setExpanded(true);
		getSectionStackSection().setCanCollapse(true);

		form = new DynamicForm();
		getSectionStackSection().addItem(buildSignonScreen());

		menu = new Canvas();
		menu.setHeight(1);
		menu.setWidth(1);
		menu.addChild(buildSignOn());
		getSectionStackSection().setControls(menu);
	}

	public void setPlmServers(final String[] servers) {
		plmservers.setValueMap(servers);
		plmservers.setValue(servers[0]);
	}

	public void focus() {
		selectItem(username);
	}

	@Override public HasClickHandlers signonButton() {
		return signon;
	}

	@Override public HasClickHandlers userImage() {
		return image;
	}

	@Override public HasChangedHandlers passwordChanged() {
		return password;
	}

	@Override public HasChangedHandlers usernameChanged() {
		return username;
	}

	@RequestGesture(request = "SignOn", gesture = ClickGesture.class) abstract protected void signon();

	@EventReceiver protected void signedOn(final SignedOn event) {
		if (event.isSuccess()) {
			getSectionStack().removeSection(getSectionStackSection().getID());
		} else {
			signonerrors++;
			if (1 == signonerrors) {
				password.setValue("");
				Dialogs.error("Sign on failed",
						"Invalid user or password.<br><br>Too many attempts will lock this Windows user.");
			} else if (2 == signonerrors) {
				password.setValue("");
				Window.alert("Invalid user or password.\nToo many attempts will lock this Windows user.\nMore failed attempts will be reported with your Windows login and your computername.");
			} else {
				getSectionStack().removeSection(getSectionStackSection().getID());
				Window.alert("Security risk:\n\nThis attempt has been reported with your Windows login and your computername.");
			}
		}
	}

	private Button buildSignOn() {
		if (signon == null) {
			signon = new Button("Sign on");
			signon.setCanFocus(false);
			signon.setHeight(20);
			signon.setWidth(72);
			signon.setHoverWrap(false);
			signon.setPrompt("Enter user and password then click here to sign on.");
		}
		return signon;
	}

	private HLayout buildSignonScreen() {
		hlayout = new HLayout();
		hlayout.setOverflow(Overflow.HIDDEN);
		hlayout.setPadding(4);
		hlayout.setMembersMargin(10);
		hlayout.setBackgroundColor("#f0f8ff");
		hlayout.setSize("100%", "100px");
		form.setPixelSize(200, 92);
		form.setNumCols(2);
		form.setColWidths("60px", "140px");
		form.setItemLayout(FormLayoutType.TABLE);
		form.setHoverWrap(false);
		final SpacerItem spacer = new SpacerItem();
		spacer.setHeight(4);
		form.setFields(buildUsername(), buildPassword(), spacer, buildPlmServers());
		form.addItemKeyPressHandler(new ItemKeyPressHandler() {
			@Override public void onItemKeyPress(final ItemKeyPressEvent event) {
				if ("Enter".equals(event.getKeyName())) {
					signon();
				}
			}
		});
		hlayout.addMember(form);
		hlayout.addMember(buildImage());
		return hlayout;
	}

	private TextItem buildUsername() {
		username = new TextItem("User");
		username.setWidth(140);
		username.setTitleAlign(Alignment.LEFT);
		username.setSelectOnFocus(true);
		username.setKeyPressFilter("[A-za-z0-9]");
		username.setChangeOnKeypress(true);
		username.setLength(5);
		username.addKeyPressHandler(new KeyPressHandler() {
			@Override public void onKeyPress(final KeyPressEvent event) {
				if (event.getKeyName().equals("Enter")) {
					selectItem(password);
				}
			}
		});
		username.addChangedHandler(new ChangedHandler() {
			@Override public void onChanged(final ChangedEvent event) {
				final String value = event.getItem().getValue().toString().toUpperCase();
				username.setValue(value);
				if (value.length() < 5) {
					image.setUrl(null);
				} else {
					image.setUrl("/redirect/images/users/" + value.toLowerCase() + "/");
				}
			}
		});
		return username;
	}

	private PasswordItem buildPassword() {
		password = new PasswordItem("Password");
		password.setWidth(140);
		password.setTitleAlign(Alignment.LEFT);
		password.setChangeOnKeypress(true);
		password.setPrompt("Press 'Enter' to sign on or click on the 'Sign on' button.");
		return password;
	}

	private SelectItem buildPlmServers() {
		plmservers = new SelectItem("enovia");
		plmservers.setWidth(140);
		plmservers.setTitle("Enovia V5");
		plmservers.setTitleAlign(Alignment.LEFT);
		plmservers
				.setPrompt("This sends a message to your active CATIA V5 session and connects it to the ENOVIA V5 selected here. If CATIA V5 is not running select 'No connection'.");
		plmservers.setDefaultToFirstOption(true);
		return plmservers;
	}

	private Canvas buildImage() {
		image = new ImageWithZoom(true, 82, 92);
		return image;
	}

	private Button signon;
	private SelectItem plmservers;
	private TextItem username;
	private PasswordItem password;
	private ImageWithZoom image;
	private HLayout hlayout;
	private final Canvas menu;
	private final DynamicForm form;
	private int signonerrors = 0;

}
