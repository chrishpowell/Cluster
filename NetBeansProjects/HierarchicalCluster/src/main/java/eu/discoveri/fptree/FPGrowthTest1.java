/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import static eu.discoveri.fptree.FTTree.dumpNodeHeadMapFull;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class FPGrowthTest1
{
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        final int MINSUP = 1;
        FTTree<Item> tree = FTTree.initialiseTree(new Item("root",0));

        // Count in opposite alpha order
        Item itemA = new Item("a",26);
        Item itemB = new Item("b",25);
        Item itemC = new Item("c",24);
        Item itemD = new Item("d",23);
        Item itemE = new Item("e",22);
//        Item itemF = new Item("f",21);
//        Item itemM = new Item("m",14);
        Item itemP = new Item("p",11);

        List<Item> ln01 = Arrays.asList(new Item[]{itemA,itemB});
        List<Item> ln02 = Arrays.asList(new Item[]{itemB,itemC,itemD});
        List<Item> ln03 = Arrays.asList(new Item[]{itemA,itemC,itemD,itemE});
        List<Item> ln04 = Arrays.asList(new Item[]{itemA,itemD,itemE});
        List<Item> ln05 = Arrays.asList(new Item[]{itemA,itemB,itemC});
        List<Item> ln06 = Arrays.asList(new Item[]{itemA,itemB,itemC,itemD});
        List<Item> ln07 = Arrays.asList(new Item[]{itemA});
        List<Item> ln08 = Arrays.asList(new Item[]{itemA,itemB,itemC});
        List<Item> ln09 = Arrays.asList(new Item[]{itemA,itemB,itemD});
        List<Item> ln10 = Arrays.asList(new Item[]{itemB,itemC,itemE});
        
        List<Item> ln11 = Arrays.asList(new Item[]{itemA,itemP});
        List<Item> ln12 = Arrays.asList(new Item[]{itemA,itemP});

        System.out.println("1st list (a,b)...");
        tree.matchAndInsert(ln01);
        System.out.println("2nd list (b,c,d)...");
        tree.matchAndInsert(ln02);
        System.out.println("3rd list (a,c,d,e)...");
        tree.matchAndInsert(ln03);
        System.out.println("4th list (a,d,e)...");
        tree.matchAndInsert(ln04);
        System.out.println("5th list (a,b,c)...");
        tree.matchAndInsert(ln05);
        System.out.println("6th list (a,b,c,d)...");
        tree.matchAndInsert(ln06);
        System.out.println("7th list (a)...");
        tree.matchAndInsert(ln07);
        System.out.println("8th list (a,b,c)...");
        tree.matchAndInsert(ln08);
        System.out.println("9th list (a,b,d)...");
        tree.matchAndInsert(ln09);
        System.out.println("10th list (b,c,e)...");
        tree.matchAndInsert(ln10);
        
//        System.out.println("11/12th lists (a,p)...");
//        tree.matchAndInsert(ln11); tree.matchAndInsert(ln12);
        
        // Item freq sorted by descr.
//        List<Item> itemSup = Arrays.asList(new Item[]{itemA,itemB,itemC,itemD,itemE,itemP});
        List<Item> itemSup = Arrays.asList(new Item[]{itemA,itemB,itemC,itemD,itemE});
        
        // Set frequent items list
        tree.setFreqItemSup(itemSup);
        
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
    }
}
