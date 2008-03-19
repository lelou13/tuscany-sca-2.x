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

package org.apache.tuscany.sca.host.embedded.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.assembly.builder.DomainBuilder;
import org.apache.tuscany.sca.context.ContextFactoryExtensionPoint;
import org.apache.tuscany.sca.context.DefaultContextFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionService;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.assembly.RuntimeAssemblyFactory;
import org.apache.tuscany.sca.core.invocation.MessageFactoryImpl;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.definitions.SCADefinitionsProvider;
import org.apache.tuscany.sca.definitions.SCADefinitionsProviderExtensionPoint;
import org.apache.tuscany.sca.definitions.impl.SCADefinitionsImpl;
import org.apache.tuscany.sca.definitions.util.SCADefinitionsUtil;
import org.apache.tuscany.sca.definitions.xml.SCADefinitionsDocumentProcessor;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;
import org.apache.tuscany.sca.extensibility.ServiceDiscovery;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.policy.DefaultIntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.IntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.work.WorkScheduler;

public class ReallySmallRuntime {
	private final static Logger logger = Logger.getLogger(ReallySmallRuntime.class.getName());
    private List<ModuleActivator> modules;
    private ExtensionPointRegistry registry;

    private ClassLoader classLoader;
    private AssemblyFactory assemblyFactory;
    private ContributionService contributionService;
    private CompositeActivator compositeActivator;
    private CompositeBuilder compositeBuilder;
    private DomainBuilder domainBuilder;    
    private WorkScheduler workScheduler;
    private ScopeRegistry scopeRegistry;
    private ProxyFactory proxyFactory;
    private List scaDefnsSink = new ArrayList();

