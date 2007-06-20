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
package org.apache.tuscany.implementation.osgi.invocation;


import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.implementation.osgi.OSGiImplementationInterface;
import org.apache.tuscany.implementation.osgi.context.OSGiPropertyValueObjectFactory;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.osgi.framework.BundleException;


/**
 * Builds a OSGi-based implementation provider from a component definition
 * 
 */
public class OSGiImplementationProviderFactory implements ImplementationProviderFactory<OSGiImplementationInterface> {
	
	
	DataBindingExtensionPoint dataBindingRegistry;
	
	public OSGiImplementationProviderFactory(DataBindingExtensionPoint dataBindings,
			OSGiPropertyValueObjectFactory factory) {
		
		dataBindingRegistry = dataBindings;
		
	}

	public ImplementationProvider createImplementationProvider(RuntimeComponent component,
            OSGiImplementationInterface implementation) {
    			
			try {
				
				return new OSGiImplementationProvider(component, implementation, dataBindingRegistry);
				
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		
    }

    public Class<OSGiImplementationInterface> getModelType() {
        return OSGiImplementationInterface.class;
    }

}
