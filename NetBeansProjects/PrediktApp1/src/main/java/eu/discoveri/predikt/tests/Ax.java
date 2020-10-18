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
public class Ax extends GraphEntity
{
    private int     axVal;
    
    public Ax( int axVal )
    {
        super("");
        this.axVal = axVal;
    }
    
    /**
     * No arg constructor for neo4j
     */
    public Ax(){}
}
