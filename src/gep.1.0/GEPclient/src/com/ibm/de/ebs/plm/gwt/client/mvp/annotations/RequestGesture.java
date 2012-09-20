package com.ibm.de.ebs.plm.gwt.client.mvp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ibm.de.ebs.plm.gwt.client.mvp.Gesture;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface RequestGesture {

	public String request();

	public Class<? extends Gesture> gesture();

}
