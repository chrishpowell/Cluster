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
public class BNode extends GraphEntity
{
    private String  bn;
    
    public BNode( String bn )
    {
        this.bn = bn;
    }
    
    public BNode(){}
    
    public String getBn() { return bn; }
}
