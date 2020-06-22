/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigBuilder;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.config.MapStoreConfig;
import eu.discoveri.predikt.cluster.CommonWordsMS;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreService;
import eu.discoveri.predikt.cluster.QRscore;
import eu.discoveri.predikt.cluster.QRscoreMS;
import eu.discoveri.predikt.sentences.CountQR;

import java.util.AbstractMap;
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
    private static Config cfg = new Config("qrScore");
    private static HazelcastInstance hi = Hazelcast.newHazelcastInstance(cfg);
    // Store backed map
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Iterable<QRscore>>  qrScore = hi.getMap("qrScore");
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>  commonWords = hi.getMap("commonwords");
    
    /**
     * Hazelcast config (Db backed maps)
     * @param mapNames
     * @return 
     */
    public static Config discoveriConfig( List<String> mapNames )
    {
        MapStoreConfig ms1 = new MapStoreConfig(), ms2 = new MapStoreConfig();
        ms1.setImplementation(new QRscoreMS(new QRscoreService()));
        ms2.setImplementation(new CommonWordsMS());
        
        ConfigBuilder cfgBuilder = new XmlConfigBuilder();
        Config cfg = cfgBuilder.build();
        
        return cfg;
    }

        
    public static void main(String[] args)
    {
        String name = "S*", namespace = "eu.discoveri.predikt";
        System.out.println("*** Getting key: " +name+ "-" +namespace);

        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        
        System.out.println("Num nodes: " +sess.countEntitiesOfType(SentenceNode.class));
        
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

        // Neo4j backed map
        QRscoreMS qrsMS = new QRscoreMS( new QRscoreService() );

        // Filter version
        Filter filter = new Filter("name",ComparisonOperator.LIKE,name);
        Iterable<SentenceNode> sns = sess.loadAll(SentenceNode.class, filter);
        sns.forEach(s1 -> {
            System.out.println(" Filter>" +s1.getName()+ ", UUID: " +s1.getSUUID());
            sns.forEach(s2 -> {
                System.out.println("    Inner: " +s2.getName()+ ", UUID: " +s2.getSUUID());
                if( !s1.equals(s2) )
                {
                    qrsMS.store( new AbstractMap.SimpleEntry<>(s1,s2), List.of(new QRscore(s1,s2,s1.getScore()*s2.getScore())) );
                }
            });
        });
        
        
        discSess.close();
    }
}