    public ReallySmallRuntime(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void start() throws ActivationException {
    	long start = System.currentTimeMillis();
    	
        // Create our extension point registry
        registry = new DefaultExtensionPointRegistry();

        //Get work scheduler
        workScheduler = registry.getExtensionPoint(WorkScheduler.class);

        // Create an interface contract mapper
        InterfaceContractMapper mapper = new InterfaceContractMapperImpl();

        // Get factory extension point
        ModelFactoryExtensionPoint factories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
        
        // Create context factory extension point
        ContextFactoryExtensionPoint contextFactories = new DefaultContextFactoryExtensionPoint();
        registry.addExtensionPoint(contextFactories);
        
        // Create Message factory
        MessageFactory messageFactory = new MessageFactoryImpl();
        factories.addFactory(messageFactory);

        // Create a proxy factory
        proxyFactory = ReallySmallRuntimeBuilder.createProxyFactory(registry, mapper, messageFactory);

        // Create model factories
        assemblyFactory = new RuntimeAssemblyFactory();
        factories.addFactory(assemblyFactory);
        PolicyFactory policyFactory = new DefaultPolicyFactory();
        factories.addFactory(policyFactory);
        
        // Load the runtime modules
        modules = loadModules(registry);
        
        // Start the runtime modules
        startModules(registry, modules);
        
        SCABindingFactory scaBindingFactory = factories.getFactory(SCABindingFactory.class);
        IntentAttachPointTypeFactory intentAttachPointTypeFactory = new DefaultIntentAttachPointTypeFactory();
        factories.addFactory(intentAttachPointTypeFactory);
        ContributionFactory contributionFactory = factories.getFactory(ContributionFactory.class); 
        
        // Create a contribution service
        contributionService = ReallySmallRuntimeBuilder.createContributionService(classLoader,
                                                                                  registry,
                                                                                  contributionFactory,
                                                                                  assemblyFactory,
                                                                                  policyFactory,
                                                                                  mapper,
                                                                                  scaDefnsSink);
        
        // Create the ScopeRegistry
        scopeRegistry = ReallySmallRuntimeBuilder.createScopeRegistry(registry); 
        
        // Create a composite activator
        compositeActivator = ReallySmallRuntimeBuilder.createCompositeActivator(registry,
                                                                                assemblyFactory,
                                                                                messageFactory,
                                                                                scaBindingFactory,
                                                                                mapper,
                                                                                proxyFactory,
                                                                                scopeRegistry,
                                                                                workScheduler);

        // Load the definitions.xml
        loadSCADefinitions(registry);
        
        if (logger.isLoggable(Level.FINE)) {
            long end = System.currentTimeMillis();
            logger.fine("The tuscany runtime is started in " + (end - start) + " ms.");
        }
    }
    
    public void stop() throws ActivationException {
    	long start = System.currentTimeMillis();

        // Stop the runtime modules
        stopModules(registry, modules);

        // Stop and destroy the work manager
        workScheduler.destroy(); 

        // Cleanup
        modules = null;
        registry = null;
        assemblyFactory = null;
        contributionService = null;
        compositeActivator = null;
        workScheduler = null;
        scopeRegistry = null;
        
        if (logger.isLoggable(Level.FINE)) {
            long end = System.currentTimeMillis();
            logger.fine("The tuscany runtime is stopped in " + (end - start) + " ms.");
        }
    }
    
    public void buildComposite(Composite composite) throws CompositeBuilderException {
        //Get factory extension point
        ModelFactoryExtensionPoint factories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
        SCABindingFactory scaBindingFactory = factories.getFactory(SCABindingFactory.class);
        IntentAttachPointTypeFactory intentAttachPointTypeFactory = factories.getFactory(IntentAttachPointTypeFactory.class);
        InterfaceContractMapper mapper = new InterfaceContractMapperImpl();
        
        //Create a composite builder
        compositeBuilder = ReallySmallRuntimeBuilder.createCompositeBuilder(assemblyFactory,
                                                                            scaBindingFactory,
                                                                            intentAttachPointTypeFactory,
                                                                            mapper);
        compositeBuilder.build(composite);
        
    }
    
    public ContributionService getContributionService() {
        return contributionService;
    }

    public CompositeActivator getCompositeActivator() {
        return compositeActivator;
    }
    
    public CompositeBuilder getCompositeBuilder() {
        return compositeBuilder;
    }

    public AssemblyFactory getAssemblyFactory() {
        return assemblyFactory;
    }

    public DomainBuilder getDomainBuilder() {
        if ( domainBuilder == null ) {
            //Create a domain builder
            //Get factory extension point
            ModelFactoryExtensionPoint factories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
            SCABindingFactory scaBindingFactory = factories.getFactory(SCABindingFactory.class);
            IntentAttachPointTypeFactory intentAttachPointTypeFactory = factories.getFactory(IntentAttachPointTypeFactory.class);
            InterfaceContractMapper mapper = new InterfaceContractMapperImpl();
            domainBuilder = ReallySmallRuntimeBuilder.createDomainBuilder(assemblyFactory,
                                                                          scaBindingFactory,
                                                                          intentAttachPointTypeFactory,
                                                                          mapper);
        }
        return domainBuilder;
    }
    
    private void  loadSCADefinitions(ExtensionPointRegistry registry) throws ActivationException {
        try {
            URLArtifactProcessorExtensionPoint documentProcessors = registry.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
            SCADefinitionsDocumentProcessor definitionsProcessor = (SCADefinitionsDocumentProcessor)documentProcessors.getProcessor(SCADefinitions.class);
            SCADefinitionsProviderExtensionPoint scaDefnProviders = registry.getExtensionPoint(SCADefinitionsProviderExtensionPoint.class);
            
            SCADefinitions systemSCADefinitions = new SCADefinitionsImpl();
            SCADefinitions aSCADefn = null;
            for ( SCADefinitionsProvider aProvider : scaDefnProviders.getSCADefinitionsProviders() ) {
               aSCADefn = aProvider.getSCADefinition(); 
               SCADefinitionsUtil.aggregateSCADefinitions(aSCADefn, systemSCADefinitions);
            }
            
            //we cannot expect that providers will add the intents and policysets into the resolver
            //so we do this here explicitly
            for ( Intent intent : systemSCADefinitions.getPolicyIntents() ) {
                definitionsProcessor.getSCADefinitionsResolver().addModel(intent);
            }
            
            for ( PolicySet policySet : systemSCADefinitions.getPolicySets() ) {
                definitionsProcessor.getSCADefinitionsResolver().addModel(policySet);
            }
            
            for ( IntentAttachPointType attachPoinType : systemSCADefinitions.getBindingTypes() ) {
                definitionsProcessor.getSCADefinitionsResolver().addModel(attachPoinType);
            }
            
            for ( IntentAttachPointType attachPoinType : systemSCADefinitions.getImplementationTypes() ) {
                definitionsProcessor.getSCADefinitionsResolver().addModel(attachPoinType);
            }
            
            //now that all system sca definitions have been read, lets resolve them rightaway
            definitionsProcessor.resolve(systemSCADefinitions, 
                                         definitionsProcessor.getSCADefinitionsResolver());
        } catch ( Exception e ) {
            throw new ActivationException(e);
        }
        
        /*URLArtifactProcessorExtensionPoint documentProcessors = registry.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        SCADefinitionsDocumentProcessor definitionsProcessor = (SCADefinitionsDocumentProcessor)documentProcessors.getProcessor(SCADefinitions.class);
        
        try {
            Map<ClassLoader, Set<URL>> scaDefinitionFiles = 
            ServiceDiscovery.getInstance().getServiceResources("definitions.xml");
            
            SCADefinitions systemSCADefinitions = new SCADefinitionsImpl();
            for ( ClassLoader cl : scaDefinitionFiles.keySet() ) {
                for ( URL scaDefnUrl : scaDefinitionFiles.get(cl) ) {
                    SCADefinitions defnSubset = definitionsProcessor.read(null, null, scaDefnUrl);
                    SCADefinitionsUtil.aggregateSCADefinitions(defnSubset, systemSCADefinitions);
                }
            }
            
            definitionsProcessor.resolve(systemSCADefinitions, definitionsProcessor.getSCADefinitionsResolver());
            scaDefnsSink.add(systemSCADefinitions);
        } catch ( ContributionReadException e ) {
            throw new ActivationException(e);
        } catch ( ContributionResolveException e ) {
            throw new ActivationException(e);
        } catch ( IOException e ) {
            throw new ActivationException(e);
        }*/
    }
    
    @SuppressWarnings("unchecked")
    private List<ModuleActivator> loadModules(ExtensionPointRegistry registry) throws ActivationException {

        // Load and instantiate the modules found on the classpath (or any registered classloaders)
        modules = new ArrayList<ModuleActivator>();
        try {
            Set<ServiceDeclaration> moduleActivators =
                ServiceDiscovery.getInstance().getServiceDeclarations(ModuleActivator.class);
            Set<String> moduleClasses = new HashSet<String>();
            for (ServiceDeclaration moduleDeclarator : moduleActivators) {
                if (moduleClasses.contains(moduleDeclarator.getClassName())) {
                    continue;
                }
                moduleClasses.add(moduleDeclarator.getClassName());
                Class<?> moduleClass = moduleDeclarator.loadClass();
                ModuleActivator module = (ModuleActivator)moduleClass.newInstance();
                modules.add(module);
            }
        } catch (IOException e) {
            throw new ActivationException(e);
        } catch (ClassNotFoundException e) {
            throw new ActivationException(e);
        } catch (InstantiationException e) {
            throw new ActivationException(e);
        } catch (IllegalAccessException e) {
            throw new ActivationException(e);
        }

        return modules;
    }
    
    private void startModules(ExtensionPointRegistry registry, List<ModuleActivator> modules)
        throws ActivationException {
        boolean debug = logger.isLoggable(Level.FINE);
        // Start all the extension modules
        for (ModuleActivator module : modules) {
            long start = 0L;
            if (debug) {
                logger.fine(module.getClass().getName() + " is starting.");
                start = System.currentTimeMillis();
            }
            module.start(registry);
            if (debug) {
                long end = System.currentTimeMillis();
                logger.fine(module.getClass().getName() + " is started in " + (end - start) + " ms.");
            }
        }
    }

    private void stopModules(ExtensionPointRegistry registry, List<ModuleActivator> modules) {
        boolean debug = logger.isLoggable(Level.FINE);
        for (ModuleActivator module : modules) {
            long start = 0L;
            if (debug) {
                logger.fine(module.getClass().getName() + " is stopping.");
                start = System.currentTimeMillis();
            }
            module.stop(registry);
            if (debug) {
                long end = System.currentTimeMillis();
                logger.fine(module.getClass().getName() + " is stopped in " + (end - start) + " ms.");
            }
        }
    }

    /**
     * @return the proxyFactory
     */
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    /**
     * @return the registry
     */
    public ExtensionPointRegistry getExtensionPointRegistry() {
        return registry;
    }

}
