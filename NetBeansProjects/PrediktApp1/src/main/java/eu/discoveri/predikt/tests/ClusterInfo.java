/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.sentences.CorpusProcessDb;
import org.neo4j.ogm.session.Session;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ClusterInfo
{
    public static void main(String[] args)
    {
        // GdB session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
//        CorpusProcessDb.dumpClusterInfo(sess);
//        CorpusProcessDb.dumpClusterSents(sess);
//        CorpusProcessDb.dumpClusterLemmas(sess);
        CorpusProcessDb.dumpClusterSentCount(CorpusProcessDb.getClusterSentences(sess));
        discSess.close();
    }
}
