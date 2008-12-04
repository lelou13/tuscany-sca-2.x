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

package org.apache.tuscany.sca.itest.conversational;

import java.io.File;

import junit.framework.Assert;

import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatefulImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatefulNonConversationalCallbackImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatelessImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceStatefulImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceStatelessImpl;
import org.apache.tuscany.sca.node.Client;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StatelessStatefulTestCase {

    private static Node node;
    private static ConversationalClient conversationalStatelessClientStatelessService;
    private static ConversationalClient conversationalStatelessClientStatefulService;
    private static ConversationalClient conversationalStatefulClientStatelessService;
    private static ConversationalClient conversationalStatefulClientStatefulService; 
    private static ConversationalClient conversationalStatelessClientRequestService;
    private static ConversationalClient conversationalStatefulClientNonConversationalCallbackStatelessService;    

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            NodeFactory nodeFactory = NodeFactory.newInstance();
            node = nodeFactory.createNode(new File("src/main/resources/Conversational/conversational.composite").toURL().toString(),
                                             new Contribution("TestContribution", 
                                                                 new File("src/main/resources/Conversational").toURL().toString()));
                    
             
            node.start();
            
            conversationalStatelessClientStatelessService = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatelessClientStatelessService");
        
            conversationalStatelessClientStatefulService  = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatelessClientStatefulService");
        
            conversationalStatefulClientStatelessService  = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatefulClientStatelessService");
        
            conversationalStatefulClientStatefulService   = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatefulClientStatefulService");
            conversationalStatelessClientRequestService    = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatelessClientRequestService");
            conversationalStatefulClientNonConversationalCallbackStatelessService    = ((Client)node).getService(ConversationalClient.class, 
                                                                                  "ConversationalStatefulClientNonConversationalCallbackStatefulService");
                
            // reset the place where we record the sequence of calls passing
            // through each component instance
            ConversationalServiceStatelessImpl.calls = new StringBuffer();
            ConversationalServiceStatefulImpl.calls  = new StringBuffer();
            ConversationalClientStatelessImpl.calls  = new StringBuffer();         
            ConversationalClientStatefulImpl.calls   = new StringBuffer();
        
        } catch(Exception ex) {
                System.err.println(ex.toString());
        }
               
    }

    @AfterClass
    public static void tearDown() throws Exception {
        node.stop();
        conversationalStatelessClientStatelessService = null;
        conversationalStatelessClientStatefulService = null;
        conversationalStatefulClientStatelessService = null;
        conversationalStatefulClientStatefulService = null; 
        conversationalStatelessClientRequestService = null;
        conversationalStatefulClientNonConversationalCallbackStatelessService = null;    
    }
    
    private static void resetCallStack() {
        
        // reset the place where we record the sequence of calls passing
        // through each component instance
        ConversationalServiceStatelessImpl.calls = new StringBuffer();
        ConversationalServiceStatefulImpl.calls  = new StringBuffer();
        ConversationalClientStatelessImpl.calls  = new StringBuffer();         
        ConversationalClientStatefulImpl.calls   = new StringBuffer();    
        ConversationalClientStatefulNonConversationalCallbackImpl.calls = new StringBuffer();
        
    }    

    // stateless client stateful service tests
    // =======================================
    @Test
    public void testStatelessStatefulConversationFromInjectedReference() {
        int count = conversationalStatelessClientStatefulService.runConversationFromInjectedReference();
        Assert.assertEquals(2, count);
    } 
    
    @Test
    public void testStatelessStatefulConversationFromInjectedReference2() {
        int count = conversationalStatelessClientStatefulService.runConversationFromInjectedReference2();
        Assert.assertEquals(2, count);
    }     
    
    @Test
    public void testStatelessStatefulConversationFromServiceReference() {
        int count = conversationalStatelessClientStatefulService.runConversationFromServiceReference();
        Assert.assertEquals(2, count);
    }          

    @Test
    public void testStatelessStatefulConversationWithUserDefinedConversationId() {
        int count = conversationalStatelessClientStatefulService.runConversationWithUserDefinedConversationId();
        Assert.assertEquals(2, count);
    }   
   
    @Test
    public void testStatelessStatefulConversationCheckUserDefinedConversationId() {
        String conversationId = conversationalStatelessClientStatefulService.runConversationCheckUserDefinedConversationId();
        Assert.assertEquals("MyConversation2", conversationId);
    } 
    
    @Test
    public void testStatelessStatefulConversationCheckingScope() {
        resetCallStack();
        conversationalStatelessClientStatefulService.runConversationCheckingScope();
        Assert.assertEquals("init,initializeCount,incrementCount,retrieveCount,endConversation,destroy,", 
                            ConversationalServiceStatefulImpl.calls.toString());
    }     

    @Test
    public void testStatelessStatefulConversationWithCallback() {
        resetCallStack();
        int count = conversationalStatelessClientStatefulService.runConversationWithCallback();
        Assert.assertEquals(0, count);
               
        Assert.assertEquals("init,runConversationWithCallback,init,initializeCount,destroy,init,incrementCount,destroy,init,retrieveCount,destroy,init,endConversation,destroy,destroy,", 
                            ConversationalClientStatelessImpl.calls.toString());        
    }  
   
    //@Test
    public void testStatelessStatefulConversationHavingPassedReference() {
        int count = conversationalStatelessClientStatefulService.runConversationHavingPassedReference();
        Assert.assertEquals(3, count);
    }    
 
    //@Test
    public void testStatelessStatefulConversationBusinessException() {
        String message = conversationalStatelessClientStatefulService.runConversationBusinessException();
        Assert.assertEquals("Business Exception", message);
    }     

    //@Test
    public void testStatelessStatefulConversationBusinessExceptionCallback() {
        String message = conversationalStatelessClientStatefulService.runConversationBusinessExceptionCallback();
        Assert.assertEquals("Business Exception", message);
    }  
    
    @Test
    public void testStatelessStatefulConversationCallingEndedConversation() {
        int count = conversationalStatelessClientStatefulService.runConversationCallingEndedConversation();
        Assert.assertEquals(0, count);
    }     
    
    @Test
    public void testStatelessStatefulConversationCallingEndedConversationCallback() {
        int count = conversationalStatelessClientStatefulService.runConversationCallingEndedConversationCallback();
        Assert.assertEquals(0, count);
    }   
    
    @Test
    public void testStatelessStatefulConversationCallingEndedConversationCheckConversationId() {
        String id = conversationalStatelessClientStatefulService.runConversationCallingEndedConversationCheckConversationId();
        Assert.assertEquals(null, id);
    }     
    
    //@Test
    public void testStatelessStatefulConversationCallingEndedConversationCallbackCheckConversationId() {
        String id = conversationalStatelessClientStatefulService.runConversationCallingEndedConversationCallbackCheckConversationId();
        Assert.assertEquals(null, id);
    } 

        
}
