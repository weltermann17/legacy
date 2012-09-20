package de.man.mn.gep.client.shared.dao;

public class Location {

	public Location(final String shortname, final String title, final String url, final boolean enabled,
			final boolean selected) {
		this.shortname = shortname;
		this.title = title;
		this.url = url;
		this.enabled = enabled;
		this.selected = selected;
	}

	public boolean selected() {
		return selected;
	}

	public void select(final boolean value) {
		selected = value;
	}

	public final String shortname;
	public final String title;
	public final String url;
	public final boolean enabled;

	private boolean selected;

}
