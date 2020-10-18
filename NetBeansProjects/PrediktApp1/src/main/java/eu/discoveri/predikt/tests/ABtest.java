/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphUtils;
import java.util.Map;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ABtest
{
    public static void main(String[] args)
    {
        // Session
        System.out.println("... Setup graph database");
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();

        // Clear database nodes and edges
        System.out.println("... Purge db");
        sess.purgeDatabase();
        
        // Drop and create indexes
        System.out.println("    > Drop and create indexes");
        GraphUtils.dropIndexes(sess);
//        GraphUtils.indexSN( sess );
        
        ANode an0 = new ANode("A0");
        an0.persist(sess);
        
        Iterable<ANode> ani = sess.query(ANode.class,"match (a:ANode) where a.name=$name return a", Map.of("name","A0"));
        for( ANode an: ani )
        {
            an.addBNode("fred");
            an.persist(sess);
        }
        
        discSess.close();
    }
}
