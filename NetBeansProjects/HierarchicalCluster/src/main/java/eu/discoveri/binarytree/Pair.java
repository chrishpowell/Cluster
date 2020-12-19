/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

/**
 * Pair/sim.score constitutes leaf node of hierarchy cluster.
 * 
 * @author chrispowell
 */
public class Pair implements Comparable<Pair>
{
    private final int       doc0, doc1;
    private final double    cosScore;
    private boolean         flagRemove = false;

    /**
     * Constructor.
     * @param doc0
     * @param doc1
     * @param cosScore 
     */
    public Pair(int doc0, int doc1, double cosScore)
    {
        this.doc0 = doc0;
        this.doc1 = doc1;
        this.cosScore = cosScore;
    }

    public int getDoc0() { return doc0; }
    public int getDoc1() { return doc1; }
    public double getCosScore() { return cosScore; }
    public boolean getFlagRemove() { return flagRemove; }
    public void setFlagRemove() { flagRemove = true; }
    
    @Override
    public int compareTo( Pair p )
    {
        if( getCosScore() > p.getCosScore() )
            return 1;
        if( getCosScore() < p.getCosScore() )
            return -1;
        
        return 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 23 * hash + this.doc0;
        hash = 23 * hash + this.doc1;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final Pair other = (Pair) obj;
        return this.doc0 == other.doc0 && this.doc1 == other.doc1;
    }

    @Override
    public String toString()
    {
        return "[" +doc0+ "]:[" +doc1+ "]> " +cosScore;
    }
}
