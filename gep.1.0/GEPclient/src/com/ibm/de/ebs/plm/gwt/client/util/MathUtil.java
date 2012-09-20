package com.ibm.de.ebs.plm.gwt.client.util;

import com.google.gwt.user.client.Random;

public class MathUtil {

	public static int randomInt(final int modulo) {
		return Random.nextInt(modulo);
	}

	public static String randomString(final int length) {
		final StringBuilder b = new StringBuilder(length);
		for (int i = 0; i < length; ++i) {
			b.append(Character.valueOf((char) (65 + MathUtil.randomInt(26))));
		}
		return b.toString();
	}

}
