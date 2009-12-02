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

package org.apache.tuscany.sca.implementation.web.client;

import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostExtensionPoint;
import org.apache.tuscany.sca.implementation.web.WebImplementation;
import org.apache.tuscany.sca.implementation.web.runtime.ClientExtensionPoint;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

public class JSClientExtensionPointImpl implements ClientExtensionPoint {

    private ServletHost servletHost;

    public JSClientExtensionPointImpl(ExtensionPointRegistry extensionPoints) {
        ServletHostExtensionPoint servletHosts = extensionPoints.getExtensionPoint(ServletHostExtensionPoint.class);
        List<ServletHost> hosts = servletHosts.getServletHosts();
        if (!hosts.isEmpty()) {
            this.servletHost = hosts.get(0);
        }
    }
    
    public ImplementationProvider createImplementationProvider(RuntimeComponent component, WebImplementation implementation) {
        ClientServlet clientServlet = new ClientServlet();
        servletHost.addServletMapping(ClientServlet.SCRIPT_PATH, clientServlet);
        servletHost.addServletMapping(ClientServlet.SCRIPT_PATH + "/" + component.getName() + "/*", clientServlet);

        if (component.getReferences().size() > 0) {
            for (ComponentReference cr : component.getReferences()) {
                clientServlet.addService(cr, component);
            }
        }

        return new ImplementationProvider() {
            public Invoker createInvoker(RuntimeComponentService arg0, Operation arg1) {
                return null;
            }
            public void start() {
            }
            public void stop() {
            }
            public boolean supportsOneWayInvocation() {
                return false;
            }
        };
    }
}