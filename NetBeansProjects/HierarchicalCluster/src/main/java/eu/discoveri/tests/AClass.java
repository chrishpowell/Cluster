/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author chrispowell
 * @param <X>
 */
public abstract class AClass<X>
{
    private X                   contents = null;
    
    // Children of this node
    protected List<AClass<X>>   children = new ArrayList<>();
    // Parent of this node
    protected AClass<X>         parent = null;
    // Node (contents) count
    protected int               nodeCount = 1;

    
    public AClass(X contents)
    {
        this.contents = contents;
    }
    
    public void addChild(AClass<X> child)
    {
        child.setParent(this);
        this.children.add(child);
    }
    
    public List<AClass<X>> getOffspring()
    {
        return children;
    }

    public X getContents() { return contents; }
    protected void setParent(AClass<X> parent) { this.parent = parent; }
}
//
//class RootN<X> extends AClass<X>
//{
//    public RootN(X contents)
//    {
//        super(contents);
//    }
//}