/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import static eu.discoveri.fptree.FTTree.dumpNodeHeadMapFull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author chrispowell
 */
public class Tree1
{
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));

        // Count in opposite alpha order
        Item itemA = new Item("a",1);
        Item itemB = new Item("b",1);
        Item itemC = new Item("c",1);
//        Item itemD = new Item("d",23);
//        Item itemE = new Item("e",22);
        Item itemF = new Item("f",1);
        Item itemM = new Item("m",1);
        Item itemP = new Item("p",1);

        List<Item> ln01 = Arrays.asList(new Item[]{itemF,itemC,itemA,itemM,itemP});
        List<Item> ln0a = Arrays.asList(new Item[]{itemC,itemP});
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
        System.out.println("6th list (c,p)...");
        tree.matchAndInsert(ln0a);

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



        // Paths from root to leaf [p]
        System.out.println("\r\n==================================================");
        System.out.println("Conditional pattern base (root paths) to p:");

        // All Ps paths to root
        List<List<FTTree<Item>>> llftP = tree.crosslinkPathBottomUp(itemP);
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
        
        Map<Item,List<FTTree<Item>>> nhm = tree.getNodeHeadMap();
        int[] leafTot = new int[1];
        nhm.get(itemP).forEach(n -> {
            leafTot[0] += n.getNodeCount();
        });
        
        llftP.forEach(lp ->
        {
            FTTree<Item> leaf = lp.get(lp.size()-1);
            
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
        System.out.println("====================================================");

        

        // Paths from root to leaf [m]
        System.out.println("\r\n==================================================");
        System.out.println("Conditional pattern base (root paths) to m:");
        List<List<FTTree<Item>>> llftM = tree.crosslinkPathBottomUp(itemM);
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

        });

        FTTree.printTree(cpTreeM, " ", false);
        System.out.println("====================================================");
    }
}
