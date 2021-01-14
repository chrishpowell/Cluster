/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;


/**
 * NB: NOT thread safe.
 * @author Chris Powell
 * @param <T> Contents of node.  Must implement equals().
 *
 */
public class FTTree<T>
{
    // Root node
    private static FTTree   root;
    // Unique id
    private final UUID      uuid = UUID.randomUUID();
    
    // Contents of node
    private T               contents = null;
    // Children of this node
    private List<FTTree<T>> children = new ArrayList<>();
    // Parent of this node
    private FTTree<T>       parent = null;
    // Flag if visited
    private boolean         visited = false;
    // Node (contents) count
    private int             nodeCount = 1;
    // Conditional Pattern Base count
    private int             cptbCount = 0;

    // Node head map: Item to cross-link (tree) list with Idx 0 being head
    private Map<T,List<FTTree<T>>>  nodeHeadMap = new HashMap<>();
    
    // List of conditional pattern bases
    private List<List<FTTree<T>>>   cpBases = new ArrayList<>();
    

    /**
     * Constructor.
     * @param contents 
     */
    public FTTree(T contents)
    {
        this.contents = contents;
    }
    
    /**
     * Create a root node.
     * 
     * @param <T>
     * @param item
     * @return 
     */
    public static <T> FTTree<T> initialiseTree(T item)
    {
        root = new FTTree(item);
        return root;
    }

