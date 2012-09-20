package com.ibm.de.ebs.plm.gwt.rebind.util;

import java.io.PrintWriter;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

public class LoggingSourceWriter implements SourceWriter {

	public LoggingSourceWriter(final String fullname, final PrintWriter printwriter, final SourceWriter sourcewriter) {
		this.printwriter = printwriter;
		this.sourcewriter = sourcewriter;
		this.printwriter.println(fullname);
		this.printwriter.flush();
	}

	@Override public void beginJavaDocComment() {
		printwriter.print("/* ");
		sourcewriter.beginJavaDocComment();
	}

	@Override public void commit(final TreeLogger logger) {
		printwriter.flush();
		sourcewriter.commit(logger);
	}

	@Override public void endJavaDocComment() {
		printwriter.print(" */");
		sourcewriter.endJavaDocComment();
	}

	@Override public void indent() {
		indent += 2;
	}

	@Override public void indentln(final String s) {
		printIndent();
		if (!"".equals(s)) {
			println(s);
		}
	}

	@Override public void indentln(final String s, final Object... args) {
		printIndent();
		println(s, args);
	}

	@Override public void outdent() {
		if (0 < indent) {
			indent -= 2;
		}
	}

	@Override public void print(final String s) {
		printwriter.print(s);
		sourcewriter.print(s);
	}

	@Override public void print(final String s, final Object... args) {
		printwriter.print(s);
		for (final Object o : args) {
			printwriter.print(o);
		}
		sourcewriter.print(s, args);
	}

	@Override public void println() {
		printwriter.println();
		sourcewriter.println();
	}

	@Override public void println(final String s) {
		print(s);
		println();
	}

	@Override public void println(final String s, final Object... args) {
		print(s, args);
		println();
	}

	private void printIndent() {
		printwriter.print(indentbuffer.substring(0, indent));
	}

	private final String indentbuffer = "                                                                      ";
	private final PrintWriter printwriter;
	private final SourceWriter sourcewriter;
	private int indent = 0;
}
