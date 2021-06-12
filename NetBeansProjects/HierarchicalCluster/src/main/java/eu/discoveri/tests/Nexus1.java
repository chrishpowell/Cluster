/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.tests;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author chrispowell
 * @param <T>
 */
public abstract class Nexus1<T>
{ // This syntax lets you confine T to a subclass of Nexus
    protected T contents;
    private Nexus1<T> parent;
    private List<Nexus1<T>> children = new ArrayList<>();
    

    protected Nexus1(T contents)
    {
        this.contents = contents;
    }

    /** @return a static for the class */
//    protected abstract Root<T> setRootOrChild();
    
    /**
     * Add child to this node.
     * 
     * @param child
     * @return 
     */
    public Nexus1<T> addChild(Nexus1<T> child)
    {
        child.setParent(this);
        this.children.add(child);
        return child;
    }
    
        /**
     * Get parent of this node.
     * @return 
     */
    public Nexus1<T> getParent() { return parent; }
    /**
     * Set a parent for this node.
     * @param parent 
     */
    protected void setParent(Nexus1<T> parent) { this.parent = parent; }

}

/** Bundled into one Object for simplicity of API */
class Root1<T> extends Nexus1<T>
{   
    public Root1(T contents)
    {
        super(contents);
    }
}

class ConcreteNexus1 extends Nexus1<Contents1>
{
    // This is the static object all instances will return from the method
    private static Root1<Contents1> root;

    public ConcreteNexus1(Contents1 c)
    {
        super(c);
    }
    
    public static Root1 createRoot(Contents1 contents)
    {
        return new Root1<>(contents);
    }
    
    public static Root1<Contents1> getRoot() { return root; }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        Nexus1<Contents1> root = ConcreteNexus1.createRoot(new Contents1("root"));
        Nexus1<Contents1> child1 = new ConcreteNexus1(new Contents1("child1"));
        
        root.addChild(child1);
    }
}

class Contents1
{
    private String name;

    public Contents1(String name) { this.name = name; }
    public String getName() { return name; }
}
