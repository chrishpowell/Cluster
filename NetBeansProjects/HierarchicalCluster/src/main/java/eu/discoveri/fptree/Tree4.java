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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * See following:
 * 1. Hierarchical Document Clustering Using Frequent Itemsets: https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf
 * 2. "http://berlin.csie.ntnu.edu.tw/PastCourses/2004S-MachineLearningandDataMining/lectures/MLDM2004S_Paper-Hierarchical Document Clustering Using Frequent Itemsets.pdf"
 * 3. Fast Algorithms for Mining Association Rules: http://www.vldb.org/conf/1994/P487.PDF
 * 4. Chap 8 Data Mining: Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html.
 * 
 * Some definitions:
 * Itemsets
 * --------
 * A "document vector" contains the count of all items (words) in a document. Eg: doc1 contains {apple:2, banana:1, cherry:1}, doc2 contains {banana:1,kumquat:1}.
 * 
 * An "itemset" is a set of items (words).  Eg: {apple},{kumquat,xigua},{apple,banana,cherry}.
 * 
 * A "feature vector" is a reduced document vector where items not reaching a "global minimum support" (count/percentage) are removed hence forming a global frequent itemset.  
 * 
 * A "global frequent itemset" refers to a set of item (words) that appear together in more than a user-specified fraction of the document set (minimum global support).
 * Eg: {apple},{banana,cherry,damson} appear in more than 60% of documents of the document set, {kumquat},{xigua,zucchini} appear in less than 60%.
 *
 * A "global frequent item" refers to an item that belongs to some global frequent itemset, eg: banana in {banana,cherry,damson}.
 *
 * The "global support" of an itemset is the percentage of documents of the document set containing that itemset. Eg: {xigua,zucchini} has global support of 2.5%.
 *
 * Clusters
 * --------
 * A global frequent item is "cluster frequent" in a cluster Ci if the item is contained in some minimum fraction of documents in Ci.
 *
 * The "cluster support" of an item in Ci is the percentage of the documents in cluster Ci that contain the item.
 * 
 * Process
 * -------
 * 1. Access document set and unique list of items (words)
 * 2. Construct initial clusters (cluster tree) using global frequent itemsets
 * 3. Adjust clusters using cluster scoring function
 * 4. Form final cluster tree:
 *     a. Cluster similarity scoring
 *     b. Tree (child) pruning
 *     c. Sibling merge
 * 
 * @author chrispowell
 */
public class Tree4
{
    /**
     * Recursive routine to find all docs in a whole subtree of given node.
     * 
     * @param node
     * @return 
     */
    public static Set<Doc> subtreeDocs(ClusterTree<Cluster> node)
    {
        return subtreeDocs(node,Collections.EMPTY_SET);
    }
    
    /**
     * Recursive routine to find all docs in a whole subtree (with set of docs
     * being appended along branch).
     * 
     * @param node
     * @param docs
     * @return 
     */
    private static Set<Doc> subtreeDocs(ClusterTree<Cluster> node, Set<Doc> docs )
    {
        // Get accumulated docs at this level
        Set<Doc> sd = node.getContents().getDocs().stream().collect(Collectors.toSet());

        // Trawl through all children
        for( DTree<Cluster> dtree: node.getChildren() )
        {
            docs = subtreeDocs((ClusterTree<Cluster>)dtree, sd);
            
            // Add any extra docs to parent set
            sd.addAll(docs);
        }
        
        // At leaf, zoom back up
        return sd;
    }
        
