package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.json.client.JSONValue;

public abstract class InitializedModel extends BusEvent<JSONValue> {

	Class<? extends BaseModel> modelclass;

	BaseModel model;

	@Override public String toString() {
		return modelclass.getName().replace("Impl", "") + " initialized (" + id + ")";
	}
}
