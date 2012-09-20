package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import java.util.Collection;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.ibm.de.ebs.plm.gwt.client.mvp.Gesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestCondition;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestGesture;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestSender;
import com.ibm.de.ebs.plm.gwt.rebind.util.Method;

public class ViewGenerator extends BaseGenerator {

	public ViewGenerator(final TreeLogger logger, final GeneratorContext context, final JClassType classtype,
			final boolean withlogging) throws UnableToCompleteException {
		super(logger, context, classtype, withlogging);
		final String presentertypename = classtype.getQualifiedSourceName().replace("View", "Presenter");
		presenter = (PresenterGenerator) BaseGenerator.createGenerator(logger, context, presentertypename, false);
		final String modeltypename = presenter.findMethodReturnType("model").getQualifiedSourceName();
		model = (ModelGenerator) BaseGenerator.createGenerator(logger, context, modeltypename, false);
	}

	@Override protected void onGenerate() throws UnableToCompleteException {
		requestSender();
		gestureMethod();
		eventReceivers("onInitBusEventHandlers");
	}

	private void generateRequestSender(final RequestSender requestsender, final JMethod m, final Method method)
			throws UnableToCompleteException {
		final String request = requestsender.value();
		final Collection<String> modelmethods = model.findRequestReceiverMethod(request);
		if (0 == modelmethods.size()) {
			System.out.println(">>> No method found in " + model.shortName() + " with annotation @RequestReceiver(\""
					+ request + "\").");
			throw new UnableToCompleteException();
		}
		final String viewmethod = m.getName();
		JType returntype = findMethodReturnType(viewmethod);
		String returntypename = returntype.getQualifiedSourceName();
		final boolean isarray = null != returntype.isArray();
		if (isarray) {
			returntype = returntype.isArray().getComponentType();
			returntypename = returntype.getQualifiedSourceName();
		}
		JMethod addmethod = null;
		for (final JMethod mm : returntype.isClassOrInterface().getMethods()) {
			if (mm.getName().startsWith("add")) {
				addmethod = mm;
				break;
			}
		}
		final JType handlertype = addmethod.getParameters()[0].getType();
		final String handlertypename = handlertype.getQualifiedSourceName();
		JMethod onmethod = null;
		for (final JMethod mm : handlertype.isClassOrInterface().getMethods()) {
			if (mm.getName().startsWith("on")) {
				onmethod = mm;
				break;
			}
		}
		final JType eventtype = onmethod.getParameters()[0].getType();
		final String eventtypename = eventtype.getQualifiedSourceName();

		final boolean hascondition = m.isAnnotationPresent(RequestCondition.class);
		String conditionmethod = null;
		String conditionvalue = null;
		if (hascondition) {
			final RequestCondition requestcondition = m.getAnnotation(RequestCondition.class);
			conditionmethod = requestcondition.method();
			conditionvalue = requestcondition.value();
		}
		if (isarray) {
			method.bodyLine("class C" + viewmethod + "Handler implements " + handlertypename + " {");
			method.indent();
			method.bodyLine("private final int i;");
			method.bodyLine("public C" + viewmethod + "Handler(final int i) {");
			method.indent();
			method.bodyLine("this.i = i;");
			method.outdent();
			method.bodyLine("}");
			method.bodyLine("@Override");
			method.bodyLine("public void " + onmethod.getName() + "(" + eventtypename + " event) {");
			method.indent();
			if (hascondition) {
				method.bodyLine("if (true) {");
				method.indent();
			}
			method.bodyLine("Request<" + eventtypename + "> request = new Request<" + eventtypename + ">(\"" + request
					+ "\", event);");
			method.bodyLine(model.generatedShortName() + " modelimpl = (" + model.generatedShortName()
					+ ") getPresenter().model();");
			for (final String modelmethod : modelmethods) {
				method.bodyLine("modelimpl." + modelmethod + "(i, request);");
			}
			if (hascondition) {
				method.endBlock();
			}
			method.endBlock();
			method.endBlock();
			method.bodyLine("int " + viewmethod + "_i = 0;");
			method.bodyLine("for (" + returntypename + " next: " + viewmethod + "()) {");
			method.indent();
			method.bodyLine("next." + addmethod.getName() + "(new C" + viewmethod + "Handler(" + viewmethod + "_i++));");
			method.endBlock();
		} else {
			method.bodyLine(viewmethod + "()." + addmethod.getName() + "(new " + handlertypename + "() {");
			method.indent();
			method.bodyLine("@Override");
			method.bodyLine("public void " + onmethod.getName() + "(" + eventtypename + " event) {");
			method.indent();
			if (hascondition) {
				method.bodyLine("if (\"" + conditionvalue + "\".equals(event." + conditionmethod + "())) {");
				method.indent();
			}
			method.bodyLine("Request<" + eventtypename + "> request = new Request<" + eventtypename + ">(\"" + request
					+ "\", event);");
			method.bodyLine(model.generatedShortName() + " modelimpl = (" + model.generatedShortName()
					+ ") getPresenter().model();");
			for (final String modelmethod : modelmethods) {
				method.bodyLine("modelimpl." + modelmethod + "(request);");
			}
			if (hascondition) {
				method.endBlock();
			}
			method.endBlock();
			method.outdent();
			method.bodyLine("});");
		}
	}

