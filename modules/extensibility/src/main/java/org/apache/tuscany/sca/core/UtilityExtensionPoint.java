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

package org.apache.tuscany.sca.core;


/**
 * The extension point for the Tuscany core utility extensions.
 *
 * @version $Rev$ $Date$
 */
public interface UtilityExtensionPoint extends LifeCycleListener {

    /**
     * Add a utility to the extension point
     * @param utility The instance of the utility
     *
     * @throws IllegalArgumentException if utility is null
     */
    void addUtility(Object utility);

    /**
     * Get the utility by the interface
     * @param utilityType The lookup key (utility interface)
     * @return The instance of the utility
     *
     * @throws IllegalArgumentException if utilityType is null
     */
    <T> T getUtility(Class<T> utilityType);

    /**
     * Get a new instance of the utility by the interface
     * @param utilityType The lookup key (utility interface)
     * @param newInstance A new instance is required
     * @return The instance of the utility
     *
     * @throws IllegalArgumentException if utilityType is null
     */
    <T> T getUtility(Class<T> utilityType, boolean newInstance);

    /**
     * Remove a utility
     * @param utility The utility to remove
     *
     * @throws IllegalArgumentException if utility is null
     */
    void removeUtility(Object utility);
}
