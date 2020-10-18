/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class AbstractEdge extends GraphEntity implements Edge
{
    private Vertex    n1 = null;
    private Vertex    n2 = null;
    
    /**
     * Constructor.
     * 
     * @param name
     * @param namespace
     * @param n1
     * @param n2 
     */
    public AbstractEdge( String name, String namespace, Vertex n1, Vertex n2 )
    {
        super(name,namespace);
        this.n1 = n1;
        this.n2 = n2;
    }
    
    /**
     * Constructor.
     */
    public AbstractEdge()
    {
        this("","eu.discoveri.predikt",null,null);
    }
    
    /**
     * Get sentence on first end.
     * @return 
     */
    public Vertex getN1() { return n1; }
    /**
     * Get sentence on second end.
     * @return 
     */
    public Vertex getN2() { return n2; }
}
