package com.ibm.de.ebs.plm.gwt.client.restlet;

import com.google.gwt.core.client.GWT;

public class BaseReference extends org.restlet.client.data.Reference {

	public BaseReference() {
		super(BaseReference.base);
	}

	public BaseReference(final String url, final int port) {
		super(BaseReference.base + url.substring(1));
		setHostPort(port);
	}

	private static final org.restlet.client.data.Reference base = new org.restlet.client.data.Reference(
			GWT.getHostPageBaseURL());
}
