package com.ibm.de.ebs.plm.gwt.client.mvp;

import com.google.gwt.json.client.JSONValue;

public interface Storable {

	void store(final JSONValue data);

	JSONValue restore();

	void reset();

	String storageKey();

}
