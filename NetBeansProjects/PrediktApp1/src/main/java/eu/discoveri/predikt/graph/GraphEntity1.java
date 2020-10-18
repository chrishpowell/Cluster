/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.config.Constants;
import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
@NodeEntity
public abstract class GraphEntity1
{
    // This can be reused by neo4j
    @Id @GeneratedValue
    private Long    nid;
    
    // Unique ref.
    private UUID    uuid;
    private String  sUUID;
    
    // Namespace and name
    private String  namespace;
    private String  name;
    

    /**
     * Constructor
     * @param name
     * @param namespace 
     */
    public GraphEntity1( String name, String namespace )
    {
        this.name = name;
        this.namespace = namespace;
        this.uuid = UUID.nameUUIDFromBytes((namespace+name).getBytes());
        this.sUUID = uuid.toString();
    }
    
    /**
     * Constructor.  Uses default namespace.
     * @param name 
     */
    public GraphEntity1( String name )
    {
        this(name,Constants.DEFNS);
    }
    
    /**
     * Constructor.
     * Default values.
     */
    public GraphEntity1(){}
    
    
    /*
     * Getters
     */
    public Long getNid() { return nid; }
    public UUID getUUID() { return uuid; }
    /**
     * String version UUID (for Neo4j storage)
     * @return 
     */
    public String getSUUID(){ return sUUID; }

    public String getNamespace() { return namespace; }
    public String getName() { return name; }
    
    public String getNameNamespace() { return namespace+name; }
}