    /**
     * M A I N
     * =======
     * @param args 
     * @throws eu.discoveri.fptree.NoGlobalSuppItemsException 
     * @throws eu.discoveri.fptree.EmptySetofClusterException 
     * @throws eu.discoveri.fptree.NoUniqueItemListException 
     */
    public static void main(String[] args)
            throws NoGlobalSuppItemsException, EmptySetofClusterException, NoUniqueItemListException, InvalidSupportPercentageException
    {
        /*
         * Setup "database" process
         * ------------------------
         */
        //----------------------[SQL based]-------------------------------------
        // Items (with count)
        Item itemFlow = new Item("flow");
        Item itemForm = new Item("form");
        Item itemLayer = new Item("layer");
        Item itemPatient = new Item("patient");
        Item itemResult = new Item("result");
        Item itemTreat = new Item("treat");

        // Documents
        Doc cisi01 = new Doc("cisi01",Arrays.asList(new Item[]{itemForm}));
        Doc cran01 = new Doc("cran01",Arrays.asList(new Item[]{itemLayer,itemFlow,itemForm}));
        Doc cran02 = new Doc("cran02",Arrays.asList(new Item[]{new Item("flow",2),itemLayer}));
        Doc cran03 = new Doc("cran03",Arrays.asList(new Item[]{new Item("result",3),new Item("layer",2),new Item("flow",2),itemForm}));
        Doc cran04 = new Doc("cran04",Arrays.asList(new Item[]{new Item("layer",3),new Item("flow",2)}));
        Doc cran05 = new Doc("cran05",Arrays.asList(new Item[]{new Item("layer",2),itemFlow}));
        Doc med01 = new Doc("med01",Arrays.asList(new Item[]{new Item("patient",8),itemResult,new Item("treat",2)}));
        Doc med02 = new Doc("med02",Arrays.asList(new Item[]{new Item("patient",4),new Item("result",3),itemTreat,itemForm}));
        Doc med04 = new Doc("med04",Arrays.asList(new Item[]{new Item("patient",6),new Item("result",3),new Item("treat",3)}));
        Doc med03 = new Doc("med03",Arrays.asList(new Item[]{new Item("patient",3),new Item("treat",2)}));
        Doc med05 = new Doc("med05",Arrays.asList(new Item[]{new Item("patient",4),itemForm}));
        Doc med06 = new Doc("med06",Arrays.asList(new Item[]{new Item("patient",9),itemResult,itemTreat}));
        
        List<Doc> lds= Arrays.asList(cisi01,cran01,cran02,cran03,cran04,cran05,med01,med02,med03,med04,med05,med06);
        
        // Total item count
        long totItemCount = 0;
        for( Doc d: lds )
        {
            for( Item i: d.getLi() )
            {
                totItemCount += i.getCount();
            }
        }
        
        // In which docs does an item appear (sometimes known as transaction db)? (SQL function)
        Map<Item,List<Doc>> item2Docs = new HashMap<>();
        List<Doc> l01 = new ArrayList<>();  // flow
        l01.add(cran01); l01.add(cran02); l01.add(cran03); l01.add(cran04); l01.add(cran05); 
        item2Docs.put(itemFlow, l01);
        
        List<Doc> l02 = new ArrayList<>();  // form
        l02.add(cisi01); l02.add(cran01); l02.add(cran03); l02.add(med02); l02.add(med05); 
        item2Docs.put(itemForm, l02);
        
        List<Doc> l03 = new ArrayList<>(); // layer
        l03.add(cran01); l03.add(cran02); l03.add(cran03); l03.add(cran04); l03.add(cran05); 
        item2Docs.put(itemLayer, l03);
        
        List<Doc> l04 = new ArrayList<>(); // patient
        l04.add(med01); l04.add(med02); l04.add(med03); l04.add(med04); l04.add(med05); l04.add(med06); 
        item2Docs.put(itemPatient, l04);
        
        List<Doc> l05 = new ArrayList<>();  // result
        l05.add(cran03); l05.add(med01); l05.add(med02); l05.add(med04); l05.add(med06); 
        item2Docs.put(itemResult, l05);
        
        List<Doc> l06 = new ArrayList<>(); // treat
        l06.add(med01); l06.add(med02); l06.add(med03); l06.add(med04); l06.add(med06); 
        item2Docs.put(itemTreat, l06);
        
        
        // Sort(?) items by frequency (SQL function)
        List<Item> uniqueItemList = new ArrayList<>(item2Docs.keySet());
        
        // Set global support count for each item (SQL function)
// OK to here
//        uniqueItemList.forEach(lis -> {
//            System.out.println(" ==> Item: " +lis+ ", size: " +item2Docs.get(lis).size());
//            lis.setGlobalSupport(item2Docs.get(lis).size());
//        });
        //----------------------[SQL based]-------------------------------------

        
        /*
         * Process
         * -------
         */
        // Init
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));
        System.out.println("\r\nA1. Form initial tree ("+tree.getRoot().getName()+")...");
        // @TODO: Set frequent items sort (SQL function??). Assumes uniqueItemList
        //    is already in desc. [index] order (necessary?)
        System.out.println("  A11. Set freq. item support (count)");
        tree.setFreqItemSup(uniqueItemList);
        
        // Global Support (% docs in which item(set) occurs)
        System.out.println("  A12. Global support calc. (% docs in which item(set) occurs) where min. global support: " +Constants.MINGSUP);
        // Global support: per Item
        Map<Item,Double> globalSupp = SupportCount.globalSupport(lds, uniqueItemList);
        // No items are global?
        if( globalSupp.isEmpty() )
            throw new NoGlobalSuppItemsException("No global items above min. support level: " +Constants.MINGSUP);
        
