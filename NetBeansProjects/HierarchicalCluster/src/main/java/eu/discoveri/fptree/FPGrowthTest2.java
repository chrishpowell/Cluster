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
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 * @author chrispowell
 */
public class FPGrowthTest2
{
    public static void main(String[] args)
    {
        final double MINSUP = 0.3d; // Min. % of docs in which item appears
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));

        // Items (with count)
        Item itemA = new Item("a",3);
        Item itemB = new Item("b",2);
        Item itemC = new Item("c",2);
        Item itemD = new Item("d",1);
        Item itemE = new Item("e",1);
        Item itemF = new Item("f",1);
        
        // Item freq sorted by descr.
        List<Item> itemSup = Arrays.asList(new Item[]{itemA,itemB,itemC,itemD,itemE,itemF});

        Doc ln01 = new Doc("ln01", Arrays.asList(new Item[]{itemA,itemB,itemC}));
        Doc ln02 = new Doc("ln02", Arrays.asList(new Item[]{itemA,itemC}));
        Doc ln03 = new Doc("ln03", Arrays.asList(new Item[]{itemA,itemD}));
        Doc ln04 = new Doc("ln04", Arrays.asList(new Item[]{itemB,itemE,itemF}));
        
        Map<Item,List<Doc>> item2Docs = new HashMap<>();
        item2Docs.put(itemA, List.of(ln01,ln02,ln03));
        item2Docs.put(itemB, List.of(ln01,ln04));
        item2Docs.put(itemC, List.of(ln01,ln02));
        item2Docs.put(itemD, List.of(ln03));
        item2Docs.put(itemE, List.of(ln04));
        item2Docs.put(itemF, List.of(ln04));

        // Build tree
        System.out.println("1st list (a,b,c)...");
        tree.matchAndInsert(ln01.getLi());
        System.out.println("4th list (a,c)...");
        tree.matchAndInsert(ln02.getLi());
        System.out.println("1st list (a,d)...");
        tree.matchAndInsert(ln03.getLi());
        System.out.println("4th list (b,e,f)...");
        tree.matchAndInsert(ln04.getLi());
        
        // Set frequent items list
        tree.setFreqItemSup(itemSup);

        System.out.println("\r\n===================[Tree:"+tree.getRoot().getName()+"]===============================");
        System.out.println("Note: ()s denote count");
        FTTree.printTree(tree, " ", false);
        System.out.println("========================================================");

        System.out.println("\r\n----------[FP growth]----->");
        List<FTTree<Item>> lfptp = tree.fpGrowth();
        
        lfptp.forEach(fptp -> {
            System.out.println("\r\n===================[FP tree projection: "+fptp.getRoot()+"]===============================");
            FTTree.printTree(fptp, " ", true);
        });
        
        // Process FPgrowth freq. itemsets (list of 'mini' trees)
        lfptp.forEach(fptp ->
        {
            String iName = fptp.getRoot().getName();
            
            // Generate freq. itemsets subsets (all subsets of the set of nodes in the 'mini' tree. Eg [a,b] -> [{},a,b,ab])
            System.out.println("==============================[Freq. itemsets subsets: "+iName+"]==============================");
            
            // First, flatten the tree (into a set/list)
            List<FTTree<Item>> ft = fptp.listFlattenedTree();
            // Remove the root object (the freq. itemsets are itemsets of the root)
            ft.remove(0);
            // Get rid of non-frequent itemsets
            ft.removeIf(i -> i.getNodeCount()<0);
            
            // Generate all subsets (being a collection of tree nodes) of this itemset
            Supplier<Stream<List<FTTree<Item>>>> lfti = () -> tree.subsets(ft);
            
            // Print each subset
            lfti.get().forEach(lss ->
            {
                System.out.print("> ");
//                List<FTTree<Item>> lfi = new ArrayList<>();
                lss.forEach(fttree ->
                {
//                    lfi.add(fttree);                    // FT(a), FT(b), FT(ab)
                    System.out.print(fttree.getContents().getName());
                });
                System.out.println("");
            });
            
            // Each subset has an initial cluster (which might get ditched)
            lfti.get().forEach(lss ->
            {
                List<Doc> docsCluster = new ArrayList<>();
                lss.forEach(fttree ->
                {
                    List<Doc> ld = item2Docs.get(fttree.getContents());
                    for( Doc doc: ld )
                    {
                        ld.stream()
                            .filter(d -> (!docsCluster.contains(d)))
                            .forEachOrdered(d -> {docsCluster.add(d);});
                    }
//                    lli.forEach(li ->
//                    {
//                        li.forEach(i ->
//                        {
//                            System.out.print("(" +i.getName()+ ")");
//                        });
//                        System.out.println("");
//                    });
                });
                
                // Docs cluster
                docsCluster.forEach(dc -> System.out.print(":"+dc.getName()));
                System.out.println("");
            });
            
            System.out.println("............................................");

//            List<List<List<Item>>> docsWithItems = new ArrayList<>();
//            lfi.forEach(fttree ->
//            {
//                List<List<Item>> lli = item2Docs.get(fttree.getContents());
//                docsWithItems.add(lli);
//            });
//            
//            // Merge list of freq. item 
//            System.out.println("");
        });
    }
}
