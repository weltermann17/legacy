package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventMultiplexer;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;

public class ModelGenerator extends BaseGenerator {

	public ModelGenerator(final TreeLogger logger, final GeneratorContext context, final JClassType classtype,
			final boolean withlogging) {
		super(logger, context, classtype, withlogging);
	}

	@Override protected void onGenerate() throws UnableToCompleteException {
		eventSenders();
		eventMultiplexers();
		eventReceivers("onInitBusEventHandlers");
	}

	public Collection<String> findRequestReceiverMethod(final String request) {
		final Collection<String> result = new LinkedList<String>();
		for (final JMethod method : classtype.getOverridableMethods()) {
			if (method.isAnnotationPresent(RequestReceiver.class)) {
				final RequestReceiver requestreceiver = method.getAnnotation(RequestReceiver.class);
				if (request.equals(requestreceiver.value())) {
					result.add(method.getName());
				}
			}
		}
		return result;
	}

	private void eventSenders() throws UnableToCompleteException {
		final Collection<JMethod> eventsendermethods = getMethodsWithAnnotation(EventSender.class);
		for (final JMethod m : eventsendermethods) {
			final String decl = m.getReadableDeclaration();
			final int b = decl.indexOf(Response.class.getCanonicalName());
			final String newdecl = decl.substring(0, b - 2) + ")";
			final String responsetype = decl.substring(b, decl.indexOf("> ", b) + 1);
			final EventSender eventsender = m.getAnnotation(EventSender.class);
			final EventReceiver eventreceiver = m.getAnnotation(EventReceiver.class);
			final RequestReceiver requestreceiver = m.getAnnotation(RequestReceiver.class);
			if (null == eventreceiver && null != requestreceiver) {
				final String eventclass = eventsender.value().getCanonicalName();
				String indexparameter = null;
				String requestparameter;
				final boolean withindex = "int".equals(m.getParameterTypes()[0].getQualifiedSourceName());
				if (withindex) {
					indexparameter = m.getParameters()[0].getName();
					requestparameter = m.getParameters()[1].getName();
				} else {
					requestparameter = m.getParameters()[0].getName();
				}
				final EventGenerator event = (EventGenerator) BaseGenerator.createGenerator(logger, context,
						eventclass, false);
				event.generate();
				sourcewriter.indent();
				sourcewriter.indentln(newdecl + " {");
				sourcewriter.indent();
				sourcewriter.indentln(eventclass + " event = GWT.create(" + eventclass + ".class);");
				sourcewriter.indentln(responsetype + " response = new " + responsetype
						+ "(DefaultEventBus.get(), event);");
				sourcewriter.indentln(m.getName() + "(" + (withindex ? indexparameter + ", " : "") + requestparameter
						+ ", response);");
				sourcewriter.outdent();
				sourcewriter.indentln("}");
				sourcewriter.outdent();
			}
		}
	}

	private void eventMultiplexers() throws UnableToCompleteException {
		final Collection<JMethod> eventmultiplexermethods = getMethodsWithAnnotation(EventMultiplexer.class);
		for (final JMethod m : eventmultiplexermethods) {
			final String decl = m.getReadableDeclaration();
			final int b = decl.indexOf(Response.class.getCanonicalName());
			final String newdecl = decl.substring(0, b - 2) + ")";
			final String responsemultiplexertype = decl.substring(b, decl.indexOf("> ", b) + 1);
			final String responsetype = responsemultiplexertype.replace("ResponseMultiplexer", "Response");
			final EventMultiplexer eventsender = m.getAnnotation(EventMultiplexer.class);
			final String eventclass = eventsender.value().getCanonicalName();
			final String requestparameter = m.getParameters()[0].getName();
			final EventGenerator event = (EventGenerator) BaseGenerator.createGenerator(logger, context, eventclass,
					false);
			event.generate();

			sourcewriter.indent();
			sourcewriter.indentln(newdecl + " {");
			sourcewriter.indent();
			sourcewriter.indentln(responsemultiplexertype + " responsemultiplexer = new " + responsemultiplexertype
					+ "() {");
			sourcewriter.indent();
			sourcewriter.indentln("@Override");
			sourcewriter.indentln("public " + responsetype + " multiplex() {");
			sourcewriter.indent();
			sourcewriter.indentln(eventclass + " sendevent = GWT.create(" + eventclass + ".class);");
			sourcewriter.indentln("return new " + responsetype + "(DefaultEventBus.get(), sendevent);");
			sourcewriter.outdent();
			sourcewriter.indentln("}");
			sourcewriter.outdent();
			sourcewriter.indentln("};");
			sourcewriter.indentln(m.getName() + "(" + requestparameter + ", responsemultiplexer);");
			sourcewriter.outdent();
			sourcewriter.indentln("}");
			sourcewriter.outdent();
		}
	}

}
