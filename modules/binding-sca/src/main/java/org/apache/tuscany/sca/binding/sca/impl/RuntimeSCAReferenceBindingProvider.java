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

package org.apache.tuscany.sca.binding.sca.impl;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.binding.sca.DistributedSCABinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.domain.SCADomainEventService;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.node.NodeFactory;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.osoa.sca.ServiceUnavailableException;

/** 
 * The sca reference binding provider mediates between the twin requirements of 
 * local sca bindings and remote sca bindings. In the local case is does 
 * very little. When the sca binding model is set as being remote (because a 
 * reference target can't be resolved in the current model) this binding will 
 * try and create a remote connection to it
 * 
 * @version $Rev$ $Date$
 */
public class RuntimeSCAReferenceBindingProvider implements ReferenceBindingProvider {
    
    private final static Logger logger = Logger.getLogger(RuntimeSCAReferenceBindingProvider.class.getName());

    private NodeFactory nodeFactory;
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private SCABinding binding;
    private boolean started = false;

    private BindingProviderFactory<DistributedSCABinding> distributedProviderFactory = null;
    private ReferenceBindingProvider distributedProvider = null;

    public RuntimeSCAReferenceBindingProvider(ExtensionPointRegistry extensionPoints,
                                              NodeFactory nodeFactory,
                                              RuntimeComponent component,
                                              RuntimeComponentReference reference,
                                              SCABinding binding) {
        this.nodeFactory = nodeFactory;
        this.component = component;
        this.reference = reference;
        this.binding = binding;

        // look to see if a distributed SCA binding implementation has
        // been included on the classpath. This will be needed by the 
        // provider itself to do it's thing
        ProviderFactoryExtensionPoint factoryExtensionPoint =
            extensionPoints.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        distributedProviderFactory =
            (BindingProviderFactory<DistributedSCABinding>)factoryExtensionPoint
                .getProviderFactory(DistributedSCABinding.class);


        // determine if the target is remote. If we can tell now then this will
        // do some initialization before we get to run time
        //isTargetRemote();
    }

    public boolean isTargetRemote() {
        boolean targetIsRemote = false;

        // first look at the target service and see if this has been resolved
        OptimizableBinding optimizableBinding = (OptimizableBinding)binding;
        
        // The descision is based primary on the results of the wiring process in the assembly model
        // however there are two notable situations when this process doesn't give the right answer
        // 1. When a callback is in operation. A callback reference bindings sometimes has to 
        //    act as though there is a local wire and sometimes as if there is a remote wire
        // 2. When a reference is retrieved from the domain. In this case the reference
        //    will not have been part of the original wiring process and will only have 
        //    a target set if the target is local 
        if (optimizableBinding.getTargetComponentService() != null){
            if (optimizableBinding.getTargetComponentService().isUnresolved() == true) {
                targetIsRemote = true;
            } else {
                targetIsRemote = false;
            }
        } else {
            // deal with the case where the wire is completely dynamic (e.g. callbacks) and
            // look at the provided URI to decide whether is a local or remote case
            try {
                URI uri = new URI(binding.getURI());
                 if (uri.isAbsolute()) {
                     targetIsRemote = true;
                 } else {
                     // look in the domain to see if this referenced service is available on this node
                     // or on some other node
                     String serviceNode = null;
                     
                     SCADomainEventService domainProxy = (SCADomainEventService)nodeFactory.getNode().getDomain();
 
                     try {
                         serviceNode =
                             domainProxy.findServiceNode(nodeFactory.getNode().getDomain().getURI(),
                                                         binding.getURI(),
                                                         binding.getClass().getName());
                     } catch (Exception ex) {
                         logger.log(Level.WARNING, 
                                    "Unable to contact domain to find service node. Service: "  +
                                    nodeFactory.getNode().getDomain().getURI() + " " +
                                    nodeFactory.getNode().getURI() + " " +
                                    binding.getURI() + " " +
                                    SCABinding.class.getName());                    
                         
                     }
                     
                     if (serviceNode.equals(domainProxy.SERVICE_NOT_KNOWN)){
                         throw new IllegalStateException("Can't resolve : " + component.getName()
                                                         + " and reference: "
                                                         + reference.getName()
                                                         + " as the service "
                                                         + binding.getURI()
                                                         + " has not been contributed to the domain"); 
                     } else if ((serviceNode.equals(domainProxy.SERVICE_NOT_REGISTERED)) ||
                                (!serviceNode.equals(nodeFactory.getNode().getURI()))){
                         targetIsRemote = true;
                     } else { 
                         targetIsRemote = false;
                     }
                 }
            } catch(Exception ex) {
                targetIsRemote = false;
            }
        }
        
/*
                     // look in the domain for the endpoint. This is the exception rather than the rule but we may
                     // get to this point if this binding belongs to a reference that has been retrieved from the domain
                     // and the reference was given a target that is remote or that didn't exist when the reference was requested
                     String serviceURL = null;
                     
                     SCADomainEventService domainProxy = (SCADomainEventService)nodeFactory.getNode().getDomain();
 
                     try {
                         serviceURL =
                             domainProxy.findServiceEndpoint(nodeFactory.getNode().getDomain().getURI(),
                                                             binding.getURI(),
                                                             binding.getClass().getName());
                     } catch (Exception ex) {
                         logger.log(Level.WARNING, 
                                    "Unable to contact domain to find service. Service: "  +
                                    nodeFactory.getNode().getDomain().getURI() + " " +
                                    nodeFactory.getNode().getURI() + " " +
                                    binding.getURI() + " " +
                                    SCABinding.class.getName());                    
                         
                     }
                     
                     if ((serviceURL == null) || serviceURL.equals("")) {
                         throw new IllegalStateException("Can't resolve : " + component
                                 .getName()
                                 + " and reference: "
                                 + reference.getName()); 
                     } else {
                         targetIsRemote = true;
                         binding.setURI(serviceURL);
                     }  
 
*/

/*        
        else {
            // if no target is found then this could be a completely dynamic
            // reference, e.g. a callback, so check the domain to see if the service is available
            // at this node. The binding uri might be null here if the dynamic reference hasn't been
            // fully configured yet. It won't have all of the information until invocation time
            if ( (nodeFactory != null) && (nodeFactory.getNode() != null) && (binding.getURI() != null)) {
                SCADomainEventService domainProxy = (SCADomainEventService)nodeFactory.getNode().getDomain();

                String serviceUrl = null;
                
                try {
                    serviceUrl =
                        domainProxy.findServiceEndpoint(nodeFactory.getNode().getDomain().getURI(),
                                                             binding.getURI(),
                                                             binding.getClass().getName());
                } catch (Exception ex) {
                    logger.log(Level.WARNING, 
                            "Unable to  find service service: "  +
                            nodeFactory.getNode().getDomain().getURI() + " " +
                            nodeFactory.getNode().getURI() + " " +
                            binding.getURI() + " " +
                            SCABinding.class.getName());                    
                    
                }
                
                if ((serviceUrl == null) || serviceUrl.equals("")) {
                    targetIsRemote = false;
                } else {
                    targetIsRemote = true;
                }

            }
        }

        // if we think the target is remote check that everything is configured correctly
        if (targetIsRemote) {
            // initialize the remote provider if it hasn't been done already
            if (distributedProvider == null) {
                if (!reference.getInterfaceContract().getInterface().isRemotable()) {
                    throw new IllegalStateException("Reference interface not remoteable for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                if (distributedProviderFactory == null) {
                    throw new IllegalStateException("No distributed SCA binding available for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                if (nodeFactory.getNode() == null) {
                    throw new IllegalStateException("No distributed domain available for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                // create the remote provider
                DistributedSCABinding distributedBinding = new DistributedSCABindingImpl();
                distributedBinding.setSCABinging(binding);

                distributedProvider = distributedProviderFactory
                        .createReferenceBindingProvider(component, reference, distributedBinding);
            }
        }
*/
        return targetIsRemote;
    }
    
