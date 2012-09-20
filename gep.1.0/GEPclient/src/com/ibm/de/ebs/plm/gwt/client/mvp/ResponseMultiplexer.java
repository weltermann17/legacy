package com.ibm.de.ebs.plm.gwt.client.mvp;

public abstract class ResponseMultiplexer<D, E extends BusEvent<D>> {

	abstract public Response<D, E> multiplex();

}
