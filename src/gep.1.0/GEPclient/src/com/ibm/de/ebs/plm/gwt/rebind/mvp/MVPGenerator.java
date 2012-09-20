package com.ibm.de.ebs.plm.gwt.rebind.mvp;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class MVPGenerator extends Generator {

	@Override public String generate(final TreeLogger logger, final GeneratorContext context, final String typename)
			throws UnableToCompleteException {
		return BaseGenerator.createGenerator(logger, context, typename, true).generate();
	}

}
