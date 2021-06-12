/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

/**
 *
 * @author chrispowell
 */
public class Constants
{
    public static final int         MINSUP = 3;                     // Min number items in a doc for clustering to be considered for this item
    public static final double      MINGSUP = 0.35d;                 // Global supp % min. of item(set) over doc. set
    public static final double      MINCSUPP = 0.7d;               // Cluster supp % min. (% across all docs in Ci in which item appears)
}
