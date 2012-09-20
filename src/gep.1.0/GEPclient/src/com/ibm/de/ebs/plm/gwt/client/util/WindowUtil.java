package com.ibm.de.ebs.plm.gwt.client.util;

public final class WindowUtil {

	public static final native void reloadWithoutRefresh()
	/*-{
		$wnd.location = $wnd.location;
	}-*/;

	public static final native void reloadWithRefresh()
	/*-{
		$wnd.location.reload();
	}-*/;

	public static final native void openUrl(final String url)
	/*-{
		$wnd.location = url;
	}-*/;

	public static final native void closeWindow()
	/*-{
		if (null != $wnd.top) {
			$wnd.top.opener = null;
		}
		$wnd.close();
	}-*/;

	public static final native void toggleFullScreen()
	/*-{
		$wnd.fullScreen = true;
	}-*/;

	public static final String openInNewWindow(final String url) {
		return "<script language='JavaScript'><!-- function load() { var l = window.open('"
				+ url
				+ "','','scrollbars=no,menubar=no,height=800,width=800,resizable=yes,toolbar=no,location=no,status=no');} --></script>";
	}

	public static final String changeLocation(final String url) {
		return "<script language='JavaScript'><!-- function load() { window.alert('" + url + "');} --></script>";
	}

}
