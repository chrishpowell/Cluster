/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Class to handle items of a tree, specifically determining frequent items.  It
 * uses a frequent pattern growth algorithm.
 * 
 * NB: NOT thread safe.
 * NB: Note freqItems *must* be sorted in ascending and descending order for
 * this class to function correctly (usually via SQL functions).
 * 
 * See following:
 * Clustering algorithms in https://dataminingbook.info/book_html/chap8/book-watermark.html
 * [Data Mining and Machine Learning, Zaki and Meira]
 * 
 * Also: https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf [Hierarchical Document
 * Clustering Using Frequent Itemsets, B Fung et al]
 * 
 * Freq. Pattern tree building in https://www.arcjournals.org/pdfs/ijrscse/v4-i4/3.pdf
 * [Analyzing Working of FP-Growth Algorithm for Frequent Pattern Mining, Kaur and Jagdev]
 * 
 * Also useful:
 * http://berlin.csie.ntnu.edu.tw/PastCourses/2004S-MachineLearningandDataMining/lectures/MLDM2004S_Paper-Hierarchical Document Clustering Using Frequent Itemsets.pdf
 * 
 * @author Chris Powell
 * Note: Item must implement equals() and compareTo()
 *
 */
public class FTTree extends DTree<Item>
{
    // Flag if visited
    private boolean     visited = false;
    // Conditional Pattern Base count
    private int         cptbCount = 0;

    // Node head map: Item to cross-link (tree) list with Idx 0 being head
    private Map<Item,List<DTree<Item>>>  nodeHeadMap = new HashMap<>();

    
    /**
     * Constructor.
     * @param contents 
     */
    public FTTree(Item contents)
    {
        super(contents);
    }
    
    /**
     * Create a (single) root node for this tree.
     * 
     * @param root item
     * @return 
     */
    public static Root initialise(Item root)
    {
        return new Root(root);
    }

