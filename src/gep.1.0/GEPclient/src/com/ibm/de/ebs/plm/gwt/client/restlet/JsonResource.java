package com.ibm.de.ebs.plm.gwt.client.restlet;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.CacheDirective;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.data.Status;
import org.restlet.client.ext.json.JsonRepresentation;
import org.restlet.client.representation.Representation;
import org.restlet.client.resource.ClientResource;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.ibm.de.ebs.plm.gwt.client.ui.smartgwt.RestDataSourceGrid;
import com.ibm.de.ebs.plm.gwt.client.util.BaseContext;
import com.ibm.de.ebs.plm.gwt.client.util.DateUtil;
import com.smartgwt.client.util.SC;

public class JsonResource implements Uniform {

	public static JsonResource get(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final boolean authenticate) {
		return JsonResource.get(url, response, "", "", authenticate);
	}

	public static JsonResource get(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final String prompt, final String errormessage, final boolean authenticate) {
		return JsonResource.handle(Method.GET, url, response, null, prompt, errormessage, authenticate);
	}

	public static JsonResource head(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final String prompt, final String errormessage, final boolean authenticate) {
		return JsonResource.handle(Method.HEAD, url, response, null, prompt, errormessage, authenticate);
	}

	public static JsonResource delete(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final String prompt, final String errormessage, final boolean authenticate) {
		return JsonResource.handle(Method.DELETE, url, response, null, prompt, errormessage, authenticate);
	}

	public static JsonResource post(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final JSONValue input, final String prompt, final String errormessage, final boolean authenticate) {
		return JsonResource.handle(Method.POST, url, response, input, prompt, errormessage, authenticate);
	}

	public static JsonResource put(final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final JSONValue input, final String prompt, final String errormessage, final boolean authenticate) {
		return JsonResource.handle(Method.PUT, url, response, input, prompt, errormessage, authenticate);
	}

