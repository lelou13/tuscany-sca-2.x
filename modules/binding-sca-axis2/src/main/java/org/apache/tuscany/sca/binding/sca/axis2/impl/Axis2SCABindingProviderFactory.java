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

package org.apache.tuscany.sca.binding.sca.axis2.impl;

import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.binding.sca.DistributedSCABinding;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostExtensionPoint;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.node.spi.NodeFactory;
import org.apache.tuscany.sca.policy.util.PolicyHandlerDefinitionsLoader;
import org.apache.tuscany.sca.policy.util.PolicyHandlerTuple;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * The factory for the Axis2 based implementation of the distributed sca binding
 * 
 * @version $Rev: 563772 $ $Date: 2007-08-08 07:50:49 +0100 (Wed, 08 Aug 2007) $
 */
public class Axis2SCABindingProviderFactory implements BindingProviderFactory<DistributedSCABinding> {
    
    private ModelFactoryExtensionPoint modelFactories;
    private ServletHost servletHost;
    private Map<ClassLoader, List<PolicyHandlerTuple>> policyHandlerClassnames = null;
    private DataBindingExtensionPoint dataBindings;

    public Axis2SCABindingProviderFactory(ExtensionPointRegistry extensionPoints) {
        ServletHostExtensionPoint servletHosts = extensionPoints.getExtensionPoint(ServletHostExtensionPoint.class);
        servletHost = servletHosts.getServletHosts().get(0);
        modelFactories = extensionPoints.getExtensionPoint(ModelFactoryExtensionPoint.class);
        policyHandlerClassnames = PolicyHandlerDefinitionsLoader.loadPolicyHandlerClassnames();
        dataBindings = extensionPoints.getExtensionPoint(DataBindingExtensionPoint.class);
    }    

    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeComponent component,
                                                                   RuntimeComponentReference reference,
                                                                   DistributedSCABinding binding) {
        return new Axis2SCAReferenceBindingProvider(component, reference, binding,
                                                    servletHost, modelFactories,
                                                    policyHandlerClassnames, dataBindings);
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeComponent component,
                                                               RuntimeComponentService service,
                                                               DistributedSCABinding binding) {
        return new Axis2SCAServiceBindingProvider(component, service, binding,
                                                  servletHost, modelFactories,
                                                  policyHandlerClassnames, dataBindings);
    }

    public Class<DistributedSCABinding> getModelType() {
        return DistributedSCABinding.class;
    }  
}
