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
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Class to handle items of a tree, specifically determining frequent items.  It
 * uses a frequent pattern growth algorithm.
 * 
 * NB: NOT thread safe.
 * NB: Note freqItems *must* be sorted in descending order for this class to
 * function correctly.
 * 
 * See following:
 * Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html
 * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf
 * http://berlin.csie.ntnu.edu.tw/PastCourses/2004S-MachineLearningandDataMining/lectures/MLDM2004S_Paper-Hierarchical Document Clustering Using Frequent Itemsets.pdf
 * 
 * @author Chris Powell
 * @param <T> Contents of node.  Must implement equals().
 *
 */
public class FTTree1<T extends Comparable> extends DTree<T>
{
    // Flag if visited
    private boolean     visited = false;
    // Conditional Pattern Base count
    private int         cptbCount = 0;
    
    // Set of freq. items (sorted incr. support)
    private List<T>     freqItemSup = new ArrayList<>();
    private List<T>     itemSupRev;

    // Node head map: Item to cross-link (tree) list with Idx 0 being head
    private Map<T,List<DTree<T>>>  nodeHeadMap = new HashMap<>();

    
    /**
     * Constructor.
     * @param contents 
     */
    public FTTree1(T contents)
    {
        super(contents);
    }
    
    /**
     * Create a root node.
     * 
     * @param <T>
     * @param root item
     * @return 
     */
    public static <T extends Comparable> FTTree1<T> initialise(T root)
    {
        DTree rootNode = new FTTree1(root,true);
        return (FTTree1)rootNode;
    }

    
    /**
     * Mutators.
     * @return 
     */
    public int getCptbCount() { return cptbCount; }
    public void incrCptbCount() { ++this.cptbCount; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public List<T> getFreqItemSup() { return freqItemSup; }
    public List<T> getFreqItemSupRev() { return itemSupRev; }
    
    /**
     * Get children of this node.
     * 
     * @return 
     */
    public List<FTTree1<T>> getChildren()
    {
        return children.stream().map(c -> (FTTree1<T>)c).collect(Collectors.toList());
    }
    
    /**
     * Set frequent item support (count)
     * @param freqItemSup 
     */
    public void setFreqItemSup( List<T> freqItemSup )
    {
        // *** Assume already sorted in desc. order (@TODO: ************ Production)
        this.freqItemSup = freqItemSup;
        
        // Now add list freq items (in desc order)
        itemSupRev = new ArrayList<>(freqItemSup);
        Collections.sort(itemSupRev,Collections.reverseOrder());
    }
    
    /**
     * Map of list of "head" nodes.
     * @return 
     */
    public Map<T,List<DTree<T>>> getNodeHeadMap() { return nodeHeadMap; }
    
    /**
     * Copy a node (not a shallow, but not a deep copy).
     * 
     * @param oldNode
     * @return 
     */
    public FTTree1<T> simpleCopy( FTTree1<T> oldNode )
    {
        FTTree1<T> copyNode = new FTTree1( oldNode.getContents() );
        copyNode.nodeCount = oldNode.nodeCount;
        
        return copyNode;
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
        insertTree(this,this,0,items, 1,1);
    }
    
    /**
     * Add nodes to tree as determined by fpGrowth() method.
     * 
     * @param items
     * @param initCount
     * @param incrBy 
     */
    private void matchAndInsertFreq( List<T> items, int initCount, int incrBy )
    {
        insertTree(this,this,0,items,initCount,incrBy);
    }

    /**
     * Recursive routine to insert a list of items into tree.  T must implement equals().
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
    private void insertTree( FTTree1<T> root, FTTree1<T> treeNode, int itemIdx, List<T> items, int initCount, int incrBy )
    {
        // Item index check
        if( itemIdx >= items.size() ) return;                           // End of list?
        // Item
        T item = items.get(itemIdx);
        
        // This will be the next node in the recursion
        FTTree1<T> nextNode = null;

        // Find children contents matching current item
        Optional<FTTree1<T>> tNode = treeNode.getOffspring().stream()    // Get the children
                .map(tn -> (FTTree1<T>)tn)                               // Map DTree to FTTree
                .filter(lc -> item.equals(lc.getContents()))            // Match predicate
                .findFirst();        

        // Does this node have any matching children contents...?
        if( tNode.isPresent() )                                         // ...Yes
        {
            // Node already exists, increment count
            nextNode = tNode.get();
//            nextNode.sumNodeCount(incrBy);
            nextNode.incrNodeCount();
            // Incr all way up path
//            for( DTree<T> node: treeNode.pathOfAllParents(nextNode) )
//            {
//                if( node != null && node.getContents() != null )
//                    System.out.println("  Path: " +node.getContents());
//            }
            System.out.println(".....> " +nextNode.getContents());
            List<DTree<T>> ldt = treeNode.pathOfAllParents(nextNode);
            List<FTTree1<T>> lft = ldt.stream().map(dt -> (FTTree1<T>)dt).collect(Collectors.toList());
            lft.forEach(ftree -> {
                if( ftree == null )
                    System.out.println("   ftree...> NULL entry!");
                else
                    System.out.println("   ...> " +ftree.getContents());
            });
        }
        else                                                            // ...No
        {
            // Ok, need to add a new node
            nextNode = (FTTree1<T>)treeNode.addChild(new FTTree1(item));
//            nextNode.setNodeCount(initCount);  // Only necessary to override default

            // Item
            T itm = nextNode.getContents();
            // New node for root, incr count
            root.incrNodeCount();
            
            // "First" node head links
            if( !nodeHeadMap.containsKey(itm) )
            { //...Whole new node
                List<DTree<T>> nodeLinks = new ArrayList<>();
                nodeLinks.add(nextNode);
                nodeHeadMap.put(itm, nodeLinks);
            }
            else
            { //...Link between nodes with same item
                nodeHeadMap.get(itm).add(nextNode);
            }
        }

        insertTree( root, nextNode, ++itemIdx, items, initCount, incrBy );
    }
    
    /**
     * Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html
     * 
     * @return 
     */
    public List<FTTree1<T>> fpGrowth()
    {
        return fpGrowth(null,null,null);
    }
    
    /**
     * Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html
     * R, P, F as defined in algo.
     * 
     * @param rTree (R)
     * @param itemSet (P)
     * @param freqItems (F)
     * @return 
     */
    public List<FTTree1<T>> fpGrowth( FTTree1<T> rTree, Set<Item> itemSet, Set<Item> freqItems )
    {
        // Frequent itemsets to be mined from tree
        List<FTTree1<T>> freqItemSets = new ArrayList<>();
        
        // Algo: Remove infrequent items from tree (R)
        // Algo: Insert subsets of tree (R) into freqItems (F)
        //... Both above handled outside of this routine (listFlattenedTree(), remove() and subsets())
        
        // Process projected FP-trees for each frequent item
        itemSupRev.forEach(i ->
        {
            // Form an FP tree (excluding i) from these paths, setting counts along the paths
            FTTree1<T> t = initialise(i);

            // Get paths
            List<List<DTree<T>>> lpd = crosslinkPathTopDown(i);
            lpd.forEach(p ->
            {
                List<T> items = new ArrayList<>();
                int pathSiz = p.size()-1;
                // Build path of items excluding i
                for( int ii=0; ii < pathSiz; ii++ )
                {
                    DTree<T> node = p.get(ii);
                    items.add(node.getContents());
                }

                // Build the FP tree projections
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
     * @param <T>
     * @param node
     * @return 
     */
    public static <T extends Comparable> List<FTTree1<T>> dfs( FTTree1<T> node )
    {
        List<FTTree1<T>> path = new ArrayList<>();
        
        node.setVisited(true);
        path.add(node);
        System.out.print(" Node: " +node.getContents());
        
        List<FTTree1<T>> ln = node.getChildren();
        ln.stream().filter(n0 -> n0!=null && !n0.isVisited()).forEach(n1 -> dfs(n1));
        
        return path;
    }
    
    /**
     * Follow path via parents.
     * 
     * @param node
     * @return 
     */
    public List<DTree<T>> traceParents( DTree<T> node )
    {
        List<DTree<T>> path = new ArrayList<>();
        
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
     * @return 
     */
    public List<List<DTree<T>>> crosslinkPathTopDown( T item )
    {
        List<List<DTree<T>>> root2leaf = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<DTree<T>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse
        lfi.forEach(p ->
        {
            List<DTree<T>> rp = traceParents(p);
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
     * @return 
     */
    public List<List<DTree<T>>> crosslinkPathBottomUp( T item )
    {
        List<List<DTree<T>>> leaf2root = new ArrayList<>();
        
        // Cross-links of node from node head map
        List<DTree<T>> lfi = nodeHeadMap.get(item);
        
        // For each cross-link path get path to root and reverse
        lfi.forEach(p ->
        {
            List<DTree<T>> rp = traceParents(p);
            leaf2root.add(rp);
        });
                
        return leaf2root;
    }
    
    /**
     * Find leaf item? Not functional atm.
     * @param tree
     * @return 
     */
    public List<Item> findLeafItems( FTTree1 tree )
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Convert streamed tree to a list.
     * 
     * @return 
     */
    public List<FTTree1<T>> listFlattenedTree()
    {
        // streamTree() from superclass
        return streamTree().map(s -> (FTTree1<T>)s).collect(Collectors.toList());
    }
    
    /**
     * Find all subsets of this set (list of tree nodes) dropping the empty set.
     * 
     * @param values
     * @return 
     */
    public Stream<List<FTTree1<T>>> subsets(List<FTTree1<T>> values)
    {
        SubSetListIterator<FTTree1<T>> ssi = new SubSetListIterator(values);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ssi, 0), false).dropWhile(t -> t.isEmpty());
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
     * @param <T> FTTree node contents
     * @param node node of tree 
     * @param appender indent char (usually spaces)
     * @param uid output if true
     */
    public static <T extends Comparable> void printTree(FTTree1<T> node, String appender, boolean uid )
    {
        System.out.println(appender + node.getContents()+ ":<" +node.getNodeCount()+ ">{" +node.getCptbCount()+ "}" +(uid ? " "+node.getUuid().toString() : " "));
        node.getChildren().stream().map(each -> (FTTree1<T>)each).forEach(each ->  printTree((FTTree1<T>)each, appender + appender, uid));
    }
    
    /**
     * Dump children of given node.
     * @param <T>
     * @param n 
     */
    public static <T extends Comparable> void dumpFromNode( FTTree1<T> n )
    {
        n.getChildren().forEach(System.out::println);
    }
    
    /**
     * Dump the node head links.
     * 
     * @param tree 
     */
    public static void dumpNodeHeadMap(FTTree1 tree)
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
    public static void dumpNodeHeadMapFull(FTTree1 tree)
    {
        System.out.println("Node Head Map (long)");
        Map<Item,List<FTTree1<Item>>> nhm = tree.getNodeHeadMap();
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
    public void dumpLeafPaths( T item )
    {
        System.out.println("Paths to item: " +item);
        List<List<DTree<T>>> lpd = crosslinkPathTopDown(item);
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
