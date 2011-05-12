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

package sample;

import java.util.List;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.binding.ws.axis2.provider.Axis2BaseBindingProvider;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

/**
 * @version $Rev: 792641 $ $Date: 2009-07-09 20:13:08 +0100 (Thu, 09 Jul 2009) $
 */
public class InteractionBindingWSReferencePolicyProvider extends BasePolicyProvider<InteractionBindingWSPolicy> {
    
    private EndpointReference endpointReference;

    public InteractionBindingWSReferencePolicyProvider(EndpointReference endpointReference) {
        super(InteractionBindingWSPolicy.class, endpointReference);
        
        this.endpointReference = endpointReference;
    }
    
    public void configureBinding(Object configuration) {
        Axis2BaseBindingProvider bindingProvider = (Axis2BaseBindingProvider)configuration;
        ConfigurationContext axisConfigurationContext = bindingProvider.getAxisConfigurationContext();
        AxisConfiguration axisConfiguration = axisConfigurationContext.getAxisConfiguration();
        List<org.apache.axis2.engine.Phase> outPhases = axisConfiguration.getOutFlowPhases();
        outPhases.get(0).addHandler(new TestAxisHandler("Reference OutFlow Handler"));
        List<org.apache.axis2.engine.Phase> inPhases = axisConfiguration.getInFlowPhases();
        inPhases.get(0).addHandler(new TestAxisHandler("Reference InFlow Handler"));      
    }
    
    public PhasedInterceptor createBindingInterceptor() {
        List<InteractionBindingWSPolicy> policies = findPolicies();   
        
        if (policies.isEmpty()){
            return null;
        } else {
            return new TestPolicyInterceptor(subject, 
                                             getContext(), 
                                             null, 
                                             policies, 
                                             Phase.REFERENCE_BINDING_POLICY);
        } 
    }

    public PhasedInterceptor createInterceptor(Operation operation) {
        List<InteractionBindingWSPolicy> policies = findPolicies();   
        
        if (policies.isEmpty()){
            return null;
        } else {
            return new TestPolicyInterceptor(subject, 
                                             getContext(), 
                                             operation, 
                                             policies, 
                                             Phase.REFERENCE_POLICY);
        }        
    }

}