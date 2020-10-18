/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigBuilder;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.map.IMap;
import eu.discoveri.predikt.cluster.CommonWordsMS;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreService;
import eu.discoveri.predikt.cluster.QRscore;
import eu.discoveri.predikt.cluster.QRscoreMS;
import eu.discoveri.predikt.graph.service.CommonWordsService;
import eu.discoveri.predikt.sentences.CountQR;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GetSentences
{
        // Hazelcast mapping
    private static Config dCfg = discoveriConfig();
    private static HazelcastInstance hi = Hazelcast.newHazelcastInstance(dCfg);
    // Store backed map
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>>  qrScore = hi.getMap("qrScore");
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>  commonWords = hi.getMap("commonwords");
    
    /**
     * Hazelcast config (Db backed maps).  Convoluted way of getting MapStoreConfig
     * into (HZC) Config. See HZC Class Config and xmlConfigBuilder.
     * @return 
     */
    public static Config discoveriConfig()
    {
        MapStoreConfig msCfg = new MapStoreConfig(), msCfg1 = new MapStoreConfig();
        // Two maps method?
        msCfg.setImplementation(new QRscoreMS(new QRscoreService())).setClassName("eu.discoveri.predikt.cluster.QRscoreMS");
        msCfg1.setImplementation(new CommonWordsMS(new CommonWordsService()));
    
        Config cfg = new Config("qrScore");
        MapConfig mapCfg = new MapConfig("qrCount");
        mapCfg.setMapStoreConfig(msCfg);
        cfg.addMapConfig(mapCfg);
        
        return cfg;
    }

        
    public static void main(String[] args)
    {
        String name = "S*", namespace = "eu.discoveri.predikt";
        System.out.println("*** Getting key: " +name+ "-" +namespace);

        // Session for Neo4j
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        System.out.println("Num SentenceNodes: " +sess.countEntitiesOfType(SentenceNode.class));
        
        System.out.println("===> Name: " +hi.getName());
        Map<String,MapConfig> dms = dCfg.getMapConfigs();
        dms.forEach((n,m) -> {
            System.out.println("--> MapConfig: " +n);
            System.out.println("   MapStoreConfig: " +m.getMapStoreConfig().getClassName());
            });

        IMap<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>> qMap = hi.getMap("qrCount");
        Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>> qMap1 = new HashMap<>();
        
                
//        Collection<DistributedObject> dObjs = hi.getDistributedObjects();
//        dObjs.forEach(dObj -> System.out.println("===> dObj: " +dObj.getName()));
        
//
//... DOESN'T WORK!!
//        Result res = sess.query( "MATCH (s:SentenceNode {name: $name, namespace: $namespace}) RETURN s", Map.of("name",name, "namespace",namespace) );
//        Iterator<Map<String,Object>> iter = res.iterator();
//        while( iter.hasNext() )
//        {
//            Map<String,Object> sn = iter.next();
//            System.out.println(">> " +sn.keySet());
//        }
        
        // Cypher version
//        Iterable<SentenceNode> snss = sess.query( SentenceNode.class, "MATCH (s:SentenceNode {name: $name, namespace: $namespace}) RETURN s", Map.of("name",name, "namespace",namespace) );
////        Iterable<SentenceNode> snss = sess.query( SentenceNode.class, "MATCH (a:SentenceNode) RETURN a", Collections.EMPTY_MAP);
//        snss.forEach(s -> {
//            System.out.println(" Cypher>" +s.getName()+ ", score: " +s.getScore());
//        });

//        // Neo4j backed map
//        QRscoreMS qrsMS = new QRscoreMS( new QRscoreService() );
//
        // Filter version
        Filter filter = new Filter("name",ComparisonOperator.LIKE,name);
        Iterable<SentenceNode> sns = sess.loadAll(SentenceNode.class, filter);
        sns.forEach(s1 -> {
            sns.forEach(s2 -> {
                if( !s1.equals(s2) )
                {
                    System.out.println(" Outer: "+s1.getName()+", Inner: " +s2.getName());
                    qMap.put( new AbstractMap.SimpleEntry<>(s1,s2), List.of(new QRscore(s1,s2,s1.getScore()*s2.getScore())) );
                }
            });
        });
        
        Hazelcast.shutdownAll();
        discSess.close();
    }
}
