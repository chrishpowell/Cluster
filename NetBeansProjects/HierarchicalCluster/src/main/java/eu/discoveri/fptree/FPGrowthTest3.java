/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import eu.discoveri.utils.Subsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 *
 * @author chrispowell
 */
public class FPGrowthTest3
{
    public static void main(String[] args) throws InvalidSupportPercentageException
    {
        final int MINSUP = 3;

        // Items (with count of docs that contain that item)
        Item itemA = new Item("a",4);
        Item itemB = new Item("b",6);
        Item itemC = new Item("c",4);
        Item itemD = new Item("d",4);
        Item itemE = new Item("e",5);

        // Documents
        Doc ln01 = new Doc("ln01",Arrays.asList(new Item[]{itemE,itemA,itemB,itemD}));
        Doc ln02 = new Doc("ln02",Arrays.asList(new Item[]{itemC,itemB,itemE}));
        Doc ln03 = new Doc("ln03",Arrays.asList(new Item[]{itemB,itemE,itemA,itemD}));
        Doc ln04 = new Doc("ln04",Arrays.asList(new Item[]{itemB,itemE,itemA,itemC}));
        Doc ln05 = new Doc("ln05",Arrays.asList(new Item[]{itemB,itemD,itemE,itemA,itemC}));
        Doc ln06 = new Doc("ln06",Arrays.asList(new Item[]{itemB,itemC,itemD}));

        List<Doc> lds= Arrays.asList(ln01,ln02,ln03,ln04,ln05,ln06);
        int docCount = lds.size();
        
        // Sort docs by item name and then item size desc. (SQL function)
        lds.forEach(doc -> {
            Collections.sort(doc.getLi(),Comparator.comparing(Item::getName));
            Collections.sort(doc.getLi());
        });
        
        lds.forEach(doc -> {
            System.out.println("Doc: " +doc.getName());
            doc.getLi().forEach(i -> System.out.print(" " +i.getName()));
            System.out.println("");
        });
        System.out.println("..................................................");
        
        Map<String,Doc> docs = new HashMap<>();
        docs.put("ln01",ln01);
        docs.put("ln02",ln02);
        docs.put("ln03",ln03);
        docs.put("ln04",ln04);
        docs.put("ln05",ln05);
        docs.put("ln06",ln06);

        Map<Item,List<Doc>> item2Docs = new HashMap<>();
        item2Docs.put(itemA,List.of(ln01,ln03,ln04,ln05));
        item2Docs.put(itemB,List.of(ln01,ln02,ln03,ln04,ln05,ln06));
        item2Docs.put(itemC,List.of(ln02,ln04,ln05,ln06));
        item2Docs.put(itemD,List.of(ln01,ln03,ln05,ln06));
        item2Docs.put(itemE,List.of(ln01,ln02,ln03,ln04,ln05));

        // Item freq sorted by descending ****************** SQL function
        List<Item> sortItemsDesc = Arrays.asList(new Item[]{itemB,itemE,itemA,itemC,itemD});
        
        //-----------------------
        // Slow method for subsets
        Stream<List<Item>> sli = Subsets.subsetsList(sortItemsDesc);
        Set<Set<Item>> allSS = Subsets.subsetsAllSets(sortItemsDesc);
        System.out.println("allSS (" +allSS.size()+ "): "+allSS);
//        sli.forEach(ssi -> {
//            System.out.println(" ...> " +ssi);
//        });
        //-----------------------
        
        // Set frequent items (num transactions/docs that contain each item/word)
        SupportCount.setFreqItemSup(sortItemsDesc);

        // Set global support count for each item (SQL function)
        SupportCount.globalSupport(lds, sortItemsDesc);
// === OK to here

        
        /*
         * Process (initial tree with cross-link maps)
         * -------
         */
        // ...Initial tree
        FTTree.Root tree = FTTree.initialise(new Item("root",0));
        
        // ...Build tree
        lds.forEach(doc -> {
            tree.matchAndInsert(doc.getLi());
        });

        // Show tree
        System.out.println("\r\n===================[Tree:"+tree.getContents().getName()+"]===============================");
        System.out.println("Note: ()s denote count");
        FTTree.printTree(tree, " ", false);
        System.out.println("========================================================");
// === OK to here  [May 2021]

        // ...Frequent Pattern growth (Algo 8.5)
        System.out.println("\r\n----------[FP growth]----->");
        List<FTTree> lfptp = tree.fpGrowth();
        
        lfptp.forEach(fptp -> {
            System.out.println("\r\n===================[FP tree projection: "+fptp.getContents().getName()+"]===============================");
            FTTree.printTree(fptp, " ", true);
        });
// OK-ish to here (counts may be wrong)

        /*
         * ...Now generate Clusters
         */
        List<Cluster> clusters = new ArrayList<>();
        
        // Process FPgrowth freq. itemsets (list of 'mini' trees)
        lfptp.forEach(fptp ->
        {
            String iName = fptp.getContents().getName();
            
            /*
             * Generate freq. itemsets subsets (all subsets of the set of nodes
             * in the 'mini' tree. Eg [a,b] -> [{},a,b,ab]), ignoring empty set
             */
            // First, flatten the tree (into a set/list)
            List<FTTree> ft = fptp.listFlattenedTree();
            
            // Remove the root object (the freq. itemsets are itemsets of the root)
//            ft.remove(0);
            // Get rid of non-frequent itemsets
            ft.removeIf(i -> i.getNodeCount()<MINSUP);
            
            // Generate all subsets (being a collection of tree nodes) of this itemset
            Supplier<Stream<List<FTTree>>> lfti = () -> Subsets.subsetsList(ft);
            
            // Print each subset
//            lfti.get().forEach(lss ->
//            {
//                System.out.print("> ");
//                lss.forEach(fttree ->
//                    { System.out.print(":"+fttree.getContents().getName()); });
//                System.out.println("");
//            });
            
            // Process each subset ('mini' tree) where a tree has size
            lfti.get().filter(lssc -> lssc.size()>0).forEach(lss ->
            {
//                Collections.reverse(lss);
                // List of docs containing items of tree
                List<Doc> docsList = new ArrayList<>();
                // Cluster name
                StringBuilder cn = new StringBuilder(iName+":");

                lss.stream().forEach(fttree ->
                {
                    // Get the list of docs associated with this subset
                    List<Doc> ld = item2Docs.get(fttree.getContents());

                    // Build a cluster name
                    cn.append(fttree.getContents().getName()).append(":");
                    
                    // Get unique set of docs for this subset
                    ld.forEach(_item -> {
                        ld.stream()
                                .filter(d -> (!docsList.contains(d)))
                                .forEachOrdered(d -> {docsList.add(d);});
                    });
                });

                clusters.add(new Cluster(cn.toString(),docsList));
            });
        });
        
        System.out.println();
        System.out.println("==============================[Clusters]==============================");
        clusters.forEach(c -> {
            System.out.print("\r\nCluster [" +c.getName()+ "]: ");
            c.getDocs().forEach(d -> System.out.print(" "+d.getName()));
        });
        System.out.println();
    }
}
