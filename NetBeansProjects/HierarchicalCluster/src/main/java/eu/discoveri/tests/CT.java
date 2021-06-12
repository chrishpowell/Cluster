/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.tests;

/**
 *
 * @author chrispowell
 */
public class CT
{
    public static void main(String[] args)
    {
        Nexus1<Contents1> root = ConcreteNexus1.createRoot(new Contents1("root"));
        Nexus1<Contents1> child1 = new ConcreteNexus1(new Contents1("child1"));
        
        root.addChild(child1);
    }
}
