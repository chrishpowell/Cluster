/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SentenceCount
{
    public static void main(String[] args)
    {
        int SKIP0 = 0, LIMIT = Constants.PAGSIZE;
        
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        Iterable<SentenceNode> isnQ = sess.loadAll(SentenceNode.class, new Pagination(SKIP0,LIMIT));
        System.out.println("Num. sentences 0: " +isnQ.spliterator().getExactSizeIfKnown());
        isnQ.forEach(sn -> {
            System.out.print("  " +sn.getName());
        });
        System.out.println("");
        
        isnQ = sess.loadAll(SentenceNode.class, new Pagination(++SKIP0,LIMIT));
        System.out.println("Num. sentences 1: " +isnQ.spliterator().getExactSizeIfKnown());
            isnQ.forEach(sn -> {
            System.out.print("  " +sn.getName());
        });
        System.out.println("");
        
        discSess.close();
    }
}
