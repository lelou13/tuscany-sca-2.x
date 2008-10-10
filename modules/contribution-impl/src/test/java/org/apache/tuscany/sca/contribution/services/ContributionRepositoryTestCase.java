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

package org.apache.tuscany.sca.contribution.services;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;

import org.apache.tuscany.sca.contribution.service.impl.ContributionRepositoryImpl;
import org.apache.tuscany.sca.contribution.service.util.FileHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContributionRepositoryTestCase {
    private static ContributionRepositoryImpl repository;

    @BeforeClass
    public static void setUp() throws Exception {
        // create repository (this should re-create the root directory)
        repository = new ContributionRepositoryImpl("target/repository/", XMLInputFactory.newInstance(), null);
        repository.init();
    }

    @Test
    public void testStore() throws Exception {
        String resourceLocation = "/repository/sample-calculator.jar";
        String contribution = "sample-calculator.jar";
        URL contributionLocation = getClass().getResource(resourceLocation);
        InputStream contributionStream = getClass().getResourceAsStream(resourceLocation);
        repository.store(contribution, contributionLocation, contributionStream);

        URL contributionURL = repository.find(contribution);
        assertNotNull(contributionURL);
    }

    @Test
    public void testRemove() throws Exception {
        String resourceLocation = "/repository/sample-calculator.jar";
        String contribution = "sample-calculator.jar";
        URL contributionLocation = getClass().getResource(resourceLocation);
        InputStream contributionStream = getClass().getResourceAsStream(resourceLocation);
        repository.store(contribution, contributionLocation, contributionStream);

        repository.remove(contribution);
        URL contributionURL = repository.find(contribution);
        assertNull(contributionURL);
    }

    @Test
    public void testList() throws Exception {
        String resourceLocation = "/repository/sample-calculator.jar";
        String contribution = "sample-calculator.jar";
        URL contributionLocation = getClass().getResource(resourceLocation);
        InputStream contributionStream = getClass().getResourceAsStream(resourceLocation);
        repository.store(contribution, contributionLocation, contributionStream);

        assertEquals(1, repository.list().size());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileHelper.deleteDirectory(new File("target/repository"));
    }
}
