package de.man.mn.gep.client.search.searchpreferences;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.InitializedModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;

import de.man.mn.gep.client.shared.dao.Location;
import de.man.mn.gep.client.shared.event.LocationsChanged;
import de.man.mn.gep.client.shared.event.SignedOff;

public abstract class SearchPreferencesModel extends BaseModel {

	@Override protected void initialize(final Response<JSONValue, InitializedModel> response) {
		JsonResource.get("/static/model/searchpref/", response, false);
	}

	@Override protected void onInit(final JSONValue data) {
		final JSONArray array = data.isArray();
		locations = new Location[array.size()];
		for (int i = 0; i < array.size(); ++i) {
			final JSONObject location = array.get(i).isObject();
			locations[i] = new Location(location.get("shortname").isString().stringValue(), location.get("title")
					.isString().stringValue(), location.get("url").isString().stringValue(), location.get("enabled")
					.isBoolean().booleanValue(), location.get("selected").isBoolean().booleanValue());
		}
		fromJSON(restore());
	}

	public Location[] getLocations() {
		return locations;
	}

	public Location[] getSelectedLocations() {
		final Location[] selectedlocations = new Location[getSelectionCount()];
		int i = 0;
		for (final Location location : locations) {
			if (location.selected()) {
				selectedlocations[i++] = location;
			}
		}
		return selectedlocations;
	}

	public Boolean[] getSelection() {
		final Boolean[] selection = new Boolean[locations.length];
		int i = 0;
		for (final Location location : locations) {
			selection[i++] = location.selected();
		}
		return selection;
	}

	public int getSelectionCount() {
		int count = 0;
		for (final Location location : locations) {
			if (location.selected()) {
				++count;
			}
		}
		return count;
	}

	@RequestReceiver("LocationChanged") @EventSender(LocationsChanged.class) void locationChanged(final int i,
			final Request<ChangedEvent> request, final Response<Object, LocationsChanged> response) {
		locations[i].select((Boolean) request.gesture().getValue());
		response.event().locations = getLocations();
		response.event().selectedlocations = getSelectedLocations();
		response.event().selection = getSelection();
		response.success();
		store(toJSON());
	}

	@EventReceiver void signedOff(final SignedOff event) {
		reset();
	}

	private JSONValue toJSON() {
		final StringBuilder buf = new StringBuilder();
		buf.append("[");
		int i = 0;
		for (final Location location : locations) {
			if (0 < i++) {
				buf.append(",");
			}
			buf.append(location.selected());
		}
		buf.append("]");
		return JSONParser.parseStrict(buf.toString());
	}

	private void fromJSON(final JSONValue data) {
		if (null != data) {
			final JSONArray restored = data.isArray();
			if (locations.length == restored.size()) {
				int i = 0;
				for (final Location location : locations) {
					location.select(restored.get(i++).isBoolean().booleanValue());
				}
			}
		}
	}

	private Location[] locations;

}
