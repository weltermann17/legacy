package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

public class EventGenerator extends BaseGenerator {

	public EventGenerator(final TreeLogger logger, final GeneratorContext context, final JClassType classtype,
			final boolean withlogging) {
		super(logger, context, classtype, withlogging);
	}

	@Override protected void onGenerate() {
		final String handlertype = "BusEventHandler<" + getDataType() + ">";

		sourcewriter.indent();
		sourcewriter.indentln("@Override");
		sourcewriter.indentln("public com.google.gwt.event.shared.GwtEvent.Type<" + handlertype
				+ "> getAssociatedType() {");
		sourcewriter.indent();
		sourcewriter.indentln("return " + generatedShortName() + ".TYPE;");
		sourcewriter.outdent();
		sourcewriter.indentln("}");
		sourcewriter.indentln("public static Type<" + handlertype + "> getType() {");
		sourcewriter.indent();
		sourcewriter.indentln("return " + generatedShortName() + ".TYPE;");
		sourcewriter.outdent();
		sourcewriter.indentln("}");
		sourcewriter.indentln("private final static Type<" + handlertype + "> TYPE = new Type<" + handlertype + ">();");
	}

	public String getDataType() {
		JClassType busevent = null;
		for (final JClassType ctype : classtype.getFlattenedSupertypeHierarchy()) {
			if (ctype.getQualifiedSourceName().equals(BusEvent.class.getCanonicalName())) {
				busevent = ctype;
				break;
			}
		}
		final JParameterizedType parameterizedtype = busevent.isParameterized();
		for (final JClassType ptype : parameterizedtype.getTypeArgs()) {
			return ptype.getQualifiedSourceName();
		}
		return null;
	}

}
