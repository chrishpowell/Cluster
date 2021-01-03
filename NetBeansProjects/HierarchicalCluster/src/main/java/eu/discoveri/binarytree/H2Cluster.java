/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author chrispowell
 */
public class H2Cluster
{
    /**
     * Dump the table
     * @param dm 
     */
    public static void dumpTable(Table<Integer,Integer,LeafNode> dm)
    {
        dm.rowMap().forEach((r,v) -> {
            v.forEach((c,n) -> {
                System.out.println("Cell [" +r+ "," +c+ "] val: " +n.getNodeVal().getCosScore());
            });
        });
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        // http://www.econ.upf.edu/~michael/stanford/maeb7.pdf
        // 0=A,1=B,2=C,3=D,4=E,5=F,6=G
        Pair ab = new Pair(0,1,0.5d);
        Pair ac = new Pair(0,2,0.4286d);
        Pair ad = new Pair(0,3,1.0d);
        Pair ae = new Pair(0,4,0.25d);
        Pair af = new Pair(0,5,0.625d);
        Pair ag = new Pair(0,6,0.375d);
        Pair bc = new Pair(1,2,0.7143d);
        Pair bd = new Pair(1,3,0.8333d);
        Pair be = new Pair(1,4,0.6667d);
        Pair bf = new Pair(1,5,0.2d);           // 1st min. doc 1 & 5
        Pair bg = new Pair(1,6,0.7778d);
        Pair cd = new Pair(2,3,1.0d);
        Pair ce = new Pair(2,4,0.4286d);
        Pair cf = new Pair(2,5,0.6667d);
        Pair cg = new Pair(2,6,0.3333d);
        Pair de = new Pair(3,4,1.0d);
        Pair df = new Pair(3,5,0.8d);
        Pair dg = new Pair(3,6,0.8571d);
        Pair ef = new Pair(4,5,0.7778d);
        Pair eg = new Pair(4,6,0.375d);
        Pair fg = new Pair(5,6,0.75d);
        
        /*
         * Distance "matrix", eg: A-G rows, A-G cols.
         * The collection of Pairs represents the bottom left triangle of the
         * distance matrix. The top right triangle is a mirror image.
         * Be aware!  As a result, row 0 [0,0..n] is not included in the table
         * whereas col 0 is. And [0,0]/[A,A] is not entered up. 
         *
         * Process: A dendrogram (a sort of binary tree) is to be formed. Cells
         * (row/col cross) initially constitute LeafNodes (extension of Node) of
         * the dendrogram.  Subsequent, higher level nodes simply point to lower
         * nodes in the dendrogram.  The process is: rows and columns are 'merged'
         * a pair at a time using min./max. rules and each merge then forms a
         * (simple) Node culminating in a root node.
         */
        //... Pairs
        List<Pair> lp = Collections.EMPTY_LIST;
        // 
        Collections.addAll(lp = new ArrayList<>(), ab,ac,ad,ae,af,ag,bc,bd,be,bf,bg,cd,ce,cf,cg,de,df,dg,ef,eg,fg );
        
        // Enough initial nodes?
        int lpSize = lp.size();
        System.out.println("Initial Pairs/nodes size: " +lpSize);
        if( lpSize < 2 )
        {
            System.out.println("Not enough Pairs");
            return;
        }
        
        //... Nodes
        // Dendrogram leaf level
        Map<Pair,LeafNode> ln = new HashMap<>();
        lp.forEach(l -> {
            ln.put(l,new LeafNode(l));
        });
        
        // "Distance" matrix
        Table<Integer,Integer,LeafNode> distMatrix = HashBasedTable.create();
        
        // Populate dist matrix (Row,Col,Value)
        ln.forEach((k,v) -> {
            distMatrix.put(k.getDoc0(),k.getDoc1(),v);
        });

        dumpTable(distMatrix);
        
        LeafNode minNode  = Collections.min(distMatrix.values());   // [1/5]
//        System.out.println("..> " +minNode.getNodeVal().getDoc0()+ "/" +minNode.getNodeVal().getDoc1());
      
        // Delete row
        Map<Integer,LeafNode> row = distMatrix.row(minNode.getNodeVal().getDoc0());
//        row.forEach((k,v) -> {
//            System.out.println("  >> " +k+ ": " +v.getNodeVal().getDoc0()+ "/" +v.getNodeVal().getDoc1()+ "(" +v.getNodeVal().getCosScore()+ ")");
//        });
        for( Iterator<Integer> r = row.keySet().iterator(); r.hasNext(); )
        {
            int k = r.next();
            r.remove();
        }
        // Delete column
        int delCol = minNode.getNodeVal().getDoc1();
        distMatrix.rowKeySet().forEach(r -> {
            distMatrix.remove(r, delCol);
        });
        
        
//        
        System.out.println("-----------------");
        dumpTable(distMatrix);
    }
}
