/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.cluster;

import com.hazelcast.map.MapStore;

import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreService;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreMS implements MapStore<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>>
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
    
    @Override
    public synchronized Iterable<QRscore> load( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey )
    {
        return qrss.findByCypher("MATCH (s:SentenceNode {name: $name, namespace: $namespace}) RETURN s", Map.of("name",nodeNodeKey.getKey().getName(), "namespace",nodeNodeKey.getValue().getName()));
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
     * Persist a new QRscore (Map) entry.  This method will take just the 'first'
     * in the Iterable.
     * @param nodeNodeKey
     * @param scores Annoyingly (HZC) expects an Iterable (because of Iterable
     * returns from Neo4j defined elsewhere).
     */
    @Override
    public synchronized void store( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey, Iterable<QRscore> scores )
    {
        Iterator<QRscore> iqrs = scores.iterator();
        if( iqrs.hasNext() )
            qrss.createOrUpdate(iqrs.next());
    }
    
    /**
     * Store all?  Annoyingly, Iterable<T> should have only one entry.
     * @param map 
     */
    @Override
    public synchronized void storeAll( Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>> map )
    {
        throw new UnsupportedOperationException("storeAll");
    }
    
//    /**
//     * Load QRscore values.
//     * @param nodeNodeKey
//     * @return score of this key.
//     */
//    public synchronized List<Double> getScore( AbstractMap.SimpleEntry<SentenceNode,SentenceNode> nodeNodeKey )
//    {
//        List<Double> ld = new ArrayList<>();
//        
//        Iterable<QRscore> sns = qrss.findByCypher( QRscore.class, "MATCH (q:QRscore {node1: $node1, node2: $node2})", Map.of("node1",nodeNodeKey.getKey(), "node2",nodeNodeKey.getValue()));
//        sns.forEach(k -> {
//            ld.add(k.getScore());
//        });
//
//        return ld;
//    }
    
    /**
     * Load all QRscore entities (Map) 
     * @param nodeNodeKeys
     * @return 
     */
    @Override
    public synchronized Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>> loadAll( Collection<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> nodeNodeKeys ){ return null; }
    
    /**
     * Load all keys of QRscores.
     * @return 
     */
    @Override
    public Iterable<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>> loadAllKeys(){ return null; }
}