	public static JsonResource handle(final Method method, final String url,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final JSONValue input, final String prompt, final String errormessage, final boolean authenticate) {
		final MediaType mediatype = MediaType.APPLICATION_JSON;
		final String key = url + mediatype.toString();
		final long now = DateUtil.now().getTime();
		if (Method.GET.equals(method) && now > JsonResource.nocacheuntil
				&& JsonResource.cachedresponses.containsKey(key) && now < JsonResource.cachedexpires.get(key).getTime()) {
			final JsonResource jsonresource = new JsonResource(url, mediatype, response, "", errormessage, true);
			final Response cachedresponse = new Response(null);
			cachedresponse.setStatus(Status.SUCCESS_OK);
			final Representation representation = new JsonRepresentation(MediaType.APPLICATION_JSON,
					JsonResource.cachedresponses.get(key));
			representation.setExpirationDate(JsonResource.cachedexpires.get(key));
			cachedresponse.setEntity(representation);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override public void execute() {
					jsonresource.handle(null, cachedresponse);
				}
			});
			return jsonresource;
		} else {
			if (JsonResource.cachedresponses.containsKey(key)) {
				JsonResource.cachedresponses.remove(key);
			}
			final JsonResource jsonresource = new JsonResource(url, mediatype, response, prompt, errormessage, false);
			final ClientResource resource = new AuthenticatingResource(jsonresource, url, authenticate);
			if (Method.GET.equals(method)) {
				if (now <= JsonResource.nocacheuntil) {
					final List<CacheDirective> cachedirectives = resource.getRequest().getCacheDirectives();
					cachedirectives.add(CacheDirective.noCache());
					cachedirectives.add(CacheDirective.noStore());
					cachedirectives.add(CacheDirective.maxAge(0));
				}
				resource.get(mediatype);
			} else if (Method.HEAD.equals(method)) {
				resource.head(mediatype);
			} else if (Method.DELETE.equals(method)) {
				resource.delete(mediatype);
			} else if (Method.POST.equals(method)) {
				resource.post(new JsonRepresentation(MediaType.APPLICATION_JSON, input));
			} else if (Method.PUT.equals(method)) {
				resource.put(new JsonRepresentation(MediaType.APPLICATION_JSON, input));
			}
			JsonResource.noCache(method);
			return jsonresource;
		}
	}

	private JsonResource(final String url, final MediaType mediatype,
			final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> response,
			final String prompt, final String errormessage, final boolean cached) {
		this.url = url;
		this.mediatype = mediatype;
		delegate = null != response ? response : null;
		this.errormessage = errormessage;
		this.cached = cached;
		this.prompt = prompt;
		show();
	}

	@Override public void handle(final Request request, final Response response) {
		clear();
		Representation representation = null;
		try {
			final Status status = response.getStatus();
			if (status.isSuccess()) {
				final JsonRepresentation json = new JsonRepresentation(response.getEntity());
				final JSONValue jsonvalue = json.getValue();
				representation = json;
				if (!cached && null != response.getEntity().getExpirationDate()) {
					final String key = url + mediatype.toString();
					JsonResource.cachedresponses.put(key, jsonvalue);
					JsonResource.cachedexpires.put(key, response.getEntity().getExpirationDate());
				}
				if (null == delegate) {
					Dialogs.warn("Success, but no handler: " + url);
				} else {
					if (!cancelled) {
						delegate.onSuccess(jsonvalue);
					}
				}
			} else {
				representation = response.getEntity();
				final String message = 0 < errormessage.length() ? errormessage : "Failure: " + response.getStatus()
						+ ", " + url;
				if (null == delegate) {
					Dialogs.warn(message);
				} else {
					if (!cancelled) {
						delegate.onFailure(message);
					}
				}
			}
		} catch (final Throwable e) {
			if (null == delegate) {
				Dialogs.warn("Failure: " + url + ", " + e);
			} else {
				if (!cancelled) {
					delegate.onFailure(url + " : " + e);
				}
			}
		} finally {
			if (null != representation) {
				try {
					representation.exhaust();
					representation.release();
				} catch (final IOException e) {
				}
			}
		}
	}

	public void cancel() {
		cancelled = true;
		clear();
	}

	private static void noCache(final Method method) {
		if (Method.PUT.equals(method) || Method.POST.equals(method) || Method.DELETE.equals(method)) {
			JsonResource.nocacheuntil = DateUtil.now().getTime() + JsonResource.expires;
			RestDataSourceGrid.noCache(JsonResource.nocacheuntil);
		}
	}

	private final void show() {
		if (0 < prompt.length()) {
			timer = new Timer() {
				@Override public void run() {
					timer = null;
					SC.showPrompt(prompt);
					JsonResource.prompts++;
					new Timer() {
						@Override public void run() {
							clear();
						}
					}.schedule(BaseContext.get().timeout());
				}
			};
			timer.schedule(BaseContext.get().veryLongUiTimeout());
		}
	}

	private final void clear() {
		if (0 < prompt.length()) {
			if (null != timer) {
				timer.cancel();
				timer = null;
			}
			if (0 < JsonResource.prompts) {
				JsonResource.prompts--;
				if (0 == JsonResource.prompts) {
					SC.clearPrompt();
				}
			}
			prompt = "";
		}
	}

	private final String url;
	private final MediaType mediatype;
	private final com.ibm.de.ebs.plm.gwt.client.mvp.Response<JSONValue, ? extends BusEvent<JSONValue>> delegate;
	private final String errormessage;
	private final boolean cached;
	private String prompt;
	private Timer timer = null;
	private boolean cancelled = false;

	private static int prompts = 0;
	private final static Map<String, JSONValue> cachedresponses = new LinkedHashMap<String, JSONValue>();
	private final static Map<String, Date> cachedexpires = new LinkedHashMap<String, Date>();
	private static long nocacheuntil = DateUtil.now().getTime();
	private final static long expires = 1000L; // 5L * 60L * 1000L;

}
