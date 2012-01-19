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
package helloworld;

import yetanotherpackage.DBean;
import anotherpackage.BBean;



public class ABean {
    
    protected String field1;
    protected String field2;
    protected BBean field3;
    protected DBean field4;

    public String getField1() {
        return field1;
    }
    
    public void setField1(String field1) {
        this.field1 = field1;
    }
    
    public String getField2() {
        return field2;
    }
    
    public void setField2(String field2) {
        this.field2 = field2;
    }

    public BBean getField3() {
        return field3;
    }
    
    public void setField3(BBean field3) {
        this.field3 = field3;
    }
    
    public DBean getField4() {
        return field4;
    }
    
    public void setField4(DBean field4) {
        this.field4 = field4;
    }
}

