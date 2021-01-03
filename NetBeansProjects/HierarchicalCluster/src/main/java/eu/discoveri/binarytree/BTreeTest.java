/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import eu.discoveri.utils.DoubleMap;


/**
 *
 * @author chrispowell
 */
public class BTreeTest
{
    public static void main(String[] args) {

// http://www.econ.upf.edu/~michael/stanford/maeb7.pdf
// 0=A,1=B,2=C,3=D,4=E,5=F,6=G
        Pair ab = new Pair(0, 1, 0.5d);
        Pair ac = new Pair(0, 2, 0.4286d);
        Pair ad = new Pair(0, 3, 1.0d);
        Pair ae = new Pair(0, 4, 0.25d);
        Pair af = new Pair(0, 5, 0.625d);
        Pair ag = new Pair(0, 6, 0.375d);
        Pair bc = new Pair(1, 2, 0.7143d);
        Pair bd = new Pair(1, 3, 0.8333d);
        Pair be = new Pair(1, 4, 0.6667d);
        Pair bf = new Pair(1, 5, 0.2d);           // 1st min. doc 1 & 5
        Pair bg = new Pair(1, 6, 0.7778d);
        Pair cd = new Pair(2, 3, 1.0d);
        Pair ce = new Pair(2, 4, 0.4286d);
        Pair cf = new Pair(2, 5, 0.6667d);
        Pair cg = new Pair(2, 6, 0.3333d);
        Pair de = new Pair(3, 4, 1.0d);
        Pair df = new Pair(3, 5, 0.8d);
        Pair dg = new Pair(3, 6, 0.8571d);
        Pair ef = new Pair(4, 5, 0.7778d);
        Pair eg = new Pair(4, 6, 0.375d);
        Pair fg = new Pair(5, 6, 0.75d);

        /*
         * Distance "matrix", eg: A-G cols, A-G rows.
         * The collection of Pairs represents the bottom left triangle of the
         * distance matrix. The top right triangle is a mirror image.
         *
         * Process: A dendrogram (a sort of binary tree) is to be formed. Cells
         * (col/row cross) initially constitute LeafNodes (extension of Node) of
         * the dendrogram.  Subsequent, higher level nodes simply point to lower
         * nodes in the dendrogram.  The process is: rows and columns are 'merged'
         * a pair at a time using min./max. rules and each merge then forms a
         * (simple) Node culminating in a root node.
         */
        //... Pairs [Note: Here doc0 is column, doc1 row.
        List<Pair> lp = Collections.EMPTY_LIST;
        Collections.addAll(lp = new ArrayList<>(), ab, ac, ad, ae, af, ag, bc, bd, be, bf, bg, cd, ce, cf, cg, de, df, dg, ef, eg, fg);

        // Enough initial nodes?
        int lpSize = lp.size();
        System.out.println("Initial Pairs/nodes size: " + lpSize);
        if (lpSize < 2) {
            System.out.println("Not enough Pairs");
            return;
        }

//        System.out.println("Pairs:");
//        lp.forEach(p -> {
//            System.out.println("[" +p.getDoc0()+ "," +p.getDoc1()+ "] " +p.getCosScore());
//        });
        // Index the Pairs list via docIds
        DoubleMap<Integer, Integer, Pair> pairTable = new DoubleMap();
        DoubleMap<Integer, Integer, Pair> origTable = new DoubleMap();
        lp.forEach(l -> {
            pairTable.put(l.getDoc0(), l.getDoc1(), l);
            origTable.put(l.getDoc0(), l.getDoc1(), l);
        });

        //... Nodes
        // Dendrogram leaf level
//        Map<Pair,Node> ln = new HashMap<>();
//        lp.forEach(l -> {
//            ln.put(l,new LeafNode(l));
//        });
                
        while( pairTable.getSize() > 1 )
        {
            // Replacement cells
            List<Pair> pAdd = new ArrayList<>();

            // Find min.
            Pair minPair = Collections.min(pairTable.allValues());
            System.out.println("Min pair: " + minPair);
            int minCol = minPair.getDoc0();                                     // Eg: 1 (b)
            int minRow = minPair.getDoc1();                                     // Eg: 5 (f)
            
            // Used for col/row shifting
            int newRow = minRow;
            int newCol = minCol;

//            System.out.println("minCol: " +minCol+ ", minRow: " +minRow);
            // Process table for new rows in current cols
            for( int col: pairTable.getBaseColKeys() )
            {
                // Don't process self and above
                if( col >= minCol ) break;

                // Process columns "to left of" minCol
                // Pair cells from which max.
                Pair p0 = pairTable.get(col, minCol);                           // Eg 0:1 (a:b)
//                System.out.println("C1. col: " +col+ ", d0: " +minCol+ "(" +p0.getCosScore()+ ")");
                Pair p1 = pairTable.get(col, minRow);                           // Eg 0:5 (a:f)
//                System.out.println("C2. col: " +col+ ", d1: " +minRow+ "(" +p1.getCosScore()+ ")");

                // Max. of these will be new score
                double v0 = p0.getCosScore();
                double v1 = p1.getCosScore();

                // "New" column
                pAdd.add(new Pair(col, minCol, (v0 >= v1 ? v0 : v1)));
            }

            // Process table for new rows in new col
            for( int row: pairTable.getRowKeys(minCol) )
            {
                // Don't process self
                if( row == minRow ) continue;

                // Pair cells from which max.
                Pair p0 = null, p1 = null;
                if( row < minRow )
                {
                    p0 = pairTable.get(minCol, row);                    // Eg 1:2 (b:c)
//            System.out.println("1. Col: " +minCol+ ", Row: " +row);
//                    System.out.println("R1. col: " +minCol+ ", d0: " +row+ "(" +p0.getCosScore()+ ")");
                    p1 = pairTable.get(row, minRow);                    // Eg 2:5 (c:f)
//            System.out.println("2. Col: " +row+ ", Row: " +minRow);
//                    System.out.println("R2. col: " +row+ ", d1: " +minRow+ "(" +p1.getCosScore()+ ")\r\n");

                    // Max. of these will be new score
                    double v0 = p0.getCosScore();
                    double v1 = p1.getCosScore();

                    // "New" column
                    pAdd.add(new Pair(minCol, row, (v0 >= v1 ? v0 : v1)));
                }
                else
                {
                    p0 = pairTable.get(minCol, row);                    // Eg 1:6 (b:c)
//                    System.out.println("R3. col: " +minCol+ ", d0: " +row+ "(" +p0.getCosScore()+ ")");
                    p1 = pairTable.get(minRow, row);                    // Eg 5:6 (c:f)
//                    System.out.println("R4. col: " +minRow+ ", d1: " +row+ "(" +p1.getCosScore()+ ")");

                    // Max. of these will be new score
                    double v0 = p0.getCosScore();
                    double v1 = p1.getCosScore();

                    // "New" column
                    pAdd.add(new Pair(minCol, newRow++, (v0 >= v1 ? v0 : v1)));
                }
            }

//            System.out.println("--------------[Start]---------------------");
//            pairTable.dumpMap(System.out);
//            System.out.println("");
//            System.out.println("------------------------------------------");
            
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            /*
             * Delete/shift rows and columns of minPair.
             * Eg: [1,5]: Delete rows 1 & 5, cols 1 & 5
             * Shift row 6 "up" to 5
             */
            pairTable.getColKeys(minRow).forEach(col ->
            {
                // Remove row
                pairTable.remove(col, minRow);
                // Shift rows up
                
            });
            pairTable.getColKeys(minCol).forEach(col -> pairTable.remove(col, minCol));
            pairTable.getRowKeys(minCol).stream()
                    .collect(Collectors.toSet())
                    .forEach(row -> pairTable.remove(minCol, row));
            pairTable.getRowKeys(minRow).stream()
                    .collect(Collectors.toSet())
                    .forEach(row -> pairTable.remove(minRow, row));
            
            pAdd.forEach(p ->
            {
                System.out.println("> " +p);
                pairTable.put(p.getDoc0(), p.getDoc1(), p);
            });
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            System.out.println("================[new map]=======================");
            pairTable.dumpMap(System.out);
            System.out.println("================================================");
        }
    }
}

//------------------------------------------------------------------------------

