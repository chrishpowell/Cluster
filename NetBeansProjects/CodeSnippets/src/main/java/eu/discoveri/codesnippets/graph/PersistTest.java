/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graph;

import eu.discoveri.codesnippets.graphs.SentenceTokenNode;
import eu.discoveri.codesnippets.graphs.SentenceTokenNodeService;

import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class PersistTest
{
    public static void main(String[] args)
    {
                // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        // Db service
        SentenceTokenNodeService sns = new SentenceTokenNodeService();

        // Clear database
        sess.purgeDatabase();
        
        // Store a node
        SentenceTokenNode sn = new SentenceTokenNode("S6","The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue.");
        sn.persist(sns);
        
        // Close
        System.out.println("");
        discSess.close();
    }
}
