/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 *
 * @author chrispowell
 */
public class Tree3
{
    public static void main(String[] args)
    {
        final int MINSUP = 1;
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));

        // Items (with count)
        Item itemA = new Item("a",4);
        Item itemB = new Item("b",6);
        Item itemC = new Item("c",4);
        Item itemD = new Item("d",4);
        Item itemE = new Item("e",5);
        
        // Item freq sorted by descr.
        List<Item> itemSup = Arrays.asList(new Item[]{itemB,itemE,itemA,itemC,itemD});

        List<Item> ln01 = Arrays.asList(new Item[]{itemB,itemE,itemA,itemD});
        List<Item> ln02 = Arrays.asList(new Item[]{itemB,itemE,itemC});
        List<Item> ln03 = Arrays.asList(new Item[]{itemB,itemE,itemA,itemD});
        List<Item> ln04 = Arrays.asList(new Item[]{itemB,itemE,itemA,itemC});
        List<Item> ln05 = Arrays.asList(new Item[]{itemB,itemE,itemA,itemC,itemD});
        List<Item> ln06 = Arrays.asList(new Item[]{itemB,itemC,itemD});
        
        Map<String,List<Item>> docs = new HashMap<>();
        docs.put("ln01",ln01);
        docs.put("ln02",ln02);
        docs.put("ln03",ln03);
        docs.put("ln04",ln04);
        docs.put("ln05",ln05);
        docs.put("ln06",ln06);

        Map<Item,List<List<Item>>> item2Doc = new HashMap<>();
        item2Doc.put(itemA,List.of(ln01,ln03,ln04,ln05));
        item2Doc.put(itemB,List.of(ln01,ln02,ln03,ln04,ln05,ln06));
        item2Doc.put(itemC,List.of(ln02,ln04,ln05,ln06));
        item2Doc.put(itemD,List.of(ln01,ln03,ln05,ln06));
        item2Doc.put(itemE,List.of(ln01,ln02,ln03,ln04,ln05));
        

        // Build tree
        System.out.println("1st list (b,e,a,d)...");
        tree.matchAndInsert(ln01);
        System.out.println("4th list (b,e,c)...");
        tree.matchAndInsert(ln02);
        System.out.println("1st list (b,e,a,d)...");
        tree.matchAndInsert(ln03);
        System.out.println("4th list (b,e,a,c)...");
        tree.matchAndInsert(ln04);
        System.out.println("1st list (b,e,a,c,d)...");
        tree.matchAndInsert(ln05);
        System.out.println("4th list (b,c,d)...");
        tree.matchAndInsert(ln06);
        
        // Set frequent items (ordered) list
        tree.setFreqItemSup(itemSup);

        System.out.println("\r\n===================[Tree:"+tree.getRoot().getName()+"]===============================");
        System.out.println("Note: ()s denote count");
        FTTree.printTree(tree, " ", false);
        System.out.println("========================================================");
        
//        System.out.println("\r\n==================================================");
//        tree.dumpLeafPaths(itemD);
//        System.out.println("====================================================\r\n");
        
//        System.out.println("\r\n==================================================");
//        System.out.println("Freq items (reverse)");
//        tree.getFreqItemSupRev().forEach(System.out::println);
//        System.out.println("====================================================");

        // Generate a list of freq. itemsets ('mini' trees) using FPgrowth algo
        System.out.println("\r\n----------[FP growth]----->");
        List<FTTree<Item>> lfptp = tree.fpGrowth();
        
        // FP projections
//        lfptp.forEach(fptp -> {
//            System.out.println("\r\n===================[FP tree projection: "+fptp.getRoot()+"]===============================");
//            FTTree.printTree(fptp, " ", true);
//        });
        
        // Freq. itemsets
//        lfptp.forEach(fptp -> {
//            System.out.println("\r\n\r\n\r\n===================[Freq. itemsets: "+fptp.getRoot()+"]===============================");
//            FTTree.getBranchDepth(fptp, 0);
//        });
        
//        lfptp.forEach(fptp -> {
//            System.out.println("==============================[Freq. itemsets flattened: "+fptp.getRoot()+"==============================");
//            fptp.listFlattenedTree().forEach(ft -> {
//                System.out.println("::> " +ft.getContents()+ ":" +ft.getNodeCount());
//            });
//        });
        
        /*
         * Generate doc clusters
         */
        // Clusters
        List<Cluster> clusters = new ArrayList<>();
        
        // Process FPgrowth freq. itemsets ('mini' trees)
        lfptp.forEach(fptp ->
        {
            String iName = fptp.getRoot().getName();
            
            // Generate freq. itemsets subsets (all subsets of the set of nodes in the 'mini' tree. Eg [a,b] -> [{},a,b,ab])
            System.out.println("==============================[Freq. itemsets subsets: "+iName+"]==============================");
            FTTree.printTree(fptp, " ", false);
            System.out.println(".......");
            
            // First, flatten the tree (into a set/list)
            List<FTTree<Item>> ft = fptp.listFlattenedTree();
            // Remove the root object (the freq. itemsets are itemsets of the root)
            ft.remove(0);
            // Get rid of non-frequent itemsets
            ft.removeIf(i -> i.getNodeCount()<3);
            
            // Generate all subsets (being a collection of tree nodes) of this itemset
            Stream<List<FTTree<Item>>> lfti = tree.subsets(ft);

            // Populate clusters
            List<List<Item>> lfi = new ArrayList<>();
            
            // Each subset has an initial cluster (which might get ditched)
            lfti.forEach((ll ->
            {
                System.out.println("---------------->");
                ll.forEach(fttree ->
                {
                    System.out.print(fttree.getContents().getName());
                });
                System.out.println("\r\n---------------->\r\n");
            
                // Give cluster a name
                System.out.print(iName+ ":");
                String[] clusterName = new String[]{iName};
                
                // Process each node in the subset
                List<Item> li = new ArrayList<>();
                ll.forEach(lll -> {
                    System.out.print("(" +lll.getContents().getName() +")");
                    li.add(lll.getContents());
                    clusterName[0] += lll.getContents().getName();
                });
                
                lfi.add(li);
                System.out.println("\r\nCluster name: " +clusterName[0]);
                clusters.add(new Cluster(clusterName[0],lfi));
            }));
        });

        System.out.println();
        System.out.println("==============================[Clusters]==============================");
        clusters.forEach(c -> {
            System.out.print("\r\nCluster [" +c.getName()+ "]: ");
            c.getDocs().forEach(System.out::print);
        });
        System.out.println();
    }
}

