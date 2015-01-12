package com.sap.dirigible.runtime.java.executors;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.runtime.java.JavaExecutor;

public class JavaExecutorStub extends JavaExecutor {

	public JavaExecutorStub(IRepository repository, File libDirectory,
			String classpath, String... rootPaths) {
		super(repository, libDirectory, classpath, rootPaths);
	}

	@Override
	protected void registerDefaultVariables(HttpServletRequest request,
			HttpServletResponse response, Object input,
			Map<Object, Object> executionContext, IRepository repository,
			Object scope) {
		// Do nothing
	}

}