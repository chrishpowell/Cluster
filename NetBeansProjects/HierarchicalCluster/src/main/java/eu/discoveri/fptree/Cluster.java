/*
 * Cluster of documents.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Clustering of documents.
 * @author chrispowell
 */
public class Cluster implements Comparable<Cluster>
{
    private final String        name;
    private double              score;
    private List<Doc>           docs;
    private int                 docCount;

    // List of items to determine cluster name
    private final List<Item>    nameList;
    
    // Subtree docs
    private Set<Doc>            subtreeDocs = Collections.EMPTY_SET;
    
    // A *unique* list of items to determine cluster name and position in the cluster tree
    private List<Item>          uniqueIList = Collections.EMPTY_LIST;
    
    // Set of all (initial) clusters
    private static Set<Cluster> clusters = new HashSet<>();
    
    // Cluster support numbers (for cluster scoring)
    private static Map<Cluster,Map<Item,Double>> clusterScoreSupp = new HashMap<>();
    private static Map<Cluster,Map<Item,Double>> clusterSimilaritySupp = new HashMap<>();
    
    // Cross cluster similarity score
    private static Map<Cluster,Map<Cluster,Double>> crossSimilarity = new HashMap<>();
    
    // If doc in multiple clusters, keep only highest scoring doc/cluster
    private static Map<Doc,Cluster> highScoreDocCluster = new HashMap<>();
    
    // Cluster to tree entry
    private static final Map<Cluster,ClusterTree<Cluster>> cluster2Tree = new HashMap<>();

        
    /**
     * Constructor.  Cluster name built from Item list.
     * 
     * @param nameList
     * @param name
     * @param score
     * @param docs 
     */
    public Cluster(List<Item> nameList, String name, double score, List<Doc> docs)
    {
        this.nameList = nameList;
        this.name = name;
        this.score = score;
        this.docs = docs;
        this.docCount = docs.size();
    }
    
    /**
     * Constructor.  Cluster name built from Item list.
     * 
     * @param nameList
     * @param score
     * @param docs 
     */
    public Cluster(List<Item> nameList, double score, List<Doc> docs)
    {
        StringBuilder n = new StringBuilder();
        this.nameList = nameList;
        nameList.forEach(nl -> {
            n.append(nl.getName()).append(":");
        });
        name = n.toString();
        this.score = score;
        this.docs = docs;
        this.docCount = docs.size();
    }
    
    /**
     * Constructor.  Cluster name built from Item list.
     * 
     * @param nameList
     * @param docs 
     */
    public Cluster(List<Item> nameList, List<Doc> docs)
    {
        this(nameList,0.0f,docs);
    }

    /**
     * Constructor.
     * 
     * @param name
     * @param score
     * @param docs 
     */
    public Cluster(String name, double score, List<Doc> docs)
    {
        this(new ArrayList<>(),name,score,docs);
    }
    
    /**
     * Constructor.
     * 
     * @param name
     * @param docs 
     */
    public Cluster(String name, List<Doc> docs)
    {
        this(new ArrayList<>(),name,0.0f,docs);
    }


    /**
     * Mutators.
     * @return 
     */
    public String getName() { return name; }
    public List<Doc> getDocs() { return docs; }
    public List<Doc> appendDocs( List<Doc> docs )
    {
        this.docs.addAll(docs);
        return this.docs;
    }
    public void setDocs( List<Doc> docs )
    {
        this.docs = docs;
        this.docCount = docs.size();
    }
    public int getDocCount() { return docCount; }
    
    public Set<Doc> getSubtreeDocs() { return subtreeDocs; }
    public void setSubtreeDocs( Set<Doc> subtreeDocs )
    {
        this.subtreeDocs = subtreeDocs;
    }
    
