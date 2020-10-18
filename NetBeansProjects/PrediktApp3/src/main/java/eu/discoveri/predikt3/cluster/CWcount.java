/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.cluster;

import eu.discoveri.predikt3.graph.GraphEntity;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CWcount extends GraphEntity
{
    private String  matchedWord;
    private int     countQ,                                 // Count of matched word sentence Q
                    countR;                                 // Count sentence R

    /**
     * Constructor.
     * @param matchedWord
     * @param countQ
     * @param countR 
     */
    public CWcount( String matchedWord, int countQ, int countR )
    {
        super("","");
        this.matchedWord = matchedWord;
        this.countQ = countQ;
        this.countR = countR;
    }

    /**
     * Mutators.
     * @return 
     */
    public String getMatchedWord() { return matchedWord; }
    public void setMatchedWord(String matchedWord) { this.matchedWord = matchedWord; }

    public int getCountQ() { return countQ; }
    public void setCountQ(int countQ) { this.countQ = countQ; }

    public int getCountR() { return countR; }
    public void setCountR(int countR) { this.countR = countR; }
}
