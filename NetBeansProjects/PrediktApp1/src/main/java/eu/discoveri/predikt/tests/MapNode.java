/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.GraphEntity;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.sentences.CountQR;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
@NodeEntity
public class MapNode extends GraphEntity
{
    @Id @GeneratedValue
    private Long    nid;
    
    @Properties
    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>  commonWords = new HashMap<>();
    
    public MapNode(){}
    
    /**
     * Save on session
     * @param ss
     * @return 
     */
    public MapNode persist( MapNodeService ss )
    {
        return ss.createOrUpdate(this);
    }

    /**
     * Delete on session
     * @param ss 
     */
    public void delete( MapNodeService ss )
    {
        ss.delete(getNid());
    }
    
    /**
     * Find a SentenceNode via nid
     * @param nid
     * @param ss
     * @return 
     */
    public MapNode find( MapNodeService ss, Long nid )
    {
        return ss.find(nid);
    }
    
 
    public Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>> getMap() { return commonWords; }
    public void setMap(Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>> cw  ) { this.commonWords = cw; }
}
