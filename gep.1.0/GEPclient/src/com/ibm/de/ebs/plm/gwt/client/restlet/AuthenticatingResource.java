package com.ibm.de.ebs.plm.gwt.client.restlet;

import org.restlet.client.Uniform;
import org.restlet.client.engine.header.Header;
import org.restlet.client.engine.util.HeaderSeries;
import org.restlet.client.resource.ClientResource;
import org.restlet.client.util.Series;

import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;

public class AuthenticatingResource extends ClientResource {

	public AuthenticatingResource(final Uniform uniform, final String url, final boolean authenticate) {
		super(url);
		setOnResponse(uniform);
		setRetryAttempts(1);
		setRetryDelay(500);
		if (authenticate) {
			@SuppressWarnings("unchecked")
			Series<Header> headers = (Series<Header>) getRequest().getAttributes().get(
					AuthenticatingResource.headersconstant);
			if (null == headers) {
				headers = new HeaderSeries();
				getRequest().getAttributes().put(AuthenticatingResource.headersconstant, headers);
			}
			headers.add(BaseContext.get().getChallengeCustomHeader(), BaseContext.get().getBasicAuthorization());
		}
	}

	protected void dump() {
		@SuppressWarnings("unchecked")
		final Series<Header> headers = (Series<Header>) getRequest().getAttributes().get(
				AuthenticatingResource.headersconstant);
		final StringBuilder buf = new StringBuilder();
		for (final String name : headers.getNames()) {
			buf.append(name).append(" -> ").append(headers.getFirstValue(name)).append(" | ");
		}
		BaseContext.get().debug(buf.toString());
	}

	private final static String headersconstant = org.restlet.client.engine.header.HeaderConstants.ATTRIBUTE_HEADERS;

}
