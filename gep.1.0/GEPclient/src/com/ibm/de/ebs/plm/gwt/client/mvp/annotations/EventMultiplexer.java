package com.ibm.de.ebs.plm.gwt.client.mvp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.event.shared.GwtEvent;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface EventMultiplexer {

	public Class<? extends GwtEvent<?>> value();

}
