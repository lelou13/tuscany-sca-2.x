/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.binding.comet.runtime;

import org.apache.tuscany.sca.host.http.ServletHost;
import org.atmosphere.cpr.AtmosphereServlet;

/**
 * This class is used to create two servlets: one exposing all the comet
 * services, the other one exposing the javascript toolkit. Exposing all comet
 * services through a single servlet is needed as the browsers are undergone by
 * the two http connection limit so all comet services should send their
 * responses via the same http connection to the same client. Dispatching to the
 * corresponding endpoint and operation is done internally using Jersey RESTful
 * Web Services integration with the AtmosphereServlet. The Javascript toolkit
 * servlet is unique as it is not tied to any of the services - it offers a
 * global API.
 */
public final class ServletFactory {

	/**
	 * Init-param key for the AtmosphereServlet defining where to look for
	 * Jersey classes.
	 */
	private static final String PACKAGE_KEY = "com.sun.jersey.config.property.packages";

	/**
	 * Package of the class handling dispatching to endpoints.
	 */
	private static final String PACKAGE_VALUE = "org.apache.tuscany.sca.binding.comet.runtime.handler";

	/**
	 * Package of the class handling Javascript toolkit retrieval.
	 */
	private static final String JS_PACKAGE_VALUE = "org.apache.tuscany.sca.binding.comet.runtime.javascript";

	/**
	 * Key in the ServletContext where the comet component context is stored.
	 */
	public static final String COMET_COMPONENT_CONTEXT_KEY = "org.apache.tuscany.sca.binding.comet.operations";

	/**
	 * Path where services will be exposed.
	 */
	public static final String PATH = "/tuscany-comet/*";

	/**
	 * Path where Javascript toolkit will be exposed.
	 */
	public static final String JS_PATH = "/tuscany-comet-js/*";

	/**
	 * The servlet that is exposing the comet services.
	 */
	private static AtmosphereServlet cometServlet = null;

	/**
	 * The servlet that is exposing the Javascript toolkit.
	 */
	private static AtmosphereServlet javascriptServlet = null;

	/**
	 * Private constructor for the singleton class.
	 */
	private ServletFactory() {
	}

	public static synchronized String registerServlet(final ServletHost servletHost) {
		String uri = registerCometServlet(servletHost);
		registerJavascriptServlet(servletHost);
		return uri;
	}

	private static String registerCometServlet(ServletHost servletHost) {
		if (ServletFactory.cometServlet == null) {
			ServletFactory.cometServlet = new AtmosphereServlet();
			ServletFactory.cometServlet.addInitParameter(ServletFactory.PACKAGE_KEY, ServletFactory.PACKAGE_VALUE);
			String uri = servletHost.addServletMapping(ServletFactory.PATH, ServletFactory.cometServlet);
			return uri;
		}
		return null;
	}

	private static void registerJavascriptServlet(ServletHost servletHost) {
		if (ServletFactory.javascriptServlet == null) {
			ServletFactory.javascriptServlet = new AtmosphereServlet();
			ServletFactory.javascriptServlet.addInitParameter(ServletFactory.PACKAGE_KEY,
					ServletFactory.JS_PACKAGE_VALUE);
			servletHost.addServletMapping(ServletFactory.JS_PATH, ServletFactory.javascriptServlet);
		}
	}

	public static void unregisterServlet(final ServletHost servletHost) {
		servletHost.removeServletMapping(ServletFactory.PATH);
		servletHost.removeServletMapping(ServletFactory.JS_PATH);
	}

}
