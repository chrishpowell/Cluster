/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;

import eu.discoveri.predikt3.config.Constants;
import java.util.UUID;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public abstract class GraphEntity
{
    // Node id
    private int     nid = -1;
    
    // Unique ref.
    private UUID    uuid;
    private String  sUUID;
    
    // Namespace and name
    private String  namespace;
    private String  name;
    

    /**
     * Constructor (generally for non-persisted Sentence).
     * @param name
     * @param namespace 
     */
    public GraphEntity( String name, String namespace )
    {
        this.name = name;
        this.namespace = namespace;
        this.uuid = UUID.nameUUIDFromBytes((namespace+name).getBytes());
        this.sUUID = uuid.toString();
    }
    
    /**
     * Constructor (generally for non-persisted Sentence).  Uses default namespace.
     * @param name 
     */
    public GraphEntity( String name )
    {
        this(name,Constants.DEFNS);
    }
    
    /**
     * Constructor (generally for persisted Sentence).
     * @param nid
     * @param name
     * @param namespace 
     */
    public GraphEntity( int nid, String name, String namespace )
    {
        this.nid = nid;
        this.name = name;
        this.namespace = namespace;
        this.uuid = UUID.nameUUIDFromBytes((namespace+name).getBytes());
        this.sUUID = uuid.toString();
    }
    
    /**
     * Constructor (generally for persisted Sentence).  Uses default namespace.
     * @param nid
     * @param name 
     */
    public GraphEntity( int nid, String name )
    {
        this(nid,name,Constants.DEFNS);
    }
    
    /**
     * Constructor.
     * Default values.
     */
//    public GraphEntity(){}
    
    
    /*
     * Getters
     */
    public int getNid() { return nid; }
    public void setNid( int nid ) { this.nid = nid; }
    public UUID getUUID() { return uuid; }
    /**
     * String version UUID
     * @return 
     */
    public String getSUUID(){ return sUUID; }

    public String getNamespace() { return namespace; }
    public String getName() { return name; }
    
    public String getNameNamespace() { return namespace+name; }
}
