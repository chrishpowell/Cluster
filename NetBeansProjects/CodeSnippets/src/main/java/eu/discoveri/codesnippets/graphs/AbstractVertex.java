/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graphs;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public abstract class AbstractVertex extends GraphEntity implements Vertex
{
    // Flag vertex as 'visited'.
    private boolean         visited = false;
    // If in a component
    private String          component = "";
    
    // Adjacencies
    @Relationship(type="ADJS")
    private List<Vertex>    adjacencies = new ArrayList<>();
    
    // Adjacencies for all nodes (Neo4j can't do this as it's not an OOdb)
    private List<Adjacency> adjs = new ArrayList<>();
    
    
    /**
     * Constructor.
     * @param name
     * @param namespace 
     */
    public AbstractVertex( String name, String namespace )
    {
        super(name,namespace);
    }
    
    /**
     * Get adjacencies of this vertex
     * @return 
     */
    @Override
    public List<Adjacency> getAdjacencies() { return adjs; }
    
    /**
     * Set adjacencies of this vertex
     * @param adjs 
     */
    @Override
    public void setAdjacencies(List<Adjacency> adjs) { this.adjs = adjs; }
    
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
}
