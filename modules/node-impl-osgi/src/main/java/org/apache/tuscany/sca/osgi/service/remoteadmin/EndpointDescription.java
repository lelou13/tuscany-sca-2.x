/*
 *
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

package org.apache.tuscany.sca.osgi.service.remoteadmin;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Version;

/**
 * A description of an endpoint that provides sufficient information for a
 * compatible distribution provider to create a connection to this endpoint An
 * Endpoint Description is easy to transfer between different systems. This
 * allows it to be used as a communications device to convey available endpoint
 * information to nodes in a network. An Endpoint Description reflects the
 * perspective of an importer. That is, the property keys have been chosen to
 * match filters that are created by client bundles that need a service.
 * 
 * @Immutable
 */
public interface EndpointDescription {
    /**
     * Returns the configuration types. A distribution provider exports a
     * service with an endpoint. This endpoint uses some kind of communications
     * protocol with a set of configuration parameters. There are many different
     * types but each endpoint is configured by only one configuration type.
     * However, a distribution provider can be aware of different configuration
     * types and provide synonyms to increase the change a receiving distributon
     * provider can create a connection to this endpoint. This value represents
     * the RemoteConstants.SERVICE_IMPORTED_CONFIGS
     * 
     * @return Returns The configuration type used for the associated endpoint
     *         and optionally synonyms.
     */
    public List<String> getConfigurationTypes();

    /**
     * Return the list of intents implemented by this endpoint. The intents are
     * based on the service.intents on an imported service, except for any
     * intents that are additionally provided by the importing distribution
     * provider. All qualified intents must have been expanded. The property the
     * intents come from is RemoteConstants.SERVICE_INTENTS
     * 
     * @return A list of expanded intents that are provided by this endpoint.
     */
    public List<String> getIntents();

    /**
     * Answer the list of interfaces implemented by the exported service. If
     * this Endpoint Description does not map to a service, then this List must
     * be empty. The value of the interfaces is derived from the objectClass
     * property.
     * 
     * @return The list of Java interface names accessible by this endpoint
     */
    public List<String> getInterfaces();

    /**
     * Answer the version of the given interface. The version is encoded by
     * prefixing the given interface name with endpoint.version., and then using
     * this as a property key. The value must then be the Version object. For
     * example: endpoint.version.com.acme.Foo
     * 
     * @param name The name of the interface for which a version is requested
     * @return Returns The version of the given interface or null if the
     *         interface has no version in this Endpoint Description
     */
    public Version getInterfaceVersion(String name);

    /**
     * Returns An immutable map referring to the properties of this Endpoint
     * Description.
     * 
     * @return Returns all endpoint properties.
     */
    public Map<String, Object> getProperties();

    /**
     * Returns the universally unique id for the service exported through this
     * endpoint. Each service in the OSGi service registry has a universally
     * unique id. The UUID can be used to detect that two Endpoint Descriptions
     * really refer to the same registered service instance in some remote
     * framework. This UUID can be used to filter out duplicate ways of
     * communicating with the same service. The service UUID is constructed from
     * two properties. It is first the org.osgi.framework.uuid System property
     * set by the framework or through configuration. This property must
     * uniquely represents the UUID of a framework instance. This UUID must not
     * contain any dots ('.' .). This is suffixed with a dot and then the
     * service.id service property of the service.
     * <p>
     * For example: 72dc5fd9-5f8f-4f8f-9821-9ebb433a5b72.121
     * <p>
     * If this Endpoint Description does not map to a remote OSGi service, for
     * example some web service, then the Endpoint Description must not have a
     * service UUID. If two endpoints have the same URI, then they must refer to
     * the same OSGi service.
     * 
     * @return Returns Unique id of a service or null if this Endpoint
     *         Description does not relate to an OSGi service
     */
    public String getRemoteServiceID();

    /**
     * Returns the endpoint's URI. The URI is an opaque id for an endpoint in
     * URI form. No two different endpoints must have the same URI, two Endpoint
     * Descriptions with the same URI must represent the same endpoint. The
     * value of the URI is stored in the RemoteConstants.ENDPOINT_URI property.
     * 
     * @return Returns The URI of the endpoint, never null.
     */
    public URI getURI();

    /**
     * Two endpoints are equal if their URIs are equal, the hash code is
     * therefore derived from the URI.
     */
    public int hashCode();

    /**
     * Two endpoints are equal if their URIs are equal.
     * 
     * @param other
     * @return
     */
    public boolean equals(Object other);

}
