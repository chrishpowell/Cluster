/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 */
public interface Vertex
{
    /**
     * Get the name of this Vertex.
     * @return 
     */
    public String getName();
    
    /**
     * Get the adjacencies for this vertex.
     * @return 
     */
    public List<Adjacency> getAdjacencies();
    
    /**
     * Set the adjacencies on a vertex.  All in one go is effected by GraphUtils
     * method.
     * @param adjs 
     */
    public void setAdjacencies(List<Adjacency> adjs);
    
    /**
     * Has this vertex been visited?  Used for analysis purposes.
     * @return 
     */
    public boolean isVisited();
    
    /**
     * Flag vertex as having been 'visited'.  Used for analysis purposes.
     */
    public void setVisited();
    
    /**
     * Set visited status of vertex.
     * @param v 
     */
    public void setVisited( boolean v );
    
    /**
     * Get component name (may be null).
     * @return 
     */
    public String getComponent();
    
    /**
     * Set component name (can be null)
     * @param component 
     */
    public void setComponent(String component);
    
    public long getLouvainIdx();
    public void setLouvainIdx(long louvainIdx);

    /**
     * toString of this Vertex.
     * @return 
     */
    @Override
    public String toString();
}
