/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class BTreeTest
{
    public static void main(String[] args)
    {
        BinaryTree bt = new BinaryTree();
        
//        bt.add( new Pair(0,1,0.0667d));
//        bt.add( new Pair(0,2,0.5d));
//        bt.add( new Pair(0,3,0.01d));
//        bt.add( new Pair(0,4,0.25d));
//        bt.add( new Pair(1,2,0.999d));
//        bt.add( new Pair(1,3,0.883d));
//        bt.add( new Pair(1,4,0.765d));
//        bt.add( new Pair(2,3,0.02d));
//        bt.add( new Pair(2,4,0.444d));
//        bt.add( new Pair(3,4,0.334d));
        
//        bt.insert( new Pair(0,1,0.0667d));
//        bt.insert( new Pair(0,2,0.5d));
//        bt.insert( new Pair(0,3,0.01d));
//        bt.insert( new Pair(0,4,0.25d));
//        bt.insert( new Pair(1,2,0.999d));
//        bt.insert( new Pair(1,3,0.883d));
//        bt.insert( new Pair(1,4,0.765d));
//        bt.insert( new Pair(2,3,0.02d));
//        bt.insert( new Pair(2,4,0.444d));
//        bt.insert( new Pair(3,4,0.334d));
        
//        bt.add( new Pair(0,1,0.5d));
//        bt.add( new Pair(0,2,0.5d));
//        bt.add( new Pair(0,3,0.5d));
//        bt.add( new Pair(0,4,0.5d));
//        bt.add( new Pair(1,2,0.5d));
//        bt.add( new Pair(1,3,0.5d));
//        bt.add( new Pair(1,4,0.5d));
//        bt.add( new Pair(2,3,0.5d));
//        bt.add( new Pair(2,4,0.5d));
//        bt.add( new Pair(3,4,0.5d));
        
//        bt.insert( new Pair(0,1,0.5d));
//        bt.insert( new Pair(0,2,0.5d));
//        bt.insert( new Pair(0,3,0.5d));
//        bt.insert( new Pair(0,4,0.5d));
//        bt.insert( new Pair(1,2,0.5d));
//        bt.insert( new Pair(1,3,0.5d));
//        bt.insert( new Pair(1,4,0.5d));
//        bt.insert( new Pair(2,3,0.5d));
//        bt.insert( new Pair(2,4,0.5d));
//        bt.insert( new Pair(3,4,0.5d));

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
        
        // Distance "matrix".  The collection is the bottom triangle of the matrix.
        List<Pair> lp = Collections.EMPTY_LIST;
        Collections.addAll(lp = new ArrayList<>(), ab,ac,ad,ae,af,ag,bc,bd,be,bf,bg,cd,ce,cf,cg,de,df,dg,ef,eg,fg );
        
        // Enough initial nodes?
        int lpSize = lp.size();
        System.out.println("Initial Pairs/nodes size: " +lpSize);
        if( lpSize < 2 )
        {
            System.out.println("Not enough Pairs");
            return;
        }
        
        // Index the Pairs list via dcocIds
        DoubleMap<Integer,Integer,Pair> pairTable = new DoubleMap();
        lp.forEach(l -> {
            pairTable.put(l.getDoc0(),l.getDoc1(),l);
        });

        // Dendrogram leaf level
        List<Node> ln = new ArrayList<>();
        lp.forEach(l -> {
            ln.add(new LeafNode(l));
        });
        
        /*
         * Build rest of dendrogram
         */
        int ii = lpSize;
        do
        {
            // Find min.
            Pair minPair = Collections.min(lp);     // Default: Sorted by score
            int d0 = minPair.getDoc0();             // Eg: 1 (b)
            int d1 = minPair.getDoc1();             // Eg: 5 (f)
            lp.remove(minPair);
            System.out.println("Min pair--> " +minPair);
            --ii;
            List<Pair> pRemove = new ArrayList<>();
            
            /*
             * Remove rows and cols matching max of min.
             * Build new row/col for merging (Eg: b&f) [NB: bf refers to cell
             * where b and f cross] 
             */
            // Row/col remove
            for( Iterator<Pair> ip = lp.iterator(); ip.hasNext(); )
            {
                Pair p1 = ip.next();
                
                // If Pair row or column match minPair (in distance matrix), remove that column
                if( p1.getDoc0() == d0 || p1.getDoc1() == d0 )
                {
                    pRemove.add(p1);
                    ip.remove();
                    --ii;
                }
                if( p1.getDoc0() == d1 || p1.getDoc1() == d1 )
                {
                    pRemove.add(p1);
                    ip.remove();
                    --ii;
                }
            }
            
            // Build new node
            double newCellScore = Double.MIN_VALUE;
            Collections.sort(pRemove,new SortByDocId0());            // Ensure order
            for( Pair ip: pRemove )
            {
                System.out.println("Removed doc " +ip.getDoc0()+ "/" +ip.getDoc1());
                if( !ip.getFlagRemove() )                            // Still active?
                {
                    if( ip.getDoc0() < d0 )
                    { //System.out.println("1...> " +d0+ "/" +d1+ ", Doc0: " +ip.getDoc0());
                        Pair p0 = pairTable.get(ip.getDoc0(),d0);
                        Pair p1 = pairTable.get(ip.getDoc0(),d1);

                        double v0 = p0.getCosScore();
                        double v1 = p1.getCosScore();

                        newCellScore = (v0 >= v1 ? v0 : v1);        // Max. of these pairs
                        p0.setFlagRemove(); p1.setFlagRemove();
                    }
                    else
                        if( d1 <= ip.getDoc1() )
                        { //System.out.println("2...> " +d0+ "/" +d1+ ", Doc1: " +ip.getDoc1());
                            Pair p0 = pairTable.get(d0,ip.getDoc1());
                            Pair p1 = pairTable.get(d1,ip.getDoc1());

                            double v0 = p0.getCosScore();
                            double v1 = p1.getCosScore();

                            newCellScore = (v0 >= v1 ? v0 : v1);    // Max. of these pairs
                            p0.setFlagRemove(); p1.setFlagRemove();
                        }
                        else
                        { //System.out.println("3...> " +d0+ "/" +d1+ ", Doc1: " +ip.getDoc1());
                            Pair p0 = pairTable.get(d0,ip.getDoc1());
                            Pair p1 = pairTable.get(ip.getDoc1(),d1);

                            double v0 = p0.getCosScore();
                            double v1 = p1.getCosScore();

                            newCellScore = (v0 >= v1 ? v0 : v1);    // Max. of these pairs
                            p0.setFlagRemove(); p1.setFlagRemove();
                        }
                    
                    System.out.println("--> Replace cell: " +d0+ "/" +d1+ ", score: " +newCellScore);
                }
            }
            
            // Add pair of Pairs to collection
//            Node merge = new Node(new Pair(0,0,));
//            merge.setHeight(minPair1st.getNodeVal().getCosScore());
            
            // Put into binary tree

        } while( ii > 0 );
        
//        bt.insert( ae ); // 4 AE
//        bt.insert( bf ); // 10 BF
//        bt.insert( cg ); // 15 CG
//        bt.insert( ab ); // 1 AB
//        bt.insert( ac ); // 2 AC
//        bt.insert( ad ); // 3 AD
//        bt.insert( af ); // 5 AF
//        bt.insert( ag ); // 6 AG
//        bt.insert( bc ); // 7 BC
//        bt.insert( bd ); // 8 BD
//        bt.insert( be ); // 9 BE
//        bt.insert( bg ); // 11 BG
//        bt.insert( cd ); // 12 CD
//        bt.insert( ce ); // 13 CE
//        bt.insert( cf ); // 14 CF
//        bt.insert( de ); // 16 DE
//        bt.insert( df ); // 17 DF
//        bt.insert( dg ); // 18 DG
//        bt.insert( ef ); // 19 EF
//        bt.insert( eg ); // 20 EG
//        bt.insert( fg ); // 21 FG
//        
//        bt.dumpTree(System.out);
    }
}

//------------------------------------------------------------------------------
class DoubleMap<R, C, V>
{
    private final Map<R, Map<C, V>> backingMap;

    public DoubleMap()
    {
        this.backingMap = new HashMap<>();
    }

    public V get(R row, C column)
    {
        Map<C, V> innerMap = backingMap.get(row);
        if(innerMap == null)
            return null;
        else
            return innerMap.get(column);
    }

    public void put(R row, C column, V value)
    {
        Map<C, V> innerMap = backingMap.get(row);
        if(innerMap == null)
        {
            innerMap = new HashMap<>();
            backingMap.put(row, innerMap);
        }
        innerMap.put(column, value);
    }
}