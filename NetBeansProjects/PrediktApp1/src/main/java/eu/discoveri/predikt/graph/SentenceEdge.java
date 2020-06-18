/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.graph.service.SentenceEdgeService;
import org.neo4j.driver.Query;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Transient;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
@RelationshipEntity(type="SIMILARTO")
public class SentenceEdge extends AbstractEdge
{
    // Edge weight.  Default 1, equiv. to no weighting.
    @Property
    private double          weight = 1.0d;
    @Property @Transient
    private boolean         visited = false;
    
        
    /**
     * Constructor.
     * 
     * @param name
     * @param namespace
     * @param weight 
     * @param s1 
     * @param s2 
     */
    public SentenceEdge( String name, String namespace, double weight, SentenceNode s1, SentenceNode s2 )
    {
        super(name,namespace, s1, s2);
        this.weight = weight;
    }
    
    /**
     * Constructor.Default edge weight = 1.
     * 
     * @param name
     * @param namespace 
     * @param s1 
     * @param s2 
     */
    public SentenceEdge( String name, String namespace, SentenceNode s1, SentenceNode s2 )
    {
        this(name,namespace,1.0d,s1,s2);
    }
    
    /**
     * Constructor.
     * Default name (merge s1,s2 names), namespace, weight
     * @param s1
     * @param s2 
     */
    public SentenceEdge( SentenceNode s1, SentenceNode s2 )
    {
        this(s1.getName()+s2.getName(),"eu.discoveri.predikt",s1,s2);
    }
    
    /**
     * Constructor.
     * Default name (merge s1,s2 names), namespace.
     * @param s1
     * @param s2
     * @param weight 
     */
    public SentenceEdge( SentenceNode s1, SentenceNode s2, double weight )
    {
        this(s1.getName()+s2.getName(),"eu.discoveri.predikt",weight,s1,s2);
    }
    
    /**
     * No arg constructor for loading from db
     */
    private SentenceEdge(){}


    /**
     * Create a weighted edge between sentences.
     * @param srcNode
     * @param tgtNode
     * @param weight
     * @return 
     */
    public static Query addEdgeCypher( SentenceNode srcNode, SentenceNode tgtNode, double weight )
    {
        return new Query( "MATCH (src:Sentence {name:$srcName}) MATCH (tgt:Sentence {name:$tgtName}) MERGE (src)-[:SIMILARTO {weight:$weight}]-(tgt)",
                          parameters("srcName",srcNode.getName(),"tgtName",tgtNode.getName(),"weight",weight)  );
    }

    /**
     * Save on session
     * @param ss
     * @return 
     */
    public SentenceEdge persist( SentenceEdgeService ss )
    {
        return ss.createOrUpdate(this);
    }
    
    /**
     * Delete on session
     * @param ss 
     */
    public void delete( SentenceEdgeService ss )
    {
        ss.delete(getNid());
    }
    
    /**
     * Find a SentenceNode via nid
     * @param nid
     * @param ss
     * @return 
     */
    public SentenceEdge find( SentenceEdgeService ss, Long nid )
    {
        return ss.find(nid);
    }

    /**
     * Get edge weight
     * @return 
     */
    public double getWeight() { return weight; }

    /**
     * Edge already been visited this session?
     * @return 
     */
    public boolean isVisited() { return visited; }
    /**
     * Has edge been visited this session?
     * @param visited 
     */
    public void setVisited(boolean visited) { this.visited = visited; }
    
    /**
     * Hash.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + getUUID().hashCode();
        return hash;
    }

    /**
     * Equals.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        final SentenceEdge other = (SentenceEdge) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getNameNamespace().equals(other.getNameNamespace());
    }
    
    /**
     * Simple output.
     * @return 
     */
    @Override
    public String toString()
    {
        return getN1().getName()+":"+getN2().getName()+ "("+weight+")";
    }
}