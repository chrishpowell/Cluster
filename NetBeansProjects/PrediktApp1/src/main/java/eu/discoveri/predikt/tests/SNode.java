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
public class SNode extends GraphEntity
{
    private String  bn;
    
    public SNode( String bn )
    {
        this.bn = bn;
    }
    
    public SNode(){}
    
    public String getBn() { return bn; }
}
