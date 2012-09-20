package com.ibm.de.ebs.plm.gwt.client.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class BaseContext {

	public static BaseContext get() {
		return BaseContext.instance;
	}

	protected static void set(final BaseContext instance) {
		BaseContext.instance = instance;
	}

	public abstract int timeout();

	public abstract int longTimeout();

	public int shortUiTimeout() {
		return 100;
	}

	public int longUiTimeout() {
		return 500;
	}

	public int veryLongUiTimeout() {
		return 2000;
	}

	public String getChallengeLogin() {
		return asString("challengelogin");
	}

	public String getChallengeSecret() {
		return asString("challengesecret");
	}

	public String getChallengeCustomHeader() {
		return "X-Authorization";
	}

	public String getBasicAuthorization() {
		return "Basic " + StringUtil.toBase64(getChallengeLogin() + ":" + getChallengeSecret());
	}

	public String getDigestAuthorization() {
		return "not implemented";
	}

	public void setChallenge(final String login, final String secret) {
		add("challengelogin", login);
		add("challengesecret", secret);
	}

	public void add(final String name, final Object value) {
		context.put(name, value);
	}

	public void add(final String name, final boolean value) {
		context.put(name, new Boolean(value));
	}

	public void add(final String name, final int value) {
		context.put(name, new Integer(value));
	}

	public void add(final String name, final double value) {
		context.put(name, new Double(value));
	}

	public Object asObject(final String name) {
		return context.get(name);
	}

	public boolean asBoolean(final String name) {
		if (context.containsKey(name)) {
			final Boolean v = (Boolean) context.get(name);
			return v.booleanValue();
		} else {
			return false;
		}
	}

	public int asInt(final String name) {
		if (context.containsKey(name)) {
			final Integer v = (Integer) context.get(name);
			return v.intValue();
		} else {
			return -1;
		}
	}

	public double asDouble(final String name) {
		if (context.containsKey(name)) {
			final Double v = (Double) context.get(name);
			return v.doubleValue();
		} else {
			return 0.;
		}
	}

	public String asString(final String name) {
		if (context.containsKey(name)) {
			final String v = (String) context.get(name);
			return v;
		} else {
			return "";
		}
	}

	public Logger getLogger() {
		return Logger.getLogger("GEPclient");
	}

	public void debug(final String message) {
		getLogger().info(message);
	}

	@Override public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append("( ");
		for (final Map.Entry<String, Object> e : context.entrySet()) {
			buf.append(", ");
			buf.append(e.getKey());
			buf.append(" -> ");
			buf.append(null != e.getValue() ? e.getValue().toString() : "null");
		}
		buf.append(" )");
		return buf.toString();
	}

	private static BaseContext instance;
	private final Map<String, Object> context = new LinkedHashMap<String, Object>();

}