// OK to here
//        System.out.println("\r\nGlobal support (item):");
//        globalSupp.forEach((item,d) -> {
//            System.out.println("" +item+ ", supp: " +d);
//        });
//        System.out.println();
        
        System.out.println("  A13. Build document tree");
        // ...Build document tree
        lds.forEach(doc -> {
//            doc.dumpDoc();  // OK to here
            tree.matchAndInsert(doc.getLi());
        });
        System.out.println();

        // Show tree
        System.out.println("Show tree");
        System.out.println("Note: Contents/item then: () denotes item total count, [] item global support score, <> parent+children count, {} conditional pattern base count");
        FTTree.printTree(tree, " ", false);
        System.out.println("--------------------------------------------------");
// OK to here

        /*
         * Frequent Pattern growth
         * -----------------------
         * Fast Algorithms for Mining Association Rules: http://www.vldb.org/conf/1994/P487.PDF
         * Algorithm 8.5 in https://dataminingbook.info/book_html/chap8/book-watermark.html
         */
        System.out.println("\r\nB1. Determine frequent pattern growth...");
        List<FTTree<Item>> lfptp = tree.fpGrowth();
//        
//        lfptp.forEach(fptp -> {
//            System.out.println("\r\n===================[FP tree projection: "+fptp.getRoot()+"]===============================");
//            FTTree.printTree(fptp, " ", true);
//        });
//        System.out.println("");

        /*
         * Form clusters
         * -------------
         * Needs current tree, freq.pattern growth list amd item to doc map
         * Note: Cluster name = itemset, eg: [item1:item3]
         */
        System.out.println("C1. Form initial clusters...");
        Set<Cluster> clusters = Cluster.clustersFromFreqItems(tree,lfptp,item2Docs);
        System.out.println("  Num. initial clusters: " +clusters.size());

//        clusters.forEach(c -> {
//            System.out.print("\r\nCluster [" +c.getName()+ "] ");
//            System.out.println("   Doc count: " +c.getDocs().size());
//            c.getDocs().forEach(d -> System.out.print(" "+d.getName()));
//        });
//        System.out.println();

        /*
         * Adjust clusters with scoring function
         * -------------------------------------
         * Intuitively, a cluster Ci is good for a document docj if there are
         * many global frequent items in docj that appear in many documents in Ci
         */
        System.out.println("D1. Determine cluster scoring...");
        System.out.println("  D11. Cluster support calc. where min. cluster support: " +Constants.MINCSUPP);

        // Cluster support: per Item per Cluster
        Map<Cluster, Map<Item, Double>> clusterSupp = Cluster.scoreSupport(uniqueItemList);

