package com.ibm.de.ebs.plm.gwt.client.util;

import java.util.Date;

public class DateUtil {

	public static Date now() {
		return new Date();
	}

	@SuppressWarnings("deprecation") public static Date today() {
		final Date now = new Date();
		now.setHours(0);
		now.setMinutes(0);
		now.setSeconds(0);
		return now;
	}

	public static Date addDays(final Date date, final long days) {
		final long millis = date.getTime();
		final long delta = days * 24L * 60L * 60L * 1000L;
		return new Date(millis + delta);
	}

}