    public List<Item> getNameList(){ return nameList; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public List<Item> getUniqueIList() { return uniqueIList; }
    public void setUniqueIList() { this.uniqueIList = Item.uniqueItemList(docs); }

    /**
     * Get cluster support.
     * 
     * @return 
     */
    public static Map<Cluster, Map<Item, Double>> getClusterScoreSupp() { return clusterScoreSupp; }
    public static Map<Cluster, Map<Item, Double>> getClusterSimilaritySupp() { return clusterSimilaritySupp; }
    
    public static Map<Cluster,Map<Cluster,Double>> getCrossSimilarity() { return crossSimilarity; } 
    
    /**
     * Highest scoring doc/cluster.
     * 
     * @return 
     */
    public static Map<Doc,Cluster> getHighScoreDocCluster() { return highScoreDocCluster; }
    
    /**
     * Recursive method to build a cluster tree. Note: in finding a parent, the
     * parent cluster node has one less name list item than its child. Set
     * intersection on name lists is used to determine which node is a parent. A
     * Cluster should have a score to be able to 'choose' a parent. The method
     * uses cluster scoring to determine 'best' parent (atomic family only!).
     * Eg: parents of 'ab' are 'a' and 'b'.  If 'b' scores higher, then 'b'
     * becomes parent of 'ab'.
     * 
     * @param lc Clusters to add to tree
     * @param c Cluster tree node at any point (parent becomes child during traversal).
     * @param root Root of whole tree
     */
    public static void add2Tree(Set<Cluster> lc, ClusterTree<Cluster> c, ClusterTree<Cluster> root)
    {
        // What was parent is now child as we move up a branch
        Cluster child = c.getContents();
        
        // Input items
        List<Item> cNames = child.getNameList();
        int childSiz = cNames.size();
        
        /*
         * Determine parents
         */
        // Find parent with max score
        Optional<Cluster> op = lc.stream()
          .filter(p -> p.getNameList().size()==childSiz-1)         // Parent has one less item than child
          .filter(p -> cNames.containsAll(p.getNameList()))        // Intersect item sets
          .max(Comparator.comparingDouble(Cluster::getScore));     // Get max. parent score

        // No parent?  Then root will adopt... :-)
        if( !op.isPresent() )
        {
            // At top level, add to root (parent) if not yet done
            if( !cluster2Tree.containsKey(child) )
                cluster2Tree.put(child, c);

            // And has child previously been added to root (via a different branch)?
            if( !root.getChildren().contains(c) )
                root.addChild(c);
        }
        else
        {
            // Attach parent to child
            Cluster pt = op.get();
            if( cluster2Tree.containsKey(pt) )
            {   // Already processed parent
                ClusterTree par = cluster2Tree.get(pt);

                // Has child already been added to parent (via a different branch)?
                if( !par.getChildren().contains(c) )
                    { par.addChild(c); }

                // Next level up
                add2Tree(lc,par,root);
            }
            else
            {   // Ok, create a node for the parent and flag as processed
                ClusterTree par = new ClusterTree(pt);
                par.addChild(c);
                cluster2Tree.put(pt, par);

                // Next level up
                add2Tree(lc,par,root);
            }
        }
    }
    
    /**
     * Cluster similarity scoring.
     * 
     * @param node
     * @param appender
     * @param rootName
     * @param uid 
     */
    public static void similarityScore( ClusterTree<Cluster> node, String appender, String rootName, boolean uid )
    {
        Cluster cr = node.getContents();
        
        if( !cr.getName().equals(rootName) )
        {
            System.out.print("\r\n   Docs:");
            cr.getDocs().forEach(d -> {
                System.out.print(" "+d.getName());
            });
            System.out.print("\r\n   Unique items:");
            if( !cr.getUniqueIList().isEmpty() )
            {
                cr.getUniqueIList().forEach(i -> {
                    System.out.print(" "+i.getName());
                });
            }
            System.out.println();
        }

        // Next node in tree
        System.out.println();
        node.getChildren().stream()
                .map(each -> (ClusterTree<Cluster>)each)
                .forEach(each ->  printClusterTreeWithScoreFromNode((ClusterTree<Cluster>)each, appender + appender, rootName, uid));
    }
    
    /**
     * Cluster support. ****************************************************************************
     * 
     * @param uniqueItemList (formed, for example, from SQL query).
     * @return 
     * @throws eu.discoveri.fptree.EmptySetofClusterException 
     * @throws eu.discoveri.fptree.NoUniqueItemListException 
     */
    public static Map<Cluster, Map<Item, Double>> scoreSupport( List<Item> uniqueItemList )
            throws EmptySetofClusterException, NoUniqueItemListException
    {
        // Check we have some clusters
        if( clusters.isEmpty() )
            throw new EmptySetofClusterException("No clusters formed");
        // Check we have a unique set of items
        if( uniqueItemList.isEmpty() )
            throw new NoUniqueItemListException("No list of unique items");
        
        clusterScoreSupp =  support( clusters, uniqueItemList );
        return clusterScoreSupp;
    }
    
    /**
     * **********************************************************************************************
     * @param sClusters
     * @param uniqueItemList
     * @return
     * @throws EmptySetofClusterException
     * @throws NoUniqueItemListException 
     */
    public static Map<Cluster, Map<Item, Double>> similaritySupport( Set<Cluster> sClusters, List<Item> uniqueItemList )
            throws EmptySetofClusterException, NoUniqueItemListException
    {
        // Check we have some clusters
        if( sClusters.isEmpty() )
            throw new EmptySetofClusterException("No clusters formed");
        // Check we have a unique set of items
        if( uniqueItemList.isEmpty() )
            throw new NoUniqueItemListException("No list of unique items");
        
        Map<Cluster,Map<Item,Double>> clusterSupp = new HashMap<>();
        
        // Calc. cluster support per cluster-item
        sClusters.forEach(c ->
        {
            Map<Item,Double> csupp = Collections.EMPTY_MAP;

            // Calc. percentage of given set of docs containing an item for each item in the given list
            csupp = SupportCount.supportPercent(c.getSubtreeDocs(), uniqueItemList, Constants.MINCSUPP);
            
            // Only interested if we have a value
            if( csupp.size() > 0 )
            {
                // Update map
                clusterSupp.put(c,csupp);
            }
        });

        return clusterSupp;
    }
    
    /**
     * **************************************************************************************************
     * @param sClusters
     * @param uniqueItemList
     * @return 
     */
    public static Map<Cluster, Map<Item, Double>> support( Set<Cluster> sClusters, List<Item> uniqueItemList )
    {
        Map<Cluster,Map<Item,Double>> clusterSupp = new HashMap<>();
        
        // Calc. cluster support per cluster-item
        sClusters.forEach(c ->
        {
            Map<Item,Double> csupp = Collections.EMPTY_MAP;
            
            // Calc. percentage of given set of docs containing an item for each item in the given list
            csupp = SupportCount.supportPercent(c.getDocs(), uniqueItemList, Constants.MINCSUPP);
            
            // Only interested if we have a value
            if( csupp.size() > 0 )
            {
                // Update map
                clusterSupp.put(c,csupp);
            }
        });

        return clusterSupp;
    }
    
    /**
     * Calculate cluster support (percentage) from count. The cluster support of
     * an item (for this cluster) is the percentage of docs.in this cluster
     * that contain that item.
     * 
     * @param csCount
     * @param minCSupp Minimum percentage for support
     * @return 
     * @throws eu.discoveri.fptree.InvalidSupportPercentageException 
     */
    public Map<Item,Double> supportPercent( Map<Item,Integer> csCount, double minCSupp )
            throws InvalidSupportPercentageException
    {
        Map<Item,Double> csPercent = new HashMap<>();
        if( minCSupp <= 0.d || minCSupp >100.d )
            throw new InvalidSupportPercentageException("Input %: " +minCSupp);
        
        csCount.forEach((c,p) -> {
            double pctCount = p/(float)docCount;
            if( pctCount >= minCSupp )
                csPercent.put(c, pctCount);
        });
        
        return csPercent;
    }
    
    /**
     * Generate clusters from all subsets of set of items in list of item trees.
     * Will generally produce a tree sparsely populated with docs (which will be
     * pruned).
     * 
     * @param tree
     * @param lfptp
     * @param item2Docs a mapping of an item to the doc in which it appears
     * @return 
     */
    public static Set<Cluster> clustersFromFreqItems( FTTree<Item> tree, List<FTTree<Item>> lfptp, Map<Item,List<Doc>> item2Docs )
    {
        // Process FPgrowth freq. itemsets (list of 'mini' trees)
        lfptp.forEach(fptp ->
        {
            /*
             * Generate freq. itemsets subsets (all subsets of the set of nodes
             * in the 'mini' tree. Eg [a,b] -> [{},a,b,ab] (but ignoring empty set)
             */
            // First, flatten the tree (into a set/list)
            List<FTTree<Item>> ft = fptp.listFlattenedTree();

            // Get rid of non-frequent itemsets (node count)
            ft.removeIf(i -> i.getNodeCount() <= Constants.MINSUP);

            // Subsets formed (excluding empty sets):
            Stream<List<FTTree<Item>>> lfti = tree.subsets(ft);
            
            // Each subset (with a global support greater than given value) forms a cluster
            // Eg: [[f1],[f1,f2],[f2],[f1,f3]...]
            lfti.forEach(lss ->
            {
                // List of docs containing items of tree
                List<Doc> docsList = new ArrayList<>();
                // Cluster name (from items)
                List<Item> li = new ArrayList<>();
                
                // For each item in a subset (Eg: each of [f1,f3])
                // ...Multiple items
                if( lss.size() > 1 )
                {
                    // Documents in idx 0
                    List<Doc> ld = item2Docs.get(lss.get(0).getContents());
                    
                    // Intersect with all other subsets
                    lss.stream().forEach(fttree ->
                    {
                        // Get the freq.item contents
                        Item itm = (Item)fttree.getContents();
                        
                        // Form cluster name @TODO: ***************************** Change this?
                        li.add(itm);
                        
                        // Get all the docs for this freq.item (cluster)
                        List<Doc> newld = item2Docs.get(itm);
                        // Intersect 
                        ld.retainAll(newld);
                    });
                    
                    docsList.addAll(ld);
                }
                else
                    // ...Single item
                    {
                        // (Single, freq.) item for this itemset
                        Item itm = lss.get(0).getContents();
                        
                        // Build a cluster name (from item names) @TODO: ********************** Change this?
                        li.add(itm);

                        // Get the list of docs associated with this subset
                        List<Doc> ld = item2Docs.get(itm);

                        // Get unique set of docs for this (single) subset 
                        ld.forEach(_item -> {
                            ld.stream()
                                    .filter(d -> (!docsList.contains(d)))
                                    .forEachOrdered(d -> {docsList.add(d);});
                        });
                    }

                // Add docs to this cluster ("best initial cluster")
                clusters.add(new Cluster(li,docsList));
            });
        });
        
        return clusters;
    }

    /**
     * Create set of clusters (if not from freq. items).
     * 
     * @param setClusters 
     */
    public static void createClusters( Set<Cluster> setClusters )
    {
        clusters = setClusters;
    }
    
   /**
     * Form the set of all Clusters on a subtree.
     * 
     * @param dtc
     * @return 
     */
    public static Set<Cluster> subtreeClusters(ClusterTree<Cluster> ct)
    {
        return subtreeClusters(ct,new HashSet<>());
    }
        
    /**
     * Form the set of all Clusters on a subtree.
     * 
     * @param dtc
     * @return 
     */
    public static Set<Cluster> subtreeClusters(ClusterTree<Cluster> ct, Set<Cluster> sc)
    {
//        Set<Cluster> sc = dtc.getChildren().stream().map(DTree::getContents).collect(Collectors.toSet());
        
        // Trawl through all children
        ct.streamChildren().forEach(dtree ->
        {
            subtreeClusters((ClusterTree)dtree,sc);
            sc.add(dtree.getContents());
        });
        
        return sc;
    }
    
    /**
     * Cluster similarity function (on default clusters).
     * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf (4.2)
     *
     * @param globalSupp 
     * @return 
     */
    public static Map<Cluster,Map<Cluster,Double>> similarity( Map<Item,Double> globalSupp )
    {
        // Score each cluster/item (NB: Functional ops fail here)
        for( Cluster ci: clusters )
        {
            Map<Cluster,Double> simMap = new HashMap<>();
                            
            // clusterSupp
            if( clusterScoreSupp.containsKey(ci) )
            {
                Map<Item,Double> itemCS = clusterScoreSupp.get(ci); //******************************************************************************
                if( !itemCS.isEmpty() )
                {
                    for( Cluster cj: clusters )
                    {
                        // No need to compare same cluster
                        if( cj.equals(ci) ) continue;
                        // Subtree docs must exist
                        Set<Doc> sd = cj.getSubtreeDocs();
                        if( sd.size() < 1 ) continue;

                        // Doc score
                        Map<Doc,Double> mdd = new HashMap<>();

                        // sum(n(x)) and sum(n(xdash))
                        long sumnx = 0l, sumnxdash = 0l;

                        // Docs in cluster i
                        for( Doc docj: sd )
                        {
                            double score = 0.d;

                            // Items in docj
                            for( Item i: docj.getLi() )
                            {
                                // NB: getCount() below is item freq in feature vec.
                                if( itemCS.containsKey(i) )
                                {
                                    // item i is global and cluster frequent [cluster_support * n(x)]                        
                                    score += itemCS.get(i) * i.getCount();
                                    sumnx += i.getCount();
                                }
                                else
                                    {
                                        // item i is not cluster frequent [global_support * n(xdash)]
                                        score -= globalSupp.get(i) * i.getCount();
                                        sumnxdash += i.getCount();
                                    }
                            }

                            mdd.put(docj,score);
                        }

                        // Aggregate scores
                        double s = mdd.values().stream().mapToDouble(v -> v).sum();
                        // Algo 4.2 creates range [0,2]
                        s = 1.d + (s/sd.size())/(sumnx + sumnxdash);

                        // Store scores
                        if( !Double.isNaN(s) )
                            { simMap.put(cj, s); }
                    }
                }
                else
                {
                    System.out.println("!!Empty score map: " +ci.getName());
                    continue;
                }
            }
//            else
//                System.out.println("!! No score supp. for: " +ci);
            
            crossSimilarity.put(ci, simMap);
        }

        return crossSimilarity;
    }

    /**
     * Cluster similarity function (on given clusters).
     * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf (4.2)
     *
     * @param fCluster the given cluster tree
     * @param globalSupp percentage of (tot.) docs. in given set containing itemset
     * @return 
     */
    public static Map<Cluster,Map<Cluster,Double>> similarity( ClusterTree<Cluster> fCluster, Map<Item,Double> globalSupp )
    {
        Set<Cluster> fClusters = subtreeClusters(fCluster);
//        System.out.println(" >> Processing subclusters");
//        fClusters.forEach(c -> {
//            System.out.println("  " +c.getName());
//        });
        
        // Score each cluster/item (NB: Functional ops fail here)
        for( Cluster ci: fClusters )
        {
            Map<Cluster,Double> simMap = new HashMap<>();
                            
            // clusterSupp
            if( clusterScoreSupp.containsKey(ci) )
            {
                Map<Item,Double> itemCS = clusterScoreSupp.get(ci);
                if( !itemCS.isEmpty() )
                {
                    for( Cluster cj: fClusters )
                    {
                        // No need to compare same cluster
                        if( cj.equals(ci) ) continue;
                        // Only subtree docs are processed
                        if( cj.getSubtreeDocs().size() < 1 ) continue;

                        // Doc score
                        Map<Doc,Double> mdd = new HashMap<>();

                        // sum(n(x)) and sum(n(xdash))
                        long sumnx = 0l, sumnxdash = 0l;

                        // Docs in cluster i
                        for( Doc docj: cj.getSubtreeDocs() )
                        {
                            double score = 0.d;

                            // Items in docj
                            for( Item i: docj.getLi() )
                            {
                                // NB: getCount() below is item freq in feature vec.
                                if( itemCS.containsKey(i) )
                                {
                                    // item i is global and cluster frequent [cluster_support * n(x)]                        
                                    score += itemCS.get(i) * i.getCount();
                                    sumnx += i.getCount();
                                }
                                else
                                    {
                                        // item i is not cluster frequent [global_support * n(xdash)]
                                        score -= globalSupp.get(i) * i.getCount();
                                        sumnxdash += i.getCount();
                                    }
                            }

                            mdd.put(docj,score);
                        }

                        // Store scores
                        double s = mdd.values().stream().mapToDouble(v -> v).sum();
                        s = 1.d + (s/cj.getSubtreeDocs().size())/(sumnx + sumnxdash);

                        if( !Double.isNaN(s) )
                            { simMap.put(cj, s); }
                    }
                }
                else
                {
                    System.out.println("!!Empty score map: " +ci.getName());
                    continue;
                }
            }
//            else
//                System.out.println("!! No score supp. for: " +ci);
            
            crossSimilarity.put(ci, simMap);
        }

        return crossSimilarity;
    }
    
    /**
     * Inter-cluster similarities.  This is the geometric mean of the normalized
     * scores Ca to Cb and Cb to Ca: sqrt[Sim(Ca/Cb)*Sim(Cb/Ca)]
     * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf (4.3)
     * 
     * @param similarities
     * @return 
     */
    public static Map<Cluster,Map<Cluster,Double>> interSim( Map<Cluster,Map<Cluster,Double>> similarities )
    {
        Map<Cluster,Map<Cluster,Double>> interSims = new HashMap<>();
        
        similarities.forEach((ci,mcd) ->
        { // Outer map
            if( !mcd.isEmpty() )
            {
                Map<Cluster,Double> simscMap = new HashMap<>();
                
                mcd.forEach((cj,simsc) ->
                { // Inner map [ci,[cj,score]]
                    // Is there an outer version of the inner map?
                    if( similarities.containsKey(cj) )
                    {   
                        if( similarities.get(cj).containsKey(ci) )
                        {
                            
                            simscMap.put(cj,Math.sqrt(similarities.get(cj).get(ci) * simsc));
//                            System.out.println("[ci]" +ci.getName()+ ">[cj]" +cj.getName());
//                            System.out.println("  .> " +Math.sqrt(similarities.get(cj).get(ci) * simsc));
                        }
                    }
                });
                
                interSims.put(ci,simscMap);
            }
        });
        
        return interSims;
    }

    /**
     * Score function.
     * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf (3.1)
     * 
     * Score = Sum[freq(x)*clusterSupp(x)] - Sum[freq(xdash)-globalSupp(xdash)]
     * where: x is global freq item and cluster freq item (in Ci)
     *        xdash is global freq item but not cluster freq item (in Ci)
     *        clusterSupp(x) is fraction of item of all items in a cluster
     * The first term of the score function rewards a cluster Ci if a global
     * frequent item x in docj is cluster frequent in Ci.
     *
     * In order to capture the importance (weight) of item x in different
     * clusters, multiply the frequency of x in docj by its cluster support
     * in Ci. The second term of the function "penalizes" cluster Ci if a global
     * frequent item x in docj is not cluster frequent in Ci. The frequency
     * of x is multiplied by its global support which can be viewed as the
     * importance of x in the entire document set.
     * 
     * @param clusters
     * @param globalSupp
     * @return 
     */
    public static Map<Cluster,Map<Doc,Double>> score( Set<Cluster> clusters, Map<Item,Double> globalSupp )
    {
        // Score per doc per cluster
        Map<Cluster,Map<Doc,Double>> clusterScore = new HashMap<>();

        // Score each cluster/item (NB: Functional ops fail here)
        for( Cluster ci: clusters )
        {
            Map<Doc,Double> mdd = new HashMap<>();

            // Docs in cluster
            // Ci is "good" for a docj if there are many global freq. items in
            // docj that appear in many docs of Ci (hence only need to consider
            // docs in Ci, not all docs).
            for( Doc docj: ci.getDocs() )
            {
                double[] score = {0.d};                                         // (Array to avoid 'final' err)

                // Items in docj
                for( Item i: docj.getLi() )
                {
                    // clusterSupp
                    Map<Item,Double> itemCS = clusterScoreSupp.get(ci); //******************************************************************************

                    // NB: getCount() below is item freq in feature vec.
                    if( itemCS.containsKey(i) )
                    {
                        // item i is global and cluster frequent [cluster_support * n(x)]                        
                        score[0] += itemCS.get(i) * i.getCount();       
                    }
                    else
                        {
                            // item i is not cluster frequent [global_support * n(xdash)]
                            score[0] -= globalSupp.get(i) * i.getCount();
                        }
                }

                mdd.put(docj,score[0]);
            }
            
            double s = mdd.values().stream().mapToDouble(v -> v).sum();
            ci.setScore(s);
            clusterScore.put(ci, mdd);
        }
        
        return clusterScore;
    }
    
    /**
     * Calculate highest scoring doc-cluster.
     * 
     * @param lds
     * @param cScore
     * @return 
     */
    public static Map<Doc,Cluster> highScoreDocCluster( List<Doc> lds, Map<Cluster,Map<Doc,Double>> cScore )
    {
        // Find highest scoring doc in each cluster
        lds.forEach(d ->
        {
            double[] vMax = {Double.MIN_VALUE};
            
            cScore.forEach((cs,ld) ->
            {
                if( ld.containsKey(d) )
                {
                    double dScore = ld.get(d) ;
                    if( dScore > vMax[0] )
                    {
                        vMax[0] = dScore;
                        highScoreDocCluster.put(d,cs);
                    }
                }
            });
        });
                
        return highScoreDocCluster;
    }
    
    /**
     * Cull low scoring (duplicate) docs from clusters.
     * 
     * @return 
     */
    public static Set<Cluster> cullLowScoreDocs()
    {
        // Remove previous lower scored cluster:doc
        clusters.forEach(c -> {
            for( Iterator<Doc> iter = c.getDocs().iterator(); iter.hasNext(); )
            {
                Doc d = iter.next();
                // Cluster contains doc but high score doc not in this cluster
                if( highScoreDocCluster.containsKey(d) &&
                    !highScoreDocCluster.get(d).equals(c) )
                {
                    iter.remove();
                }
            }
        });
        
        return clusters;
    }

    /**
     * Print Cluster tree from given node.
     * 
     * @param node
     * @param appender
     * @param rootName
     * @param uid 
     */
    public static void printClusterTreeWithScoreFromNode( ClusterTree<Cluster> node, String appender, String rootName, boolean uid )
    {
        // Get the contents (items) from this (cluster) node
        Cluster cr = node.getContents();

        // Get the docs and unique items for this node
        System.out.print(appender +cr.getName()+ "(" +cr.getScore()+ ")>");
        if( !cr.getName().equals(rootName) )
        {
            System.out.print("\r\n   Docs:");
            cr.getDocs().forEach(d -> {
                System.out.print(" "+d.getName());
            });
            System.out.print("\r\n   Unique items:");
            if( !cr.getUniqueIList().isEmpty() )
            {
                cr.getUniqueIList().forEach(i -> {
                    System.out.print(" "+i.getName());
                });
            }
            System.out.println();
        }

        // Next node in tree
        System.out.println();
        node.getChildren().stream()
                .map(each -> (ClusterTree<Cluster>)each)
                .forEach(each ->  printClusterTreeWithScoreFromNode((ClusterTree<Cluster>)each, appender + appender, rootName, uid));
    }

    /**
     * Sort by largest number items per cluster (descending)
     * @param c
     */
    @Override
    public int compareTo(Cluster c)
    {
        return c.getNameList().size() - this.getNameList().size();
    }

    /**
     * To string
     * @return 
     */
    @Override
    public String toString()
    {
        return name;
    }
}