//        System.out.println("Cluster support> ");
//        clusterSupp.forEach((c,is) -> {
//            System.out.println("++> Cluster: " +c.getName());
//            List<Doc> ld = c.getDocs();
//            ld.forEach(d -> System.out.print(" "+d.getName()));
//            System.out.println();
//            is.forEach((i,s) -> {
//                System.out.println("  Item(set) : " +i.getName()+ ", freq: " +String.format("%.2f%%",s*100));
//            });
//        });
//
//        // Global Support (% docs in which item(set) occurs)
//        System.out.println("  D12. Global support calc. (% docs in which item(set) occurs) where min. global support: " +Constants.MINGSUP);
//        
//        // Global support: per Item
//        Map<Item,Double> globalSupp = SupportCount.globalSupport(lds, uniqueItemList);
//        // No items are global
//        if( globalSupp.isEmpty() )
//            throw new NoGlobalSuppItemsException("No global items above min. support level: " +Constants.MINGSUP);

        
//        System.out.println("\r\nGlobal support item(set) %:");
//        globalSupp.forEach((c,g) -> {
//            System.out.println(" " +c.getName()+ ": " +g);
//        });
//        System.out.println();
        
        /*
         * Score function
         * --------------
         */
        System.out.println("  D12. Forming cluster scores");
        
        // Score: per Doc per Cluster ("goodness" of a cluster Ci for a document docj)
        Map<Cluster,Map<Doc,Double>> clusterScore = Cluster.score(clusters,globalSupp);

        
        /*
         * Adjust cluster doc entries
         * --------------------------
         * @TODO: What if two clusters have same documents (which will have same scores)?
         */
        System.out.println("  D13. Now adjusting cluster scores...");
        
        System.out.println("    Keeping highest scoring doc-cluster combo only");
        // If doc in multiple clusters, keep only highest scoring doc/cluster
        Map<Doc,Cluster> highScoreDocCluster = Cluster.highScoreDocCluster(lds,clusterScore);
        
//        System.out.println("\r\n----------------[Doc:Cluster]--------------------------------");
//        highScoreCluster.forEach((d,c) -> {
//            System.out.println("Doc: " +d.getName()+ ":" +c.getName());
//        });

        System.out.println("    Cull low scoring (duplicate) docs");
        // Remove empty clusters etc.
        clusters = Cluster.cullLowScoreDocs();