    /**
     * Add a child to a node.
     * 
     * @param child
     * @return 
     */
    public FTTree<T> addChild(FTTree<T> child)
    {
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    /**
     * Get all children of a node.
     * 
     * @return 
     */
    public List<FTTree<T>> getChildren() { return children; }
    public void addChildren(List<FTTree<T>> children)
    {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }
    
    /**
     * Mutators.
     * @return 
     */
    public int getNodeCount() { return nodeCount; }
    private void sumNodeCount( int nodeCount  ) { this.nodeCount += nodeCount; }
    private void setNodeCount( int nodeCount ) { this.nodeCount = nodeCount; }
    public void incrNodeCount() { ++this.nodeCount; }

    public int getCptbCount() { return cptbCount; }
    public void incrCptbCount() { ++this.cptbCount; }

    public T getContents() { return contents; }
    public void setContents(T contents) { this.contents = contents; }

    public FTTree<T> getParent() { return parent; }
    private void setParent(FTTree<T> parent) { this.parent = parent; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    public static FTTree getRoot() { return root; }

    public UUID getUuid() { return uuid; }
    
    public Map<T,List<FTTree<T>>> getNodeHeadMap() { return nodeHeadMap; }
    
    /**
     * Copy a node (not a shallow, but not a deep copy).
     * 
     * @param oldNode
     * @return 
     */
    public FTTree<T> simpleCopy( FTTree<T> oldNode )
    {
        FTTree<T> copyNode = new FTTree( oldNode.getContents() );
        copyNode.nodeCount = oldNode.nodeCount;
        
        return copyNode;
    }

    /**
     * Merge lists of nodes into a conditional FP list. Constructs a list 
     * of nodes using a pre-determined node count from each entered node and
     * adding counts when a match occurs.
     * 
     * @param nodes 
     */
    public void conditionalFPlist( List<FTTree<T>> nodes )
    {
        // 
    }
    
    /**
     * Add list of items (creating a node) to a tree. Constructs a (non-binary)
     * tree of nodes matching along path, incrementing counts. T Must implement
     * equals().
     * 
     * @param node a node in the tree
     * @param items List of items to store in tree.
     */
    public void matchAndInsert( FTTree<T> node, List<T> items )
    {
        // Insert from root of items
        insertTree(node,node,0,items);
    }
    
    /**
     * Add list of item nodes to a root node. Constructs a (non-binary) tree of
     * nodes matching along path, incrementing counts. T Must implement equals().
     * 
     * @param items List of items to be stored in tree.
     */
    public void matchAndInsert( List<T> items )
    {
        // Insert from root of items
        insertTree(root,root,0,items);
    }

    /**
     * Insert a list of items into tree.  T must implement equals().
     * 
     * @param treeNode
     * @param itemIdx
     * @param items 
     */
    private void insertTree( FTTree<T> root, FTTree<T> treeNode, int itemIdx, List<T> items )
    {
        // Item index check
        if( itemIdx >= items.size() ) return;                   // End of list?
        // Item
        T item = items.get(itemIdx);
        
        // This will be the next node in the recursion
        FTTree<T> nextNode = null;

        // Does this node have any matching children contents...?
        Optional<FTTree<T>> tNode = treeNode.getChildren().stream()
            .filter(tn -> item.equals(tn.getContents())).findFirst();

        if( tNode.isPresent() )                                 // ...Yes
        {
            // Node already exists, increment count
            nextNode = tNode.get();
            nextNode.incrNodeCount();
        }
        else                                                    // ...No
        {
            // Ok, need to add a new node
            nextNode = treeNode.addChild(new FTTree(item));
            // Item
            T itm = nextNode.getContents();
            
            // "First" node head links
            if( !nodeHeadMap.containsKey(itm) )
            { //...Whole new node
                List<FTTree<T>> nodeLinks = new ArrayList<>();
                nodeLinks.add(nextNode);
                nodeHeadMap.put(itm, nodeLinks);
            }
            else
            { //...Link between nodes with same item
                nodeHeadMap.get(itm).add(nextNode);
            }
        }

        root.incrNodeCount();
        insertTree( root, nextNode, ++itemIdx, items );
    }

    /**
     * Depth first search.
     * 
     * @param <T>
     * @param node
     * @return 
     */
    public static <T> List<FTTree<T>> dfs( FTTree<T> node )
    {
        List<FTTree<T>> path = new ArrayList<>();
        
        node.setVisited(true);
        path.add(node);
        System.out.print(" Node: " +node.getContents());
        
        List<FTTree<T>> ln = node.getChildren();
        ln.stream().filter(n0 -> n0!=null && !n0.isVisited()).forEach(n1 -> dfs(n1));
        
        return path;
    }
    
    /**
     * Follow path via parents.
     * 
     * @param node
     * @return 
     */
    public List<FTTree<T>> traceParents( FTTree<T> node )
    {
        List<FTTree<T>> path = new ArrayList<>();
        
        while( node.getParent() != null )
        {
            path.add(node);
            node = node.getParent();
        }
        
        return path;
    }
    
    /**
     * List of paths from tree root to given cross-link item (node contents) of
     * node head map.
     * 
     * @param item
     * @param tree Initialised tree
     * @return 
     */
    public List<List<FTTree<T>>> crosslinkPathTopDown( FTTree tree, T item )
    {
        List<List<FTTree<T>>> root2leaf = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<FTTree<T>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse
        lfi.forEach(p ->
        {
            List<FTTree<T>> rp = tree.traceParents(p);
            Collections.reverse(rp);
            root2leaf.add(rp);
        });
                
        return root2leaf;
    }
    
    /**
     * List of paths from cross-link item leaf (node contents) of
     * node head map to root.
     * 
     * @param item
     * @param tree Initialised tree
     * @return 
     */
    public List<List<FTTree<T>>> crosslinkPathBottomUp( FTTree tree, T item )
    {
        List<List<FTTree<T>>> leaf2root = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<FTTree<T>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse
        lfi.forEach(p ->
        {
            List<FTTree<T>> rp = tree.traceParents(p);
            leaf2root.add(rp);
        });
                
        return leaf2root;
    }
    
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
        
        final FTTree<T> other = (FTTree<T>) obj;
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
    
    /**
     * Print tree with UUIDs etc.
     * @param <T>
     * @param node
     * @param appender 
     */
    public static <T> void printFullTree(FTTree<T> node, String appender)
    {
        System.out.println(appender + node);
        node.getChildren().forEach(each ->  printFullTree(each, appender + appender));
    }
    
    /**
     * Print simple tree.
     * @param <T>
     * @param node
     * @param appender 
     * @param uid 
     */
    public static <T> void printTree(FTTree<T> node, String appender, boolean uid )
    {
        System.out.println(appender + node.getContents()+ "(" +node.getNodeCount()+ "){" +node.getCptbCount()+ "}" +(uid ? " "+node.getUuid().toString() : " "));
        node.getChildren().forEach(each ->  printTree(each, appender + appender, uid));
    }
    
    /**
     * Dump children of given node.
     * @param <T>
     * @param n 
     */
    public static <T> void dumpFromNode( FTTree<T> n )
    {
        n.getChildren().forEach(System.out::println);
    }
    
    /**
     * Dump the node head links.
     * 
     * @param tree 
     */
    public static void dumpNodeHeadMap(FTTree tree)
    {
        System.out.println("Node Head Map (short):");
        Map<Item,List<FTTree<Item>>> nhm = tree.getNodeHeadMap();
        nhm.forEach((k,v) -> {
            System.out.println("Key: " +k+ "(" +v.get(0).getNodeCount()+ "):");
            v.forEach(i ->
            {
                Item item = i.getContents();
                System.out.print(" {" +item.getName()+ "} ");
            });
            System.out.println("\r\n");
        });
    }
    
    /**
     * Dump the node head links.
     * 
     * @param tree 
     */
    public static void dumpNodeHeadMapFull(FTTree tree)
    {
        System.out.println("Node Head Map (long)");
        Map<Item,List<FTTree<Item>>> nhm = tree.getNodeHeadMap();
        nhm.forEach((k,v) -> {
            System.out.println("Key: " +k+ "(" +v.get(0).getNodeCount()+ "):");
            v.forEach(i ->
            {
                System.out.print(" {[" +i.getUuid()+ "] " +i.getContents()+ "(" +i.getNodeCount()+ ")} ");
            });
            System.out.println("\r\n");
        });
    }
    
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        FTTree tree = FTTree.initialiseTree(new Item("root",0));

        // Count in opposite alpha order
        Item itemA = new Item("a",26);
        Item itemB = new Item("b",25);
        Item itemC = new Item("c",24);
//        Item itemD = new Item("d",23);
//        Item itemE = new Item("e",22);
        Item itemF = new Item("f",21);
        Item itemM = new Item("m",14);
        Item itemP = new Item("p",11);

        List<Item> ln01 = Arrays.asList(new Item[]{itemF,itemC,itemA,itemM,itemP});
        List<Item> ln02 = Arrays.asList(new Item[]{itemF,itemC,itemA,itemB,itemM});
        List<Item> ln03 = Arrays.asList(new Item[]{itemF,itemB});
        List<Item> ln04 = Arrays.asList(new Item[]{itemC,itemB,itemP});
        List<Item> ln05 = Arrays.asList(new Item[]{itemF,itemC,itemA,itemM,itemP});
//        List<Item> lnC = Arrays.asList(new Item[]{itemC});

        System.out.println("1st list (f,c,a,m,p)...");
        tree.matchAndInsert(ln01);
        System.out.println("2nd list (f,c,a,b,m)...");
        tree.matchAndInsert(ln02);
        System.out.println("3rd list (f,b)...");
        tree.matchAndInsert(ln03);
        System.out.println("4th list (c,b,p)...");
        tree.matchAndInsert(ln04);
        System.out.println("5th list (f,c,a,m,p)...");
        tree.matchAndInsert(ln05);

//------------------------------------------------------------------------------
//  Expect [()s denote count]:
//   root [0](21)
//         f [6](4)
//            c [3](3)
//               a [1](3)
//                  m [13](2)
//                     p [16](2)
//                  b [2](1)
//                     m [13](1)
//            b [2](1)
//         c [3](1)
//            b [2](1)
//               p [16](1)
//------------------------------------------------------------------------------
        System.out.println("\r\n==================================================");
        System.out.println("Note: ()s denote count");
        FTTree.printTree(tree, " ", false);
        System.out.println("====================================================");
        
        System.out.println("\r\n------------------------------------------------");
        dumpNodeHeadMapFull(tree);
        System.out.println("------------------------------------------------");
        
        
        //........................................................................................................
        // Paths from root to leaf [p]
        System.out.println("\r\n==================================================");
        System.out.println("Paths to p:");

        // All Ps paths to root
        List<List<FTTree<Item>>> llftP = tree.crosslinkPathBottomUp(tree, itemP);
        List<List<FTTree<Item>>> cllftP = new ArrayList<>();
        
        // Root to leaf (print)
        llftP.forEach(lp ->
        {
            FTTree<Item> leaf = lp.get(0);
            System.out.println("Leaf: " +leaf.getContents().getName()+ "(" +leaf.getNodeCount()+ ") [" +leaf.getUuid()+ "]");
            
            Collections.reverse(lp);
            System.out.print("  >>");
            lp.forEach(r -> System.out.print(" " +r.getContents().getName()+ "(" +r.getNodeCount()+ ") "));
            System.out.println("");
        });
                
        System.out.println("Conditional FP-tree");
        System.out.println("\r\n----------------------------------------------------");
        System.out.println("CFPtree (p):");

        int[] leafTot = new int[1];
        llftP.forEach(lp ->
        {
            FTTree<Item> leaf = lp.get(lp.size()-1);
            leafTot[0] += leaf.getNodeCount();
            
            // Set each node count to leaf count
            List<FTTree<Item>> cfptP = lp.stream()
                    .filter(p -> !p.equals(leaf))
                    .map(p ->
                    {
                        p.setNodeCount(leaf.getNodeCount());
                        return p;
                    })
                    .collect(Collectors.toList());
            
            cllftP.add(cfptP);
        });
        
        List<FTTree<Item>> merge = cllftP.get(0);
        cllftP.remove(0);
        
        cllftP.forEach(lp ->
        {
            lp.forEach(l ->
            {
                System.out.println("(l)--> " +l.getContents().getName()+ ", " +l.getContents().getCount());
                int i = merge.indexOf(l);
                if( i >=0 )
                {
                    FTTree<Item> m = merge.get(i);
                    System.out.println("merge val: " +m.getContents().getName()+"/"+m.getContents().getCount()+ ", Upd val: " +l.getContents().getName()+"/"+l.getContents().getCount());
                    m.sumNodeCount(l.getNodeCount());
                }
            });
        });
        
        // Removed unmatched nodes
        for( Iterator<FTTree<Item>> iter=merge.iterator(); iter.hasNext(); )
        {
            FTTree<Item> m = iter.next();
            if( m.getNodeCount() < leafTot[0] )
                iter.remove();
        }
        
        merge.forEach(m -> {
            System.out.println("....> " +m.getContents().getName()+ ": " +m.getNodeCount());
        });
        //........................................................................................................
//        FTTree cpTreeP = FTTree.initialiseTree(new Item("rootP",0));
//        
//        llftP.forEach(lp ->
//        {
//            FTTree<Item> leaf = lp.get(lp.size()-1);
//            
//            // Set each node count to leaf count
//            List<FTTree<Item>> cfptP = lp.stream()
//                                        .filter(p -> !p.equals(leaf))
//                                        .map(p ->
//                                        {
//                                            p.setNodeCount(leaf.getNodeCount());
//                                            return p;
//                                        })
//                                        .collect(Collectors.toList());
//
//            // Merge lists into conditional FP tree (list)
//            cpTreeP.conditionalFPlist(cfptP);
//        });
//
//        FTTree.printTree(cpTreeP, " ", false);
        System.out.println("====================================================");
        
        // Paths from root to leaf [m]
        System.out.println("\r\n==================================================");
        System.out.println("Paths to m:");
        List<List<FTTree<Item>>> llftM = tree.crosslinkPathBottomUp(tree, itemM);
        llftM.forEach(lp ->
        {
            FTTree<Item> leaf = lp.get(0);
            System.out.println("Leaf: " +leaf.getContents().getName()+ "(" +leaf.getNodeCount()+ ") [" +leaf.getUuid()+ "]");
            
            Collections.reverse(lp);
            System.out.print("  >>");
            lp.forEach(r -> System.out.print(" " +r.getContents().getName()+ "(" +r.getNodeCount()+ ") "));
            System.out.println("");
        });
        
        System.out.println("Conditional FP-tree");
        System.out.println("\r\n----------------------------------------------------");
        System.out.println("CFPtree (m):");
        FTTree cpTreeM = FTTree.initialiseTree(new Item("rootM",0));
        
        llftM.forEach(lp ->
        {
            FTTree<Item> leaf = lp.get(lp.size()-1);
            // Construct tree with 
            List<FTTree<Item>> cfptM = lp.stream()
                                        .filter(p -> !p.equals(leaf))
                                        .map(p ->
                                        {
                                            p.setNodeCount(leaf.getNodeCount());
                                            return p;
                                        })
                                        .collect(Collectors.toList());

            cpTreeM.conditionalFPlist(cfptM);
        });

        FTTree.printTree(cpTreeM, " ", false);
        System.out.println("====================================================");
    }
}

//------------------------------------------------------------------------------
class Item
{
    private final String  name;
    private final int     count;

    public Item(String name, int count)
    {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public int getCount() { return count; }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final Item other = (Item) obj;
        return this.name.equals(other.name);
    }
    
    @Override
    public String toString()
    {
        return name +" ["+ count+ "]";
    }
}
