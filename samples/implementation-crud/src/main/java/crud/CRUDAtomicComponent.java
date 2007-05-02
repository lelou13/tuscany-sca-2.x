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
package crud;

import java.net.URI;
import java.util.List;

import org.apache.tuscany.assembly.Component;
import org.apache.tuscany.assembly.ComponentReference;
import org.apache.tuscany.core.RuntimeComponentReference;
import org.apache.tuscany.core.RuntimeWire;
import org.apache.tuscany.core.component.ComponentContextImpl;
import org.apache.tuscany.core.component.ComponentContextProvider;
import org.apache.tuscany.core.component.ServiceReferenceImpl;
import org.apache.tuscany.core.component.scope.InstanceWrapperBase;
import org.apache.tuscany.core.invocation.JDKProxyService;
import org.apache.tuscany.core.invocation.WireObjectFactory;
import org.apache.tuscany.interfacedef.Operation;
import org.apache.tuscany.interfacedef.impl.DefaultInterfaceContractMapper;
import org.apache.tuscany.spi.ObjectCreationException;
import org.apache.tuscany.spi.SingletonObjectFactory;
import org.apache.tuscany.spi.component.InstanceWrapper;
import org.apache.tuscany.spi.component.TargetInvokerCreationException;
import org.apache.tuscany.spi.component.TargetResolutionException;
import org.apache.tuscany.spi.extension.AtomicComponentExtension;
import org.apache.tuscany.spi.wire.TargetInvoker;
import org.apache.tuscany.spi.wire.Wire;
import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;

/**
 * The runtime instantiation of CRUD component implementations. FIXME We need to
 * remove the requirement for such a class, implementing the implementation
 * model should be sufficient.
 * 
 * @version $Rev$ $Date$
 */
public class CRUDAtomicComponent extends AtomicComponentExtension implements ComponentContextProvider {
    private ComponentContext componentContext;
    private ResourceManager resourceManager;
    private Component component;
    private CRUDImplementation implementation;

    public CRUDAtomicComponent(URI uri, URI groupId, Component component, CRUDImplementation implementation) {
        super(uri, null, null, groupId, 50);
        this.component = component;
        this.implementation = implementation;
        componentContext = new ComponentContextImpl(this);
        resourceManager = new ResourceManager(implementation.getDirectory());
    }

    public Object createInstance() throws ObjectCreationException {
        return resourceManager;
    }

    public InstanceWrapper createInstanceWrapper() throws ObjectCreationException {
        return new InstanceWrapperBase(createInstance());
    }

    public Object getTargetInstance() throws TargetResolutionException {
        return resourceManager;
    }

    public void attachCallbackWire(Wire arg0) {
    }

    public void attachWire(Wire arg0) {
    }

    public void attachWires(List<Wire> arg0) {
    }

    public List<Wire> getWires(String arg0) {
        return null;
    }

    public TargetInvoker createTargetInvoker(String targetName, final Operation operation, boolean callback)
        throws TargetInvokerCreationException {
        return new CRUDTargetInvoker(operation, resourceManager);
    }

    @Override
    public ComponentContext getComponentContext() {
        return componentContext;
    }

    public <B, R extends CallableReference<B>> R cast(B target) {
        return null;
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        return null;
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        List<ComponentReference> refs = component.getReferences();
        for (ComponentReference ref : refs) {
            if (ref.getName().equals(referenceName)) {
                RuntimeComponentReference attachPoint = (RuntimeComponentReference)ref;
                RuntimeWire wire = attachPoint.getRuntimeWires().get(0);
                return new JDKProxyService(null, new DefaultInterfaceContractMapper()).createProxy(businessInterface,
                                                                                                   wire);
            }
        }
        return null;
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        List<ComponentReference> refs = component.getReferences();
        for (ComponentReference ref : refs) {
            if (ref.getName().equals(referenceName) || referenceName.equals("$self$_")
                && ref.getName().startsWith(referenceName)) {
                RuntimeComponentReference attachPoint = (RuntimeComponentReference)ref;
                RuntimeWire wire = attachPoint.getRuntimeWires().get(0);
                JDKProxyService proxyService = new JDKProxyService(null, new DefaultInterfaceContractMapper());
                WireObjectFactory<B> factory = new WireObjectFactory<B>(businessInterface, wire, proxyService);
                return new ServiceReferenceImpl<B>(businessInterface, factory);
            }
        }
        return null;

    }

}