    static class Root extends FTTree
    {
        public Root(Item contents)
        {
            super(contents);
        }
    }

    
    /**
     * Mutators.
     * @return 
     */
    public int getCptbCount() { return cptbCount; }
    public void incrCptbCount() { ++this.cptbCount; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    
    /**
     * Get children of this node.
     * 
     * @return 
     */
    public List<FTTree> getChildren()
    {
        return children.stream().map(c -> (FTTree)c).collect(Collectors.toList());
    }
    
    /**
     * Map of list of "head" nodes.
     * @return 
     */
    public Map<Item,List<DTree<Item>>> getNodeHeadMap() { return nodeHeadMap; }
    
    /**
     * Copy a node (not a shallow, but not a deep copy).
     * 
     * @param oldNode
     * @return 
     */
    public FTTree simpleCopy( FTTree oldNode )
    {
        FTTree copyNode = new FTTree( oldNode.getContents() );
        copyNode.nodeCount = oldNode.nodeCount;
        
        return copyNode;
    }
    
    /**
     * Add list of item nodes to a root node as determined by fpGrowth() algo.
     * Constructs a (non-binary) tree of nodes matching along path, incrementing
     * counts.
     * 
     * @param items List of items to be stored in tree.
     */
    public void matchAndInsert( List<Item> items )
    {
        // Insert from root of items
        insertTree(this,this,0,items, 0);
    }

    /**
     * Recursive routine to insert a list of items into tree.  Also creates
     * cross-link maps (paths across the tree for nodes with the same contents).
     * 
     * Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html
     * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf
     * http://berlin.csie.ntnu.edu.tw/PastCourses/2004S-MachineLearningandDataMining/lectures/MLDM2004S_Paper-Hierarchical Document Clustering Using Frequent Itemsets.pdf
     * 
     * @param root
     * @param treeNode
     * @param itemIdx
     * @param items
     * @param initCount
     * @param incrBy
     */
    private void insertTree( FTTree root, FTTree treeNode, int itemIdx, List<Item> items, int initCount )
    {
        // End of Item list?
        if( itemIdx >= items.size() ) return;
        // Get Item (in index order)
        Item item = items.get(itemIdx);

        // Find children contents matching current item
        Optional<FTTree> tNode = treeNode.getChildren().stream()        // Get the children
                .filter(lc -> item.equals(lc.getContents()))            // Match predicate (current item)
                .findFirst();

        // Does this node have any matching children contents...?
        // (Certainly not on first call after initialise())
        if( tNode.isPresent() )                                         // ...Yes
        {
            // Node already exists
            FTTree nextNode = tNode.get();
//            nextNode.incrNodeCount();
            
            insertTree( root, nextNode, ++itemIdx, items, initCount );
        }
        else                                                            // ...No
        {
            // Ok, need to add a new node
            FTTree nextNode = (FTTree)treeNode.addChild(new FTTree(item));
            nextNode.setNodeCount(initCount);  // Only necessary to override default
            
//            System.out.println("  Adding new node: " +nextNode.getContents().getName());

            // Item
            Item itm = nextNode.getContents();
            // New node on tree, incr along path ***********************
            root.incrNodeCount();
            
            // Increment counts along path
                    //----------------------
                    List<DTree<Item>> path = traceParents(nextNode);
//                    System.out.println("  Path of node: " +nextNode.getContents().getName());
//                    System.out.print("  > ");
                    path.forEach(n -> {
//                        nextNode.sumNodeCount(incrBy);
                        n.incrNodeCount();
//                        System.out.print(" " +n.getContents().getName());
                    });
//                    System.out.println("");
                    //----------------------
            
            // "First" node head links
            if( !nodeHeadMap.containsKey(itm) )
            { //...Whole new node
                List<DTree<Item>> nodeLinks = new ArrayList<>();
                nodeLinks.add(nextNode);
                nodeHeadMap.put(itm, nodeLinks);
            }
            else
            { //...Link between nodes with same item
                nodeHeadMap.get(itm).add(nextNode);
            }
            
            insertTree( root, nextNode, ++itemIdx, items, initCount );
        }
    }
    
    /**
     * Add nodes to tree as determined by fpGrowth() algo.
     * 
     * @param items
     * @param initCount
     * @param incrBy 
     */
    private void matchAndInsertFreq( List<Item> items, int initCount, int incrBy )
    {
        insertTree(this,this,0,items,initCount);
    }

    /**
     * Build Frequent Pattern trees. Freq. Pattern tree building in
     * https://www.arcjournals.org/pdfs/ijrscse/v4-i4/3.pdf
     * [Analyzing Working of FP-Growth Algorithm for Frequent Pattern Mining, Kaur and Jagdev]
     * Algorithm 8.2.3 in
     * https://dataminingbook.info/book_html/chap8/book-watermark.html
     * 
     * Note: This method expects a list of frequent items to initiate the process,
     * plus the cross link paths across the initial tree (formed in insertTree()).
     * 
     * For a compact tree, the most frequent items should be at the top of the
     * tree (that is, nodes having most references will be towards the root and
     * with a higher count.
     * 
     * For each frequent item, get the cross-link map entry of the (freq. item)
     * nodes in the initial tree
     * --------------------------------------------------
     * R, P, F as defined in algo.
     * FTTree rTree, Set<Item> pItemSet, Set<Item> fFreqItems
     * @param rTree (R) Freq. Pattern tree constructed from doc. db 
     * @param pItemSet (P)
     * @param fFreqItems (F)
     * @return 
     *         
     * Algo: Remove infrequent items from tree (R).  Below uses
     *   SupportCount.getFreqItemSupIncr(), ie: infrequent items already removed.
        // Algo: Insert subsets of tree (R) into freqItems (F)
        //... Both above handled outside of this routine (listFlattenedTree(), remove() and subsets())
     * ----------------------------------------------------
     */
    public List<FTTree> fpGrowth()
    {
        // Frequent itemsets to be mined from tree
        List<FTTree> freqItemSets = new ArrayList<>();
        
        SupportCount.globalSuppDesc().forEach((i,d) -> {
            System.out.println("   ##> " +i.getName()+ ", " +d);
        });

        /*
         * Process projected FP-trees for each frequent item.
         * Form an FP tree from cross-link paths, setting counts along the paths
         */
        // Only interested in frequent items, ordered by sup(i) desc, having highest support counts towards root
        SupportCount.globalSuppDesc().forEach((i,d) ->
        {
            // Initialise FP-tree for i
            FTTree t = initialise(i);

            // Get paths from initial tree cross-links
            List<List<DTree<Item>>> lpd = crosslinkPathTopDown(i);

            // For each entry build an FP tree via recursion
            lpd.forEach(p ->
            {
                // Create item list of i
                List<Item> items = new ArrayList<>();

                // Build path of items excluding i
                int pathSiz = p.size()-1;
                // Add path to FP-tree
                for( int ii=0; ii < pathSiz; ii++ )
                {
                    DTree<Item> node = p.get(ii);
                    items.add(node.getContents());
                }

                // Build the FP tree projections (items,initCount,incrBy)
                t.matchAndInsertFreq(items, p.get(pathSiz).getNodeCount(),p.get(pathSiz).getNodeCount());
            });

            // Add to tree list
            freqItemSets.add(t);
        });
        
        return freqItemSets;
    }

    /**
     * Depth first search.
     * 
     * @param node
     * @return 
     */
    public static List<FTTree> dfs( FTTree node )
    {
        List<FTTree> path = new ArrayList<>();
        
        node.setVisited(true);
        path.add(node);
        System.out.print(" Node: " +node.getContents());
        
        List<FTTree> ln = node.getChildren();
        ln.stream().filter(n0 -> n0!=null && !n0.isVisited()).forEach(n1 -> dfs(n1));
        
        return path;
    }
    
    /**
     * Follow path via parents.
     * 
     * @param node
     * @return 
     */
    public List<DTree<Item>> traceParents( DTree<Item> node )
    {
        List<DTree<Item>> path = new ArrayList<>();
        
        while( node.getParent() != null )
        {
            path.add(node);
            node = node.getParent();
        }
        
        return path;
    }
    
    /**
     * List of paths (path: List of DTree) from tree root to given cross-link
     * items (node contents) of node head map.
     * 
     * @param item
     * @return 
     */
    public List<List<DTree<Item>>> crosslinkPathTopDown( Item item )
    {
        List<List<DTree<Item>>> root2leaf = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<DTree<Item>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse (ie root->node)
        lfi.forEach(p ->
        {
            List<DTree<Item>> rp = traceParents(p);
            Collections.reverse(rp);
            root2leaf.add(rp);
        });
                
        return root2leaf;
    }
    
    /**
     * List of paths from cross-link items leaf (node contents) of
     * node head map to root.
     * 
     * @param item
     * @return 
     */
    public List<List<DTree<Item>>> crosslinkPathBottomUp( Item item )
    {
        List<List<DTree<Item>>> leaf2root = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<DTree<Item>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse
        lfi.forEach(p ->
        {
            List<DTree<Item>> rp = traceParents(p);
            leaf2root.add(rp);
        });
                
        return leaf2root;
    }
    
    /**
     * Find leaf item? Not functional atm.
     * @param tree
     * @return 
     */
    public List<Item> findLeafItems( FTTree tree )
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Convert streamed tree to a list.
     * 
     * @return 
     */
    public List<FTTree> listFlattenedTree()
    {
        // streamTree() from superclass
        return streamTree().map(s -> (FTTree)s).collect(Collectors.toList());
    }
    //--------------------------------------------------------------------------

    
    /**
     * Print simple tree.
     * Outputs denoted by:
     *   FTTree contents (NB: T may include contents counts)
     *   <> node (parent+children) count
     *   {} conditional pattern base count
     *   optional UID
     *
     * @param node node of tree 
     * @param appender indent char (usually spaces)
     * @param uid output if true
     */
    public static void printTree(FTTree node, String appender, boolean uid )
    {
        System.out.println(appender + node.getContents()+ ":<" +node.getNodeCount()+ ">{" +node.getCptbCount()+ "}|" +node.getFreqCount()+ "|" +(uid ? " "+node.getUuid().toString() : " "));
        node.getChildren().stream().map(each -> (FTTree)each).forEach(each ->  printTree((FTTree)each, appender + appender, uid));
    }
    
    /**
     * Dump children of given node.
     * @param <T>
     * @param n 
     */
    public static <T extends Comparable> void dumpFromNode( FTTree n )
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
        Map<Item,List<DTree<Item>>> nhm = tree.getNodeHeadMap();
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
        Map<Item,List<DTree<Item>>> nhm = tree.getNodeHeadMap();
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
     * Dump all paths to item type.
     * 
     * @param item (Implement toString())
     */
    public void dumpLeafPaths( Item item )
    {
        System.out.println("Paths to item: " +item);
        List<List<DTree<Item>>> lpd = crosslinkPathTopDown(item);
        lpd.forEach(lfi ->
        {
            System.out.print("> ");
            lfi.forEach(pd -> {
                System.out.print(" " +pd.getContents()+ "[" +pd.getNodeCount()+ "]");
            });
            System.out.println("");
        });
    }
    
    @Override
    public String toString()
    {
        return ""+contents;
    }
}
