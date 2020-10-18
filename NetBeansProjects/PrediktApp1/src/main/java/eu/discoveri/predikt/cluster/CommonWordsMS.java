/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.cluster;

import com.hazelcast.map.MapStore;

import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.CommonWordsService;
import eu.discoveri.predikt.graph.service.QRscoreService;
import eu.discoveri.predikt.sentences.CountQR;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CommonWordsMS implements MapStore<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>
{
    private CommonWordsService cws;
    
    /**
     * Constructor.
     * 
     * @param conn 
     */
    public CommonWordsMS( CommonWordsService cws )
    {
        this.cws = cws;
    }
    
    @Override
    public synchronized void delete( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey ){}
    
    @Override
    public synchronized void deleteAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){}
    
    @Override
    public synchronized void store( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey, Map<String,CountQR> counts ){}
    
    @Override
    public synchronized void storeAll( Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>> map ){}
    
    @Override
    public synchronized Map<String,CountQR> load( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey ){ return null;}
    
    @Override
    public synchronized Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>> loadAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){ return null; }
    
    @Override
    public Iterable<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> loadAllKeys(){ return null; }
}
