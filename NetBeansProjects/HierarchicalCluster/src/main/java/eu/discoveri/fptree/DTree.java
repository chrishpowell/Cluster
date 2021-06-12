/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds (non-binary) trees. Nota Bene: The root of the tree is not represented
 * per se here, as it is a static (single) generic (which is troublesome...). The
 * strategy here is to have the root represented as a static inner class of the
 * concrete representation of this class: (eg: FTTree extends DTree).
 * 
 * @author chrispowell
 * @param <T>
 */
public class DTree<T extends Comparable>
{
    // Unique id
    private final UUID          uuid = UUID.randomUUID();
    
    // Contents of node
    protected T                 contents = null;
    // Children of this node
    protected List<DTree<T>>    children = new ArrayList<>();
    // Parent of this node
    protected DTree<T>          parent = null;
    // Node (and children nodes of this node) count
    protected int               nodeCount = 0;
    // Frequency count (the number of times the contents of this node appears in child nodes of this node).
    protected int               freqCount = 0;


    /**
     * Constructor to generate root node.
     * 
     * @param contents
     */
    protected DTree(T contents)
    {
        this.contents = contents;
    }
    
    /**
     * Add a child to a node.
     * 
     * @param child
     * @return 
     */
    public DTree<T> addChild(DTree<T> child)
    {
        child.setParent(this);
        this.children.add(child);
        return child;
    }
    
    /**
     * Add a list of children.
     * 
     * @param children 
     */
    public void addChildren(List<DTree<T>> children)
    {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    /**
     * Get all children of a node.  Note name allows subclasses to have a
     * getChildren() method.
     * 
     * @return 
     */
    public List<DTree<T>> getOffspring() { return children; }
    
    /**
     * Delete a given node.
     * 
     * @param node 
     */
    public void delNode( DTree<T> node )
    {
       List<DTree<T>> childrn = node.getOffspring();
       DTree<T> parnt = node.getParent();
       
       // Set parent's children
       parnt.children.clear();
       parnt.children.addAll(childrn);
       
       // Set children's parent
       children.forEach(child -> {
           child.setParent(parnt);
       });
    }
    
    /**
     * Get parent of this node.
     * @return 
     */
    public DTree<T> getParent() { return parent; }
    /**
     * Set a parent for this node.
     * @param parent 
     */
    protected void setParent(DTree<T> parent) { this.parent = parent; }
    
    /**
     * Get contents of this node
     * @return 
     */
    public T getContents() { return contents; }
    /**
     * Set contents of this node.
     * @param contents 
     */
    public void setContents(T contents) { this.contents = contents; }
    
    /**
     * Get count of nodes (includes child nodes of this node)
     * @return 
     */
    public int getNodeCount() { return nodeCount; }
    /**
     * Add a number to nodes (multiple node add)
     * @param nodeCount 
     */
    protected void sumNodeCount( int nodeCount  ) { this.nodeCount += nodeCount; }
    protected void setNodeCount( int nodeCount ) { this.nodeCount = nodeCount; }
    /**
     * Increment node count
     */
    protected void incrNodeCount() { ++this.nodeCount; }

    /**
     * Get frequency count of node.  The freq. count is the number of times the
     * contents of a node appear in the node (once) and in child node transactions.
     * @return 
     */
    public int getFreqCount() { return freqCount; }
    /**
     * Set freq. count.
     * @param freqCount 
     */
    public void setFreqCount(int freqCount) { this.freqCount = freqCount; }
    /**
     * Increment frequency count.
     */
    public void incrFreqCount() { ++this.freqCount; }

    /**
     * UUID
     * @return 
     */
    public UUID getUuid() { return uuid; }
        
    /**
     * Determine depth of a branch.
     * 
     * @param <T>
     * @param node
     * @param depth 
     */
    public static <T extends Comparable> void getBranchDepth(DTree<T> node, int depth)
    {
        List<DTree<T>> lft = node.getOffspring();
        System.out.print(node.getContents()+ "(" +node.getNodeCount()+ "):");
        for( DTree<T> tree: lft )
        {
            depth++;
            getBranchDepth(tree,depth);
        }
        System.out.println("");
    }

    /**
     * Convert this DTree (from node to all leaves) to a Stream (this + children)
     * 
     * @return 
     */
    public Stream<DTree<T>> streamTree()
    {
        return Stream.concat( Stream.of(this), children.stream().flatMap(DTree::streamTree) );
    }
    
    /**
     * Convert this DTree's children (from this node to all leaves) to a Stream
     * 
     * @return 
     */
    public Stream<DTree<T>> streamChildren()
    {
        return children.stream().flatMap(DTree::streamTree);
    }
    
    /**
     * Convert this DTree's children (from this node to all leaves) to a List
     * 
     * @return 
     */
    public List<DTree<T>> listAllChildren()
    {
        return children.stream().flatMap(DTree::streamTree).collect(Collectors.toList());
    }
    
    /**
     * Path (list) of all Parents excluding root.
     * 
     * @param node
     * @return 
     */
    public List<DTree<T>> pathOfAllParents( DTree<T> node )
    {
        return listAllParents(node,new ArrayList<>());
    }

    /**
     * Build path of parents.  Recursively move up path to root (but not
     * including root) adding to a list of nodes.
     * 
     * @param node Node from which path is constructed.
     * @param path List of nodes to root.
     * @return 
     */
    private List<DTree<T>> listAllParents(DTree<T> node, List<DTree<T>> path)
    {
        if( node != null )
        {
            // Add node to path
            path.add(node);
            listAllParents(node.getParent(),path);
        }
        
        return path;
    }
//------------------------------------------------------------------------------


    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    /**
     * Equates *contents* (contained class T) of this class.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final DTree<T> other = (DTree<T>) obj;
        return this.getContents().equals(other.getContents());
    }
    
    /**
     * Dump this node.
     * 
     * @return 
     */
    @Override
    public String toString()
    {
        return uuid.toString() +" : "+ contents+ ": Parent [" +parent+ "] numChildren: " +children.size()+ ", (" +nodeCount+ ")";
    }
}
