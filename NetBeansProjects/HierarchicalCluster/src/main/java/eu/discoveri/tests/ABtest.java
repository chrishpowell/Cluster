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
public class ABtest
{
    public static void main(String[] args)
    {
        BClass.RootN root = BClass.initialise(new XClass("root"));
        root.output();
        
        BClass child1Node = new BClass(new XClass("child1"));
        root.addChild(child1Node);
        
        BClass child2Node = new BClass(new XClass("child2"));
        child1Node.addChild(child2Node);
        
        BClass child3Node = new BClass(new XClass("child3"));
        child3Node.addChild(child2Node);

        root.getOffspring().stream().map(c -> (BClass)c).forEach(c -> {
            System.out.println(">> " +c.getContents());
            c.getChildren().forEach(cn -> {
                System.out.println("  >> " +cn.getContents());
                cn.output();
            });
        });
    }
}
