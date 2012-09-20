package com.ibm.de.ebs.plm.gwt.client.util;

import java.util.LinkedList;

import name.pehl.piriti.client.json.Json;

public abstract class History<E> {

	abstract protected String asString(final E value);

	public E getValue() {
		if (null == value && 0 < stack.size()) {
			value = stack.get(stack.size() - 1);
		}
		return value;
	}

	public void setValue(final E value) {
		this.value = value;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
		while (limit < stack.size()) {
			stack.remove(0);
		}
	}

	public int getLimit() {
		return limit;
	}

	public void setStack(final LinkedList<E> stack) {
		this.stack = stack;
	}

	public LinkedList<E> getStack() {
		return stack;
	}

	public String[] asStringArray() {
		final String[] values = new String[stack.size()];
		int i = stack.size();
		for (final E e : stack) {
			values[--i] = asString(e);
		}
		return values;
	}

	public void commitValue(final E value) {
		setValue(value);
		commitValue();
	}

	public void commitValue() {
		stack.remove(value);
		while (limit <= stack.size()) {
			stack.remove(0);
		}
		stack.add(value);
		cursor = value;
	}

	public E getPreviousValue() {
		final int i = stack.indexOf(cursor);
		cursor = 0 >= i ? getValue() : stack.get(i - 1);
		return cursor;
	}

	public E getNextValue() {
		int i = stack.indexOf(cursor);
		if (0 <= i) {
			if (stack.size() == i + 1) {
				i = -1;
			}
			cursor = stack.get(i + 1);
		} else {
			cursor = getValue();
		}
		return cursor;
	}

	private E value;
	private E cursor;
	@Json private LinkedList<E> stack = new LinkedList<E>();
	@Json private int limit = 20;

}
