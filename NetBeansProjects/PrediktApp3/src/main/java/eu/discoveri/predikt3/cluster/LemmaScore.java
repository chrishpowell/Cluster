/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.cluster;

import java.util.List;


/**
 * Lemma scoring.
 * @author chrispowell
 */
public class LemmaScore
{
    private final String  term;
    private double        score;

    public LemmaScore(String term, double score)
    {
        this.term = term;
        this.score = score;
    }

    public String getTerm() { return term; }
    public void setScore( double score ) { this.score = score; }
    public double getScore() { return score; }
    
    /**
     * Return double array of scores.
     * 
     * @param lls
     * @return 
     */
    public static double[] toScoreArray( List<LemmaScore> lls )
    {
        return lls.stream().mapToDouble(m -> m.getScore()).toArray();
    }
}
