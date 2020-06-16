/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.cluster;

import com.hazelcast.map.MapStore;
import eu.discoveri.predikt.graph.SentenceNode;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreMS implements MapStore<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double>
{


    /**
     * Constructor.
     * 
     * @param conn 
     */
    public QRscoreMS( Connection conn )
    {
        this.conn = conn;
    }
    
    @Override
    public synchronized void delete( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey )
    {
        System.out.println("*** Deleting key: " +nodeNodeKey.getKey()+ "-" +nodeNodeKey.getValue());
        try
        {
            
        }
    }
    
    @Override
    public synchronized void deleteAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){}
    
    @Override
    public synchronized void store( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey, Double score ){}
    
    @Override
    public synchronized void storeAll( Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double> map ){}
    
    @Override
    public synchronized Double load( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey ){ return null;}
    
    @Override
    public synchronized Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double> loadAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){ return null; }
    
    @Override
    public Iterable<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> loadAllKeys(){ return null; }
}
