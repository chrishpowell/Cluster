/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author chrispowell
 */
public class ItemsTree extends FTTree<Item>
{
    public ItemsTree(Item item) { super(item); }
        
    public static RootNOTNEST initialise(Item root)
    {
        return new RootNOTNEST(root);
    }
    
    public List<Item> getChildren1()
    {
        return children.stream().map(c -> (Item)c).collect(Collectors.toList());
    }
}
