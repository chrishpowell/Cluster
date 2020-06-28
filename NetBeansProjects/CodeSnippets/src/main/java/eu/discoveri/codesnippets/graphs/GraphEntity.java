/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graphs;

import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public abstract class GraphEntity
{
    // This can be reused by neo4j
    @Id @GeneratedValue
    private Long    nid;
    
    // Unique ref.
    private UUID    uuid;
    
    // Namespace and name
    private String  namespace;
    private String  name;
    

    /**
     * Constructor
     * @param name
     * @param namespace 
     */
    public GraphEntity( String name, String namespace )
    {
        this.name = name;
        this.namespace = namespace;
        this.uuid = UUID.nameUUIDFromBytes((namespace+name).getBytes());
    }
    
    /**
     * Constructor.
     * Default values.
     */
    public GraphEntity()
    {
        this( "","eu.discoveri" );
    }
    
    public Long getNid() { return nid; }
    public UUID getUUID() { return uuid; }

    public String getNamespace() { return namespace; }
    public String getName() { return name; }
    
    public String getNameNamespace() { return namespace+name; }
}
