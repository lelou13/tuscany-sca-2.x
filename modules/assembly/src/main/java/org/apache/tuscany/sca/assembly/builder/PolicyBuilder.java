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

package org.apache.tuscany.sca.assembly.builder;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;

/**
 * A builder that is contributed by a specific policy languange to validate the configurations
 * for SCA endpoints, endpoint references and component implementations
 */
public interface PolicyBuilder<T> {
    /**
     * Get the policy type
     * @return
     */
    QName getPolicyType();

    /**
     * Build (and validate) the policy settings on the endpoint
     * @param endpoint
     * @param monitor
     * @return true if the policy setting is compatible
     */
    boolean build(Endpoint endpoint, BuilderContext context);

    /**
     * Build (and validate) the policy settings on the endpoint reference
     * @param endpointReference
     * @param monitor
     * @return true if the policy setting is compatible
     */
    boolean build(EndpointReference endpointReference, BuilderContext context);

    /**
     * Build (and validate) the policy settings on the component implementation
     * @param component
     * @param implementation
     * @param monitor
     * @return true if the policy setting is compatible
     */
    boolean build(Component component, Implementation implementation, BuilderContext context);
    
    /**
     * Build (and validate) the policy settings on the endpoint reference is compatible with the endpoint
     * @param endpointReference 
     * @param endpoint
     * @param context
     * @return if the policy setting is compatible
     */
    boolean build(EndpointReference endpointReference, Endpoint endpoint, BuilderContext context);
}