	private void requestSender() throws UnableToCompleteException {
		final JMethod outer = findMethod("viewBindings");
		final Method method = new Method(outer, sourcewriter);
		if (!outer.isAbstract()) {
			method.bodyLine("super." + outer.getName() + "();");
		}
		final Collection<JMethod> requestsendermethods = getMethodsWithAnnotation(RequestSender.class);
		for (final JMethod m : requestsendermethods) {
			if ("onBind".equals(m.getName())) {
				System.out
						.println(">>> Method with annotation @RequestSender must not override \"protected void onBind()\"");
				throw new UnableToCompleteException();
			}
			final RequestSender requestsender = m.getAnnotation(RequestSender.class);
			generateRequestSender(requestsender, m, method);
		}
		method.commit();
		method.outdent();
	}

	private void generateGestureMethod(final RequestGesture requestsendermethod, final JMethod m)
			throws UnableToCompleteException {
		final String request = requestsendermethod.request();
		final Class<? extends Gesture> gestureclass = requestsendermethod.gesture();
		final Method method = new Method(m, sourcewriter);
		String viewmethod = null;
		for (final JMethod v : getMethodsWithAnnotation(RequestSender.class)) {
			final RequestSender requestsender = v.getAnnotation(RequestSender.class);
			if (requestsender.value().equals(request)) {
				viewmethod = v.getName();
				break;
			}
		}
		if (null == viewmethod) {
			System.out.println(">>> No method found with annotation @RequestSender(\"" + request + "\")");
			throw new UnableToCompleteException();
		}
		final JType viewmethodreturntype = findMethodReturnType(viewmethod);
		final boolean isarray = null != viewmethodreturntype.isArray();
		final String parametername = 0 < m.getParameters().length ? m.getParameters()[0].getName() : null;
		final String gesturetype = gestureclass.getCanonicalName();

		String index = "";
		if (isarray) {
			index = "[i]";
			method.bodyLine("for (int i = 0; i < java.lang.Math.min(" + viewmethod + "().length, " + parametername
					+ ".length); ++i) {");
			method.indent();
		}
		method.bodyLine(gesturetype + " gesture = new " + gesturetype + "();");
		method.bodyLine("gesture.setHasHandlers(" + viewmethod + "()" + index + ");");
		if (1 == m.getParameters().length) {
			method.bodyLine("gesture.setValue(" + parametername + index + ");");
		}
		method.bodyLine(viewmethod + "()" + index + ".fireEvent(gesture);");
		if (isarray) {
			method.endBlock();
		}
		method.commit();
		method.outdent();
	}

	// private void modelViewPresenters() throws UnableToCompleteException {
	// final Collection<JMethod> mvpmethods =
	// getMethodsWithAnnotation(ModelViewPresenter.class);
	// for (final JMethod m : mvpmethods) {
	// final ModelViewPresenter mvp = m.getAnnotation(ModelViewPresenter.class);
	// final Class<? extends Model> mvpclass = mvp.value();
	// final Method method = new Method(m, sourcewriter);
	// final String base = mvpclass.getCanonicalName() + ".class";
	// method.indent();
	// method.bodyLine("final BaseModel model = GWT.create(" + base + ");");
	// method.bodyLine("final BaseView view = GWT.create(" +
	// base.replace("Model", "View") + ");");
	// method.bodyLine("final BasePresenter<BaseModel, BaseView> presenter = GWT.create("
	// + base.replace("Model", "Presenter") + ");");
	// if (2 == m.getParameters().length) {
	// final String eventparam = m.getParameters()[1].getName();
	// method.bodyLine("return super." + m.getName() +
	// "(new MVP(model, view, presenter, " + eventparam
	// + "), " + eventparam + ");");
	// } else {
	// method.bodyLine("return super." + m.getName() +
	// "(new MVP(model, view, presenter));");
	// }
	// method.commit();
	// method.outdent();
	// }
	// }

	private void gestureMethod() throws UnableToCompleteException {
		final Collection<JMethod> requestsendermethods = getMethodsWithAnnotation(RequestGesture.class);
		for (final JMethod m : requestsendermethods) {
			final RequestGesture requestsendermethod = m.getAnnotation(RequestGesture.class);
			generateGestureMethod(requestsendermethod, m);
		}
	}

	private final PresenterGenerator presenter;
	private final ModelGenerator model;

}
