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
 * @param <X>
 */
public abstract class AClass1<X>
{
    private X                   contents = null;
    private X   root = null;
    
    // Children of this node
    protected List<AClass1<X>>   children = new ArrayList<>();
    // Parent of this node
    protected AClass1<X>         parent = null;
    // Node (contents) count
    protected int               nodeCount = 1;

    
    protected AClass1(X contents, boolean rootNode)
    {
        if( rootNode )
            this.root = contents;
        else
            this.contents = contents;
    }
    
    public AClass1(X contents)
    {
        this.contents = contents;
    }
    
    public void addChild(AClass1<X> child)
    {
        child.setParent(this);
        this.children.add(child);
    }
    
    public X getRoot() { return root; }
    public X getContents() { return contents; }
    protected void setParent(AClass1<X> parent) { this.parent = parent; }
}

