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
public class XClass
{
    private String name;
    
    public XClass(String name){ this.name = name; }
    
    @Override
    public String toString()
    {
        return name;
    }
}
