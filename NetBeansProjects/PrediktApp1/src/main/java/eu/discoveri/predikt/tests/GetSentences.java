/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GetSentences
{
    public static void main(String[] args)
    {
        String name = "S1", namespace = "eu.discoveri.predikt";
        System.out.println("*** Getting key: " +name+ "-" +namespace);

        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        
        System.out.println("Num nodes: " +sess.countEntitiesOfType(SentenceNode.class));
        
//        Result res = sess.query( "MATCH (s:SentenceNode {name: $name, namespace: $namespace}) RETURN s", Map.of("name",name, "namespace",namespace) );
        Result res = sess.query( "MATCH (a:SentenceNode) RETURN a", Collections.EMPTY_MAP);

        Iterator<Map<String,Object>> iter = res.iterator();
        while( iter.hasNext() )
        {
            Map<String,Object> sn = iter.next();
            System.out.println(">> " +sn.keySet());
        }
        
        discSess.close();
    }
}
