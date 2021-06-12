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
public class SimScoreData
{
    private final double  score;
    private final int     sumx;
    private final int     sumxdash;

    public SimScoreData(double score, int sumx, int sumxdash)
    {
        this.score = score;
        this.sumx = sumx;
        this.sumxdash = sumxdash;
    }

    public double getScore() { return score; }
    public int getSumx() { return sumx; }
    public int getSumxdash() { return sumxdash; }
}
