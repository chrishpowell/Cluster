/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.GraphEntity;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Cx extends GraphEntity
{
    private double  cxVal;
    
    public Cx( double cxVal )
    {
        super("");
        this.cxVal = cxVal;
    }
    
    public Cx(){}
    
    public double getVal() { return cxVal; }
}
