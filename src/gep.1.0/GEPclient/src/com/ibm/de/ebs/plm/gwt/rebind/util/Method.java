package com.ibm.de.ebs.plm.gwt.rebind.util;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.user.rebind.SourceWriter;

public class Method {

	public Method(final JMethod m, final SourceWriter sourcewriter) {
		w = sourcewriter;
		w.indent();
		if (!m.isFinal()) {
			w.indentln("@Override");
		}
		w.indentln("");

		if (m.isPrivate()) {
			w.print("private ");
		}
		if (m.isProtected()) {
			w.print("protected ");
		}
		if (m.isPublic()) {
			w.print("public ");
		}
		w.print(m.getReturnType().getQualifiedSourceName() + " ");
		w.print(m.getName() + "(");

		for (final JParameter p : m.getParameters()) {
			if (0 < parametercount++) {
				w.print(", ");
			}
			w.print("final ");
			w.print(p.getType().getQualifiedSourceName() + " ");
			w.print(p.getName());
		}
		w.println(") {");
		w.indent();
	}

	public void bodyLine(final String line) {
		w.indentln(line);
	}

	public void commit() {
		w.outdent();
		w.indentln("}");
	}

	public void endBlock() {
		commit();
	}

	public void indent() {
		w.indent();
	}

	public void outdent() {
		w.outdent();
	}

	private int parametercount = 0;

	private final SourceWriter w;

}