    private ReferenceBindingProvider getDistributedProvider(){

        // initialize the remote provider if it hasn't been done already
        if (distributedProvider == null) {
            if (!reference.getInterfaceContract().getInterface().isRemotable()) {
                throw new IllegalStateException("Reference interface not remoteable for component: " + component
                    .getName()
                    + " and reference: "
                    + reference.getName());
            }

            if (distributedProviderFactory == null) {
                throw new IllegalStateException("No distributed SCA binding available for component: " + component
                    .getName()
                    + " and reference: "
                    + reference.getName());
            }

            if (nodeFactory.getNode() == null) {
                throw new IllegalStateException("No distributed domain available for component: " + component
                    .getName()
                    + " and reference: "
                    + reference.getName());
            }

            // create the remote provider
            DistributedSCABinding distributedBinding = new DistributedSCABindingImpl();
            distributedBinding.setSCABinging(binding);

            distributedProvider = distributedProviderFactory
                    .createReferenceBindingProvider(component, reference, distributedBinding);
        }
        
        return distributedProvider;
        
    }

    public InterfaceContract getBindingInterfaceContract() {
        if (isTargetRemote()) {
            return getDistributedProvider().getBindingInterfaceContract();
        } else {
            return reference.getInterfaceContract();
        }
    }

    public boolean supportsOneWayInvocation() {
        if (isTargetRemote()) {
            return getDistributedProvider().supportsOneWayInvocation();
        } else {
            return false;
        }
    }

    /**
     * @param wire
     */
    private Invoker getInvoker(RuntimeWire wire, Operation operation) {
        EndpointReference target = wire.getTarget();
        if (target != null) {
            RuntimeComponentService service = (RuntimeComponentService)target.getContract();
            if (service != null) { // not a callback wire
                SCABinding scaBinding = service.getBinding(SCABinding.class);
                return service.getInvoker(scaBinding, wire.getSource().getInterfaceContract(), operation);
            }
        }
        return null;
    }

    public Invoker createInvoker(Operation operation) {
        if (isTargetRemote()) {
            return getDistributedProvider().createInvoker(operation);
        } else {
            RuntimeWire wire = reference.getRuntimeWire(binding);
            Invoker invoker = getInvoker(wire, operation);
            if (invoker == null) {
                throw new ServiceUnavailableException("Service not found for component "
                    + component.getName()
                    + " reference " 
                    + reference.getName()
                    + " (bindingURI="
                    + binding.getURI()
                    + " operation="
                    + operation.getName()
                    + "). Ensure that the composite containing the service is loaded and "
                    + "started somewhere in the SCA domain and that if running in a "
                    + "remote node that the interface of the target service marked as @Remotable");
            }
            return invoker;
        }
    }

    public void start() {
        if (started) {
            return;
        } else {
            started = true;
        }

        if (distributedProvider != null) {
            distributedProvider.start();
        }
    }

    public void stop() {
        if (distributedProvider != null) {
            distributedProvider.stop();
        }
    }

}
