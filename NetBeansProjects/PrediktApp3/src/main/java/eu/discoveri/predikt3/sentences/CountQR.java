/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.sentences;

import java.io.Serializable;

/**
 * Token count over sentence pairs.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CountQR implements Serializable
{
    private int             q = 0, r = 0;
    private final String    qName, rName;

    /**
     * Constructor.
     * 
     * @param qName Source sentence token/word
     * @param q Count in q
     * @param rName Target sentence token/word
     * @param r Count in r
     */
    public CountQR( String qName, int q, String rName, int r )
    {
        this.q = q;
        this.r = r;
        this.qName = qName;
        this.rName = rName;
    }

    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }

    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    public String getqName() { return qName; }
    public String getrName() { return rName; }
}
