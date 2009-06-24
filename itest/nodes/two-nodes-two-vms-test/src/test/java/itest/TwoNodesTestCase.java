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

package itest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import itest.nodes.Helloworld;

import java.io.File;
import java.net.URL;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasisopen.sca.client.SCAClient;
import org.oasisopen.sca.client.SCAClientFactory;

/**
 * This shows how to test the Calculator service component.
 */
public class TwoNodesTestCase{

    private static Node serviceNode;
    private static Node clientNode;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        NodeFactory factory = NodeFactory.newInstance();

        serviceNode = factory.createNode(new URL("file:./server-config.xml"));
        serviceNode.start();
        
        //clientNode = factory.createNode(new Contribution("client", getJar("../helloworld-client/target")));
        //clientNode.start();
    }

    /**
     * Get the jar in the target folder without being dependent on the version name to 
     * make tuscany releases easier
     */
    private static String getJar(String targetDirectory) {
        File f = new File(targetDirectory);
        for (File file : f.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                return file.toURI().toString();
            }
        }
        throw new IllegalStateException("Can't find jar in: " + targetDirectory);
    }
    
    @Test
    public void testNothing() throws Exception {

    }    

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (serviceNode != null) {
            serviceNode.stop();
        }
        if (clientNode != null) {
            clientNode.stop();
        }
    }
}