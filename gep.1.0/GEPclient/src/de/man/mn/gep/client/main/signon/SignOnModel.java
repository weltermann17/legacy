package de.man.mn.gep.client.main.signon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.ibm.de.ebs.plm.gwt.client.util.StringUtil;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.SignedOff;
import de.man.mn.gep.client.shared.event.SignedOn;
import de.man.mn.gep.client.shared.event.SignedOnUser;
import de.man.mn.gep.client.shared.event.UserSelected;

public abstract class SignOnModel extends BaseModel {

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/model/plmservers/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {
		try {
			final JSONObject stored = restore().isObject();
			username = stored.get("username").isString().stringValue();
			password = StringUtil.fromBase64(stored.get("password").isString().stringValue());
			final long expires = (long) stored.get("expires").isNumber().doubleValue();
			final boolean stillvalid = DateUtil.now().getTime() < expires;
			if (stillvalid && 0 < username.length() && 0 < password.length()) {
				final SignedOn event = GWT.create(SignedOn.class);
				signOn(null, new Response<JSONValue, SignedOn>(event));
			}
		} catch (final Exception e) {
		}
		final JSONArray servers = data.isArray();
		plmservers = new String[servers.size()];
		for (int i = 0; i < servers.size(); ++i) {
			plmservers[i] = servers.get(i).isObject().get("name").isString().stringValue();
		}
	}

	public String[] getPlmServers() {
		return plmservers;
	}

	public String getPassword() {
		return password;
	}

	@RequestReceiver("UsernameChanged") void usernameChanged(final Request<ChangedEvent> request) {
		username = request.gesture().getValue().toString().toUpperCase();
	}

	@RequestReceiver("PasswordChanged") void passwordChanged(final Request<ChangedEvent> request) {
		password = request.gesture().getValue().toString();
	}

	@RequestReceiver("SignOn") @EventSender(SignedOn.class) <E extends GwtEvent<?>> void signOn(
			final Request<?> request, final Response<JSONValue, SignedOn> response) {
		if ("starten".equals(password) || username.equalsIgnoreCase(password)) {
			Dialogs.error("Security problem",
					"Sorry, but this password is too simple. <br><br>Please change your Windows login password to something more secure.");
		} else {
			Context.get().setChallenge(username, password);
			response.event().setSecret(password);
			JsonResource.get("/users/signon/" + username.toLowerCase() + "/" + StringUtil.toCryptHexString(password)
					+ "/", response, "Signing on ... (ActiveDirectory may be busy)",
					"Sign on failed. Service not available.", false);
		}
	}

	@EventReceiver void signedOff(final SignedOff event) {
		reset();
	}

	@EventReceiver @EventSender(SignedOnUser.class) void signedOn(final SignedOn event,
			final Response<JSONValue, SignedOnUser> response) {
		storeExpiration(event);
		if (event.isSuccess()) {
			JsonResource.get("/users/details/" + username.toLowerCase() + "/", response, true);
		}
	}

	@RequestReceiver("CellClicked") @EventSender(UserSelected.class) void userSelected(
			final Request<CellClickEvent> request, final Response<JSONValue, UserSelected> response) {
		final CellClickEvent gesture = request.gesture();
		gesture.getColNum();
		final Detail detail = Detail.create(gesture.getRecord());
		final ListGridField field = ((ListGrid) gesture.getSource()).getField(gesture.getColNum());
		if (field.getAttributeAsBoolean("islink") && "user".equalsIgnoreCase(field.getAttribute("linktype"))) {
			final String fieldname = field.getName();
			final String user = detail.getAttribute(fieldname);
			if (null != user) {
				response.event().user = user;
				response.event().datasource = new RestletDataSource(detail.getLink(fieldname), response, "", user
						+ " not found.");
			}
		}
	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		storeExpiration(event);
	}

	private void storeExpiration(final JSONValueEvent event) {
		if (event.isSuccess()) {
			final JSONObject data = new JSONObject();
			data.put("username", new JSONString(username));
			data.put("password", new JSONString(StringUtil.toBase64(password)));
			final long onehour = 1 * 60 * 60 * 1000;
			final long expires = DateUtil.now().getTime() + onehour;
			data.put("expires", new JSONNumber(expires));
			store(data);
		}
	}

	private String username;
	private String password;
	private String[] plmservers;

}
