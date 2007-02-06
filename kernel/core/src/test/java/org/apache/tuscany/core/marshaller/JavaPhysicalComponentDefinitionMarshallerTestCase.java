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
package org.apache.tuscany.core.marshaller;

import java.net.URI;

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.core.component.JavaPhysicalComponentDefinition;
import org.apache.tuscany.spi.marshaller.ModelMarshaller;
import org.apache.tuscany.spi.util.stax.StaxUtil;

import junit.framework.TestCase;

public class JavaPhysicalComponentDefinitionMarshallerTestCase extends TestCase {

    public JavaPhysicalComponentDefinitionMarshallerTestCase(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUnmarshall() throws Exception {
        
        String xml = 
            "<ns:componentJava componentId=\"uri\" xmlns:ns=\"http://tuscany.apache.org/xmlns/1.0-SNAPSHOT\">" +
            "  <instanceFactoryByteCode>Base 64 Encoded byte code</instanceFactoryByteCode>" +
            "</ns:componentJava>";
        XMLStreamReader reader = StaxUtil.createReader(xml);
        
        ModelMarshaller<JavaPhysicalComponentDefinition> marshaller = new JavaPhysicalComponentDefinitionMarshaller();
        JavaPhysicalComponentDefinition definition = marshaller.unmarshall(reader);
        
        assertEquals(new URI("uri"), definition.getComponentId());
        assertEquals("Base 64 Encoded byte code", new String(definition.getInstanceFactoryByteCode()));
        
    }

}
