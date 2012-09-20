package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.rebind.util.LoggingSourceWriter;
import com.ibm.de.ebs.plm.gwt.rebind.util.Method;

public abstract class BaseGenerator {

	public BaseGenerator(final TreeLogger logger, final GeneratorContext context, final JClassType classtype,
			final boolean withlogging) {
		this.logger = logger;
		this.context = context;
		this.classtype = classtype;
		this.withlogging = withlogging;
	}

	public String generate() throws UnableToCompleteException {
		final StringWriter stringwriter = new StringWriter();
		try {
			final PrintWriter printwriter = context.tryCreate(logger, packageName(), generatedShortName());
			alreadygenerated = null == printwriter;
			if (!alreadygenerated) {
				final PrintWriter console = new PrintWriter(stringwriter);
				composer = !alreadygenerated ? new ClassSourceFileComposerFactory(packageName(), generatedShortName())
						: null;
				composer.addImport("com.google.gwt.core.client.GWT");
				composer.addImport("com.ibm.de.ebs.plm.gwt.client.mvp.*");
				composer.setSuperclass(fullName());
				sourcewriter = !alreadygenerated ? (withlogging ? new LoggingSourceWriter(generatedFullName(), console,
						composer.createSourceWriter(context, printwriter)) : composer.createSourceWriter(context,
						printwriter)) : null;
				onGenerate();
				sourcewriter.commit(logger);
				if (Logger.getAnonymousLogger().isLoggable(Level.FINEST)) {
					Logger.getAnonymousLogger().finest(stringwriter.toString());
				}
			}
			return generatedFullName();
		} catch (final Exception e) {
			e.printStackTrace();
			Logger.getAnonymousLogger().severe(stringwriter.toString());
			throw new UnableToCompleteException();
		}
	}

	abstract protected void onGenerate() throws UnableToCompleteException;

	public boolean alreadyGenerated() {
		return alreadygenerated;
	}

	static public String suffix() {
		return "Impl";
	}

	public String shortName() {
		return classtype.getSimpleSourceName();
	}

	public String fullName() {
		return classtype.getQualifiedSourceName();
	}

	public String generatedShortName() {
		return shortName() + BaseGenerator.suffix();
	}

	public String generatedFullName() {
		return fullName() + BaseGenerator.suffix();
	}

	public String packageName() {
		return classtype.getPackage().getName();
	}

	public Collection<JMethod> getMethodsWithAnnotation(final Class<? extends Annotation> annotationclass) {
		final Set<JMethod> methods = new HashSet<JMethod>();
		for (final JClassType ctype : classtype.getFlattenedSupertypeHierarchy()) {
			for (final JMethod method : ctype.getOverridableMethods()) {
				if (method.isAnnotationPresent(annotationclass)) {
					methods.add(method);
				}
			}
		}
		return methods;
	}

	public JType findMethodReturnType(final String methodname) {
		for (final JMethod method : classtype.getOverridableMethods()) {
			if (methodname.equals(method.getName())) {
				return method.getReturnType();
			}
		}
		return null;
	}

	public JClassType getClassType() {
		return classtype;
	}

	public JMethod findMethod(final String methodname) {
		for (final JMethod method : classtype.getOverridableMethods()) {
			if (methodname.equals(method.getName())) {
				return method;
			}
		}
		return null;
	}

	public boolean hasSuperType(final Class<?> supertype) {
		for (final JClassType ctype : classtype.getFlattenedSupertypeHierarchy()) {
			if (ctype.getQualifiedSourceName().equals(supertype.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasSuperType(final JClassType classtype, final Class<?> supertype) {
		for (final JClassType ctype : classtype.getFlattenedSupertypeHierarchy()) {
			if (ctype.getQualifiedSourceName().equals(supertype.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}

	public static BaseGenerator createGenerator(final TreeLogger logger, final GeneratorContext context,
			final String typename, final boolean withlogging) throws UnableToCompleteException {
		JClassType classtype;
		try {
			classtype = context.getTypeOracle().getType(typename);
		} catch (final NotFoundException e) {
			e.printStackTrace();
			throw new UnableToCompleteException();
		}
		if (BaseGenerator.hasSuperType(classtype, com.ibm.de.ebs.plm.gwt.client.mvp.Model.class)) {
			return new ModelGenerator(logger, context, classtype, withlogging);
		} else if (BaseGenerator.hasSuperType(classtype, com.ibm.de.ebs.plm.gwt.client.mvp.View.class)) {
			return new ViewGenerator(logger, context, classtype, withlogging);
		} else if (BaseGenerator.hasSuperType(classtype, com.ibm.de.ebs.plm.gwt.client.mvp.Presenter.class)) {
			return new PresenterGenerator(logger, context, classtype, withlogging);
		} else if (BaseGenerator.hasSuperType(classtype, com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent.class)) {
			return new EventGenerator(logger, context, classtype, withlogging);
		} else {
			return new BaseGenerator(logger, context, classtype, withlogging) {
				@Override protected void onGenerate() throws UnableToCompleteException {
				}
			};
		}
	}

	protected void eventReceivers(final String outermethod) throws UnableToCompleteException {
		final JMethod outer = findMethod(outermethod);
		final Method method = new Method(outer, sourcewriter);
		final Collection<JMethod> eventreceivermethods = getMethodsWithAnnotation(EventReceiver.class);
		for (final JMethod m : eventreceivermethods) {
			final String rcveventclass = m.getParameters()[0].getType().getQualifiedSourceName();
			final EventGenerator recevent = (EventGenerator) BaseGenerator.createGenerator(logger, context,
					rcveventclass, false);
			recevent.generate();
			final String receventtype = recevent.fullName();
			final String receventdatatype = recevent.getDataType();
			final EventSender eventsender = m.getAnnotation(EventSender.class);
			String sendeventclass = null;
			String responsetype = null;
			if (null != eventsender) {
				final String decl = m.getReadableDeclaration();
				final int b = decl.indexOf(Response.class.getCanonicalName());
				responsetype = decl.substring(b, decl.indexOf("> ", b) + 1);
				sendeventclass = eventsender.value().getCanonicalName();
			}
			method.bodyLine("getPresenter().addBusEventHandler(" + recevent.generatedFullName()
					+ ".getType(), new BusEventHandler<" + receventdatatype + ">() {");
			method.indent();
			method.bodyLine("@Override");
			method.bodyLine("public void onBusEvent(BusEvent<" + receventdatatype + "> busevent) {");
			method.indent();
			method.bodyLine(receventtype + " event = (" + receventtype + ") busevent;");
			if (null == eventsender) {
				method.bodyLine(generatedShortName() + ".this." + m.getName() + "(event);");
			} else {
				sourcewriter.indentln(sendeventclass + " sendevent = GWT.create(" + sendeventclass + ".class);");
				sourcewriter.indentln(responsetype + " response = new " + responsetype
						+ "(DefaultEventBus.get(), sendevent);");
				method.bodyLine(generatedShortName() + ".this." + m.getName() + "(event, response);");
			}
			method.endBlock();
			method.outdent();
			method.bodyLine("});");
		}
		method.commit();
		method.outdent();
	}

	protected final JClassType classtype;
	protected final TreeLogger logger;
	protected final GeneratorContext context;
	protected SourceWriter sourcewriter;
	protected ClassSourceFileComposerFactory composer;
	private final boolean withlogging;
	private boolean alreadygenerated = false;

}
