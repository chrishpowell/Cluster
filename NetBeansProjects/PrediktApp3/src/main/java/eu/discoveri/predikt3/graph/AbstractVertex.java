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
public abstract class AbstractVertex extends GraphEntity implements Vertex
{
    // Flag vertex as 'visited'.
    private boolean         visited = false;
    // If in a component/cluster
    private String          component = "";
    // Clustering
    private long            louvainIdx;


    /**
     * Constructor (generally for non-persisted Sentence).
     * @param name
     * @param namespace 
     */
    public AbstractVertex( String name, String namespace )
    {
        super(name,namespace);
    }
    
    /**
     * Constructor (generally for persisted Sentence).
     * @param nid
     * @param name
     * @param namespace 
     */
    public AbstractVertex( int nid, String name, String namespace )
    {
        super(nid,name,namespace);
    }
    
    /**
     * Has this vertex been visited?
     * @return 
     */
    @Override
    public boolean isVisited() { return visited; }
    
    /**
     * Set vertex as visited.
     */
    @Override
    public void setVisited() { visited = true; }

    /**
     * Get the component name.
     * @return 
     */
    @Override
    public String getComponent() { return component; }

    /**
     * Set the component name.
     * @param component 
     */
    @Override
    public void setComponent(String component) { this.component = component; }
    
    /**
     * Set visited status of vertex.
     * @param v 
     */
    @Override
    public void setVisited( boolean v ) { visited = v; }

    /**
     * Get Louvain algo index.
     * @return 
     */
    @Override
    public long getLouvainIdx() { return louvainIdx; }
    /**
     * Set Louvain algo index.
     * @param louvainIdx 
     */
    @Override
    public void setLouvainIdx(long louvainIdx) { this.louvainIdx = louvainIdx; }
}
