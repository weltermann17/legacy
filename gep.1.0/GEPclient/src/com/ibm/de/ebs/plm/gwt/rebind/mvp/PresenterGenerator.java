package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import java.util.Collection;
import java.util.Stack;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.rebind.util.Method;

public class PresenterGenerator extends BaseGenerator {

	public PresenterGenerator(final TreeLogger logger, final GeneratorContext context, final JClassType classtype,
			final boolean withlogging) throws UnableToCompleteException {
		super(logger, context, classtype, withlogging);
		final String modeltypename = findMethodReturnType("model").getQualifiedSourceName();
		model = (ModelGenerator) BaseGenerator.createGenerator(logger, context, modeltypename, false);
	}

	@Override protected void onGenerate() throws UnableToCompleteException {
		requestSender();
		eventReceivers("onBindBusEventHandlers");
		final JMethod outer = findMethod("onBind");
		final Method outermethod = new Method(outer, sourcewriter);
		if (!outer.isAbstract()) {
			outermethod.bodyLine("super." + outer.getName() + "();");
		}
		while (!methods.isEmpty()) {
			final JMethod m = methods.pop();
			if (!("initializeModel".equals(m.getName()) || "onBindBusEventHandlers".equals(m.getName()))) {
				outermethod.bodyLine(m.getName() + "();");
			}
		}
		outermethod.commit();
	}

	private void generateRequestSender(final RequestSender requestsender, final Method method)
			throws UnableToCompleteException {
		final String request = requestsender.value();
		final Collection<String> modelmethods = model.findRequestReceiverMethod(request);
		if (0 == modelmethods.size()) {
			System.out.println(">>> No method found in " + model.shortName() + " with annotation @RequestReceiver(\""
					+ request + "\").");
			throw new UnableToCompleteException();
		}
		method.bodyLine(model.generatedShortName() + " modelimpl = (" + model.generatedShortName() + ") model();");
		method.bodyLine("Request<?> request = new Request<com.google.gwt.event.shared.GwtEvent<?>>(\"" + request
				+ "\");");
		method.indent();
		for (final String modelmethod : modelmethods) {
			method.bodyLine("modelimpl." + modelmethod + "(request);");
		}
		method.outdent();
	}

	private void requestSender() throws UnableToCompleteException {
		final Collection<JMethod> requestsendermethods = getMethodsWithAnnotation(RequestSender.class);
		for (final JMethod m : requestsendermethods) {
			if ("onBind".equals(m.getName())) {
				System.out
						.println(">>> Method with annotation @RequestSender must not override \"protected void onBind()\"");
				throw new UnableToCompleteException();
			}
			final Method innermethod = new Method(m, sourcewriter);
			if (!m.isAbstract()) {
				innermethod.bodyLine("super." + m.getName() + "();");
			}
			final RequestSender requestsender = m.getAnnotation(RequestSender.class);
			generateRequestSender(requestsender, innermethod);
			innermethod.commit();
			innermethod.outdent();
			methods.add(m);
		}
	}

	private final Stack<JMethod> methods = new Stack<JMethod>();
	private final ModelGenerator model;

}
