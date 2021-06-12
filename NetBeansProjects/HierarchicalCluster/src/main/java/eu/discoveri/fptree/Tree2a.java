/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import static eu.discoveri.fptree.FTTree.dumpNodeHeadMapFull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author chrispowell
 */
public class Tree2a
{
    /**
     * Brute force freq. items
     * @param docs
     * @param minSup
     * @return 
     */
//    public Set<Set<Item>> bruteForce(List<Doc> docs, double minSup)
//    {
//        
//    }
    
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        final int MINSUP = 1;

        // Count in opposite alpha order
        Item itemA = new Item("a",26);
        Item itemB = new Item("b",25);
        Item itemC = new Item("c",24);
        Item itemD = new Item("d",23);
        Item itemE = new Item("e",22);

        Doc ln01 = new Doc("lno1",Arrays.asList(new Item[]{itemA,itemB,itemD,itemE}));
        Doc ln02 = new Doc("lno1",Arrays.asList(new Item[]{itemB,itemC,itemE}));
        Doc ln03 = new Doc("lno1",Arrays.asList(new Item[]{itemA,itemB,itemD,itemE}));
        Doc ln04 = new Doc("lno1",Arrays.asList(new Item[]{itemA,itemB,itemC,itemE}));
        Doc ln05 = new Doc("lno1",Arrays.asList(new Item[]{itemA,itemB,itemC,itemD,itemE}));
        Doc ln06 = new Doc("lno1",Arrays.asList(new Item[]{itemB,itemC,itemD}));

        List<Doc> lds= Arrays.asList(ln01,ln02,ln03,ln04,ln05,ln06);
        float docCount = (float)lds.size();

        // "Vertical" database
        Map<Item,List<Doc>> item2Docs = new HashMap<>();
        item2Docs.put(itemA, List.of(ln01,ln03,ln04,ln05));
        item2Docs.put(itemB, List.of(ln01,ln02,ln03,ln04,ln05,ln06));
        item2Docs.put(itemC, List.of(ln02,ln04,ln05,ln06));
        item2Docs.put(itemD, List.of(ln01,ln03,ln05,ln06));
        item2Docs.put(itemE, List.of(ln01,ln02,ln03,ln04,ln05));
        
        // Sort items by frequency (SQL function)
        List<Item> sortItemsDesc = new ArrayList<>(item2Docs.keySet());
        // Set global support count for each item (SQL function)
        sortItemsDesc.forEach(lis -> {
            lis.setGlobalSupport(item2Docs.get(lis).size());
        });

        /*
         * Process
         * -------
         */
        // Init
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));
        // @TODO: Set frequent items sort (SQL function??)
        tree.setFreqItemSup(sortItemsDesc);
        
        // ...Build document tree
        lds.forEach(doc -> {
            tree.matchAndInsert(doc.getLi());
        });
        
        System.out.println("\r\n==================================================");
        System.out.println("Note: ()s denote count");
        FTTree.printTree(tree, " ", false);
        System.out.println("====================================================");

//        
//        System.out.println("\r\n------------------------------------------------");
//        dumpNodeHeadMapFull(tree);
//        System.out.println("------------------------------------------------");
        
        System.out.println("\r\n----------[FP growth]----->");
        List<FTTree<Item>> lfptp = tree.fpGrowth();
        
        lfptp.forEach(fptp ->
        {
            System.out.println("\r\n===================[FP tree projection: "+fptp.getRoot()+"]===============================");
            FTTree.printTree(fptp, " ", true);
        });
        
        /*
         * Form clusters
         * -------------
         * Needs current tree, freq.pattern growth list amd item to doc map
         */
        System.out.println("\r\nForm clusters:");
        Set<Cluster> clusters = Cluster.clustersFromFreqItems(tree,lfptp,item2Docs,true);
        
        System.out.println();
        System.out.println("==============================[Clusters]==============================");
        clusters.forEach(c -> {
            System.out.print("\r\nCluster [" +c.getName()+ "]: ");
            c.getDocs().forEach(d -> System.out.print(" "+d.getName()));
        });
        System.out.println();
    }
}
