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
package org.apache.tuscany.sca.assembly.builder.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.Contract;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.builder.ComponentPreProcessor;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * @version $Rev$ $Date$
 */
public class ComponentTypeBuilderImpl {
    private static final Logger logger = Logger.getLogger(ComponentTypeBuilderImpl.class.getName());
    
    protected static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200903";
    protected static final String BINDING_SCA = "binding.sca";
    protected static final QName BINDING_SCA_QNAME = new QName(SCA11_NS, BINDING_SCA);    
    
    private ComponentBuilderImpl componentBuilder;
    private Monitor monitor;
    private AssemblyFactory assemblyFactory;
    private SCABindingFactory scaBindingFactory;
    private InterfaceContractMapper interfaceContractMapper;


    public ComponentTypeBuilderImpl(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = utilities.getUtility(MonitorFactory.class);
        monitor = monitorFactory.createMonitor();
        
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        scaBindingFactory = modelFactories.getFactory(SCABindingFactory.class);       

        interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);
    }
    
    public void setComponentBuilder(ComponentBuilderImpl componentBuilder){
        this.componentBuilder = componentBuilder;
    }

    /**
     * Calculate the component type for the provided implementation
     * 
     * @param implementation
     * @return component type
     */
    public void createComponentType(Implementation implementation){
        if (!(implementation instanceof Composite)){
            // component type will have been calculated at resolve time
            return;
        }
        
        // create the composite component type as this was not
        // calculated at resolve time
        Composite composite = (Composite)implementation;
        
        // first make sure that each child component has been properly configured based
        // on its own component type
        for (Component component : composite.getComponents()) {
            
            // Check for duplicate component names
            if (composite.getComponent(component.getName()) == null) {
                Monitor.error(monitor, 
                              this, 
                              "assembly-validation-messages", 
                              "DuplicateComponentName", 
                              composite.getName().toString(),
                              component.getName());
            } 
            
            /* process structural hierarchy
             * replace by structuralParent links and associated processing
            // Push down the autowire flag from the composite to components
            if (component.getAutowire() == null) {
                component.setAutowire(composite.getAutowire());
            }
            
            // what else needs pushing down? 
            //  intents
            //  policySets
             
            */
            
            // configure the component from its component type
            componentBuilder.configureComponentFromComponentType(component);
        }

        // create the composite component type based on the promoted artifacts
        // from the components that it contains
        
        // index all the components, services and references in the
        // component type so that they are easy to find
        Map<String, Component> components = new HashMap<String, Component>();
        Map<String, ComponentService> componentServices = new HashMap<String, ComponentService>();
        Map<String, ComponentReference> componentReferences = new HashMap<String, ComponentReference>();
        indexComponentsServicesAndReferences(composite, components, componentServices, componentReferences);
        
        // services
        calculateServices(composite, components, componentServices);
        
        // references
        //calculateReferences(composite);
        
        // properties
        //calculateProperties(composite);
        
        // autowire
        //calculateAutowire(composite);
        
    }
    
    /**
     * Index components, services and references inside a composite.
     * 
     * @param composite
     * @param components
     * @param componentServices
     * @param componentReferences
     */
    private void indexComponentsServicesAndReferences(Composite composite,
                                                        Map<String, Component> components,
                                                        Map<String, ComponentService> componentServices,
                                                        Map<String, ComponentReference> componentReferences) {

        for (Component component : composite.getComponents()) {

            // Index components by name
            components.put(component.getName(), component);

            ComponentService nonCallbackService = null;
            int nonCallbackServices = 0;
            for (ComponentService componentService : component.getServices()) {

                // Index component services by component name / service name
                String uri = component.getName() + '/' + componentService.getName();
                componentServices.put(uri, componentService);

                // count how many non-callback services there are
                // if there is only one the component name also acts as the service name
                if (!componentService.isForCallback()) {

                    // Check how many non callback non-promoted services we have
                    if (nonCallbackServices == 0) {
                        nonCallbackService = componentService;
                    }
                    nonCallbackServices++;
                }
            }

            if (nonCallbackServices == 1) {
                // If we have a single non callback service, index it by
                // component name as well
                componentServices.put(component.getName(), nonCallbackService);
            }

            // Index references by component name / reference name
            for (ComponentReference componentReference : component.getReferences()) {
                String uri = component.getName() + '/' + componentReference.getName();
                componentReferences.put(uri, componentReference);
            }
        }
    }    
    
    
    /**
     * Connect the services in the component type to the component services that
     * they promote
     * 
     * @param componentType
     * @param component
     */
    private void calculateServices(ComponentType componentType,
                                   Map<String, Component> components,
                                   Map<String, ComponentService> componentServices){

        // Connect this component type's services to the 
        // services from child components which it promotes
        connectPromotedServices(componentType,
                                components,
                                componentServices);
        
        // look at each component type service in turn and 
        // calculate its configuration based on OASIS rules
        for (Service service : componentType.getServices()) {
            CompositeService compositeService = (CompositeService)service;
            Component promotedComponent = compositeService.getPromotedComponent();
            ComponentService promotedComponentService = compositeService.getPromotedService();
            
            // promote interface contracts
            calculatePromotedInterfaceContract(compositeService, promotedComponentService);
           
            // promote bindings
            calculatePromotedBindings(compositeService, promotedComponentService);
            
            // promote intents
            // calculatePromotedIntents(compositeService, promotedComponentService);
            
            // promote policy sets
            // calculatePromotedPolicySets(compositeService, promotedComponentService);
        
        }
               
    }
    
    /**
     * Connect the services in the component type to the component services that
     * they promote
     * 
     * @param componentType
     * @param component
     */
    private void connectPromotedServices(ComponentType componentType,
                                         Map<String, Component> components,
                                         Map<String, ComponentService> componentServices){

        for (Service service : componentType.getServices()) {
            // Connect composite (component type) services to the component services 
            // that they promote 
            CompositeService compositeService = (CompositeService)service;
            ComponentService componentService = compositeService.getPromotedService();
            if (componentService != null && componentService.isUnresolved()) {
                // get the name of the promoted component/service
                String promotedComponentName = compositeService.getPromotedComponent().getName();
                String promotedServiceName;
                if (componentService.getName() != null) {
                    if (compositeService.isForCallback()) {
                        // For callbacks the name already has the form "componentName/servicename"
                        promotedServiceName = componentService.getName();
                    } else {
                        promotedServiceName = promotedComponentName + '/' + componentService.getName();
                    }
                } else {
                    promotedServiceName = promotedComponentName;
                }
                
                // find the promoted service
                ComponentService promotedService = componentServices.get(promotedServiceName);
                
                if (promotedService != null) {

                    // Point to the resolved component
                    Component promotedComponent = components.get(promotedComponentName);
                    compositeService.setPromotedComponent(promotedComponent);

                    // Point to the resolved component service
                    compositeService.setPromotedService(promotedService);
                } else {
                    Monitor.error(monitor, 
                                  this, 
                                  "assembly-validation-messages", 
                                  "PromotedServiceNotFound", 
                                  ((Composite)componentType).getName().toString(),
                                  promotedServiceName);
                }                
            }
        }      
    }  
    
    /**
     * OASIS RULE: Interface contracts from higher in the implementation hierarchy takes precedence
     * 
     * @param compositeService
     * @param promotedComponentService
     */
    private void calculatePromotedInterfaceContract(CompositeService compositeService,
                                                    ComponentService promotedComponentService){
        // Use the interface contract from the promoted component service if
        // none is specified on the composite service
        InterfaceContract compositeServiceInterfaceContract = compositeService.getInterfaceContract();
        InterfaceContract promotedServiceInterfaceContract = promotedComponentService.getInterfaceContract();
        if (compositeServiceInterfaceContract == null) {
            compositeService.setInterfaceContract(promotedServiceInterfaceContract);
        } else if (promotedServiceInterfaceContract != null) {
            // Check that the compositeServiceInterfaceContract and promotedServiceInterfaceContract
            // are compatible
            boolean isCompatible =
                interfaceContractMapper.isCompatible(compositeServiceInterfaceContract,
                                                     promotedServiceInterfaceContract);
            if (!isCompatible) {
                Monitor.error(monitor, 
                              this, 
                              "assembly-validation-messages", 
                              "ServiceInterfaceNotSubSet", 
                              promotedComponentService.getName());
            }
        }         
    }
    
    /**
     * OASIS RULE: Bindings from higher in the implementation hierarchy take precedence
     * 
     * @param compositeService
     * @param promotedComponentService
     */    
    private void calculatePromotedBindings(CompositeService compositeService,
                                           ComponentService promotedComponentService){  
        // forward bindings
        if (compositeService.getBindings().isEmpty()) {
            for (Binding binding : promotedComponentService.getBindings()) {
                try {
                    compositeService.getBindings().add((Binding)binding.clone());
                } catch (CloneNotSupportedException ex) {
                    // this binding can't be used in the promoted service
                }
            }
        }
        
        if (compositeService.getBindings().isEmpty()) {
            createSCABinding(compositeService, null);
        }

        // callback bindings
        if (promotedComponentService.getCallback() != null){
            if (compositeService.getCallback() != null) {
                compositeService.getCallback().getBindings().clear();
            } else {
                compositeService.setCallback(assemblyFactory.createCallback());
            }
            
            for (Binding binding : promotedComponentService.getCallback().getBindings()) {
                try {
                    compositeService.getCallback().getBindings().add((Binding)binding.clone());
                } catch (CloneNotSupportedException ex) {
                    // this binding can't be used in the promoted service
                }
            }  
        }        
    }
    
    /**
     * Create a default SCA binding in the case that no binding
     * is specified by the user
     * 
     * @param contract
     * @param definitions
     */
    protected void createSCABinding(Contract contract, Definitions definitions) {

        SCABinding scaBinding = scaBindingFactory.createSCABinding();

        if (definitions != null) {
            for (ExtensionType attachPointType : definitions.getBindingTypes()) {
                if (attachPointType.getType().equals(BINDING_SCA_QNAME)) {
                    ((PolicySubject)scaBinding).setExtensionType(attachPointType);
                }
            }
        }

        contract.getBindings().add(scaBinding);
        contract.setOverridingBindings(false);
    }       
    

} //end class