//        System.out.println("\r\nReduced clusters:");
//        clusters.forEach(c -> {
//            if( !c.getDocs().isEmpty() )
//            {
//                System.out.println("Cluster: " +c.getName());
//                c.getDocs().forEach(d -> {
//                    System.out.println(" " +d.getName());
//                });
//            }
//        });

        /*
         * Final cluster tree construction
         * -------------------------------
         */
        System.out.println("\r\nE1. Final cluster tree build from (initial) clusters...");

        /*
         * Create (final?) tree 
         */
        // Root
        ClusterTree<Cluster> fc = ClusterTree.initialiseTree(new Cluster("root",new ArrayList<>()));
        Map<Cluster, Map<Item, Double>> clusterSimSupp = new HashMap<>();
        
        // Build (final?) tree
        for( Cluster c: clusters )
        {
            if( !c.getDocs().isEmpty() )
            {
                // Determine unique item list of this cluster
                c.setUniqueIList();
                // Create branch of tree
                Cluster.add2Tree(clusters,new ClusterTree<>(c),fc);
            }
        }

        System.out.println("----------------------");
        Cluster.printClusterTreeWithScoreFromNode(fc, " ", "root", false);
        System.out.println("----------------------");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        // fc: Final Cluster
        ClusterTree.dumpAllSubtrees(fc, " ");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        
        /*
         * Cluster similarity scoring
         * --------------------------
         * https://www2.cs.sfu.ca/~ester/papers/FWE03Camera.pdf (4.2)
         * Similarity between Ci and Cj:
         *   ([Score of doc set (doc(Cj), being agglomeration of all docs along
         *    subtree of Cj) in Ci] / [Sum(n(x)) +Sum(n(xdash))]) + 1
         *
         * where:
         *  . the nominator measures the "goodness" of a cluster Ci for the set
         *    of docs in the subtree cluster Cj [see Cluster.score()]
         *  . the denominator: x represents a global freq. item in doc(Cj) that
         *    is also cluster freq. in Ci; xdash represents a global freq. item
         *    in doc(Cj) that is NOT cluster freq. in Ci.
         */
        System.out.println(".....................");
        System.out.println("F1. Cluster similarity support:");
        
        System.out.println("  F12. Form subtree (aggregated) docs");
        List<DTree<Cluster>> ldc = fc.listAllChildren();
        for( DTree<Cluster> dc: ldc )
        {
            // Cluster subtree doc set
            Set<Doc> sd = subtreeDocs((ClusterTree)dc);

            // Set of docs in subtree
            dc.getContents().setSubtreeDocs(sd);
        }

        System.out.println("  F13. Calculate cluster similarity scores");
        Map<Cluster,Map<Cluster,Double>> similarities = Cluster.similarity(fc,globalSupp);

        System.out.println("  F14. Calculate intercluster (Ca to/from Cb) scores");
        Map<Cluster,Map<Cluster,Double>> interSims = Cluster.interSim(similarities);
        interSims.forEach((ci,mcd) -> {
            mcd.forEach((cj,score) -> {
                System.out.println("[ci]" +ci.getName()+ ">[cj]" +cj.getName()+ ", score: " +score);
            });
        });
                
        /*
         * Tree pruning (via cluster similarity): Child pruning and sibling merge
         * ----------------------------------------------------------------------
         */
        System.out.println("\r\n  F15. Child pruning (leaf nodes to be processed where parent is not root):");
        Map<ClusterTree<Cluster>,ClusterTree<Cluster>> lc = ClusterTree.findAllLeavesParentNotRoot(fc);
        lc.forEach((leaf,parent) -> {
            System.out.println("  "+leaf.getContents().getName()+ " [parent]: " +parent.getContents().getName());
            if( interSims.get(parent.getContents()).get(leaf.getContents()) > 1.d )
            {
                System.out.println("  Remove: " +leaf.getContents().getName());
                // Remove node
                fc.delNode(leaf);

                // Remove all interSims with matching value to leaf
                interSims.remove(leaf.getContents());
                interSims.values().forEach(lf ->
                {
                    lf.remove(leaf.getContents());
                });
            }
        });

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        // fc: Final Cluster
        ClusterTree.dumpAllSubtrees(fc, " ");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");

        System.out.println("\r\n  F16. Sibling merge:");
        List<DTree<Cluster>> rootChildren = fc.getChildren();
        rootChildren.forEach(dtree -> {
            Cluster level1 = dtree.getContents();
            System.out.println("  Level 1 node: " +level1.getName());

            for( Map.Entry<Cluster,Double> sibling: interSims.get(level1).entrySet() )
            {
                if( sibling.getValue() > 1.d )
                    System.out.println("   Merge: " +level1.getName()+ " with: " +sibling.getKey().getName());
            }
        });
        System.out.println("vvvvvvvvv");
        
//        fc.streamChildren().forEach(subtree -> {
//            Set<Cluster> sc = subtree.getChildren().stream().map(DTree::getContents).collect(Collectors.toSet());
//            sc.add(subtree.getContents());
//            System.out.println("" +subtree.getContents().getName()+ ":Clusters> " +sc);
//            // Cluster subtree doc set
//            Set<Doc> sd = subtreeDocs((ClusterTree)subtree);
//            System.out.println("" +subtree.getContents().getName()+ ":Docs> " +sd);
//            
////            try
////            {
////                clusterSimSupp = Cluster.clusterSimilaritySupport(sc, uniqueItemList);
////            }
////            catch( Exception ex )
////            {
////                System.out.println("!!" +ex.getMessage());
////            }
//        });

        
//        getChildren().stream().flatMap(DTree::streamTree);
        System.out.println(".....................");


        
        /*
         * Final cluster
         */
        System.out.println("Final cluster (TBD)...");
        
        System.out.println("--------------------------------------------------");
    }
}
