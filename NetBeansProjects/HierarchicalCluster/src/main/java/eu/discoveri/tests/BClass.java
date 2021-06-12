/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.tests;

import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author chrispowell
 */
public class BClass extends AClass<XClass>
{
    public BClass(XClass contents) { super(contents); }
        
    public static RootN initialise(XClass root)
    {
        return new RootN(root);
    }

    static class RootN extends BClass
    {
        public RootN(XClass contents)
        {
            super(contents);
        }
    }
    
    public List<BClass> getChildren()
    {
        return children.stream().map(c -> (BClass)c).collect(Collectors.toList());
    }
    
    /*
     * How to call from root???
    */
    public static void printTree(BClass node, String appender )
    {
        System.out.println(appender + node.getContents());
        node.getChildren().stream().map(each -> (BClass)each).forEach(each ->  printTree((BClass)each, appender + appender));
    }
    
    public void output() { System.out.println("BClass!!"); }
}
