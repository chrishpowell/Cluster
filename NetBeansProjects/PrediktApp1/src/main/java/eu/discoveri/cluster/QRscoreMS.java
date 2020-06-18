/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.cluster;

import com.hazelcast.map.MapStore;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreService;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.neo4j.ogm.model.Result;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreMS implements MapStore<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double>
{
    private QRscoreService qrss;

    /**
     * Constructor.
     * 
     * @param conn 
     */
    public QRscoreMS( QRscoreService qrss )
    {
        this.qrss = qrss;
    }
    
    /**
     * Delete QRscore (Map) entry.
     * @param nodeNodeKey 
     */
    @Override
    public synchronized void delete( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey )
    {
        System.out.println("*** Deleting key: " +nodeNodeKey.getKey()+ "-" +nodeNodeKey.getValue());
        qrss.deleteByCypher( "MATCH (q:QRscore {node1: $node1, node2: $node2})", Map.of("node1",nodeNodeKey.getKey(), "node2",nodeNodeKey.getValue()) );
    }
    
    /**
     * Delete all QRscore (Map) entries.
     * @param nodeNodeKeys 
     */
    @Override
    public synchronized void deleteAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys )
    {
        throw new UnsupportedOperationException("deleteAll");
    }
    
    /**
     * Persist a new QRscore (Map) entry.
     * @param nodeNodeKey
     * @param score 
     */
    @Override
    public synchronized void store( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey, Double score ){}
    
    /**
     * Store all?
     * @param map 
     */
    @Override
    public synchronized void storeAll( Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double> map )
    {
        throw new UnsupportedOperationException("storeAll");
    }
    
    /**
     * Load QRscore values.
     * @param nodeNodeKey
     * @return score of this key.
     */
    public synchronized List<Double> getScore( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey )
    {
        List<Double> ld = new ArrayList<>();
        
       Result res = qrss.findByCypher("MATCH (q:QRscore {node1: $node1, node2: $node2})", Map.of("node1",nodeNodeKey.getKey(), "node2",nodeNodeKey.getValue()));
       res.forEach(k -> {
            k.forEach((a,b)->{System.out.println(a);});
       });

       return ld;
    }
    
    /**
     * Load all QRscore entities (Map) 
     * @param nodeNodeKeys
     * @return 
     */
    @Override
    public synchronized Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double> loadAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){ return null; }
    
    /**
     * Load all keys of QRscores.
     * @return 
     */
    @Override
    public Iterable<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> loadAllKeys(){ return null; }
}
