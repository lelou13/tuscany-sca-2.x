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

package launcher;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;

import calculator.CalculatorService;


/**
 * This client program shows how to create an embedded SCA runtime, start it,
 * and locate and invoke a SCA component 
 */
public class SampleJSELauncher extends RuntimeIntegration {
    
    public static void main(String[] args) throws Exception {
        SampleJSELauncher launcher = new SampleJSELauncher();
        
        String contribution = null;
        
        if (args == null || args.length != 1){
            System.out.println("Please provide the name of the sample contribution to run as a parameter");
            System.out.println("Running binding-sca-calculator by default");
            contribution = "contribution-binding-sca-calculator";
        } else {
            contribution = args[0];
        }   
        
        if (contribution.equals("contribution-binding-sca-calculator")){
            launcher.launchBindingSCACalculator();
        } else if (contribution.equals("contribution-binding-ws-calculator")){
            launcher.launchBindingWSCalculator();
        } else if (contribution.equals("contribution-binding-rmi-calculator")){
            launcher.launchBindingRMICalculator();    
        } else if (contribution.equals("contribution-implementation-java-calculator")){
            launcher.launchImplementationJavaCalculator();              
        } else {
            System.out.println("Sample contribution " + contribution + "not found");
        }
    }
       
    /**
     * The contribution-binding-sca-calculator contribution includes a client component 
     * that calls the CalculatorServiceComponent from an operation marked by @Init. 
     */
    public void launchBindingSCACalculator(){
        Node node = startNode(new Contribution("c1", "../contribution-binding-sca-calculator/target/sample-contribution-binding-sca-calculator.jar"));
              
        stopNode(node);
    }    
    
    /*
     * Using a Tuscany specific mechanism for getting at local service proxies
     */
    public void launchBindingWSCalculator(){
        Node node = startNode(new Contribution("c1", "../contribution-binding-ws-calculator/target/classes"));
        
        CalculatorService calculator = node.getService(CalculatorService.class, "CalculatorServiceComponent");
        // TODO - could use JUnit assertions but don't want to have to handle JUnit dependency from Ant script
        double result = calculator.add(3, 2);
        System.out.println("3 + 2 = " + result);
        if (result != 5.0){
            throw new SampleLauncherException();
        }
        
        stopNode(node);
    }
    
    /*
     * Using a Tuscany specific mechanism for getting at local service proxies
     */
    public void launchBindingRMICalculator(){
        Node node1 = startNode(new Contribution("c1", "../binding-rmi/contribution-calculator-service/target/classes"));
        Node node2 = startNode(new Contribution("c1", "../binding-rmi/contribution-calculator-reference/target/classes"));
        
        CalculatorService calculator = node2.getService(CalculatorService.class, "CalculatorServiceComponent");
        // TODO - could use JUnit assertions but don't want to have to handle JUnit dependency from Ant script
        double result = calculator.add(3, 2);
        System.out.println("3 + 2 = " + result);
        if (result != 5.0){
            throw new SampleLauncherException();
        }
        
        stopNode(node2);
        stopNode(node1);
    }   
    
    /**
     * The contribution-binding-sca-calculator contribution includes a client component 
     * that calls the CalculatorServiceComponent from an operation marked by @Init. 
     */
    public void launchImplementationJavaCalculator(){
        Node node = startNode(new Contribution("c1", "../contribution-implementation-java-calculator/target/classes"));
               
        stopNode(node);
    }      
    
}
