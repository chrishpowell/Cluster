/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class PaginationBasic
{
    static int SKIP0 = 0, SKIP1 = 0, LIMIT = 8, PCNT = 0, OUTCNT = 0, INCNT = 0;
    public static void main(String[] args)
    {
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        while( true )
        {
            // Get a bunch of SentenceNodes (Qs)
            Iterable<SentenceNode> isnQ = sess.loadAll(SentenceNode.class, new Pagination(SKIP0,LIMIT));
            System.out.println("Iterable size outer: " +((Collection<?>)isnQ).size());

            /*
             * Sort sentences by tokens' count (long to short hence s2 - s1)
             * Allows sentence matching to work efficiently/properly
             * @TODO:   HMMM...  What if lemmas exceeds tokens (blank/null lemmas in Token)?
             */
            // Default sorted() is on SentenceNode token list size. Supplier: to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<SentenceNode>> isnQs = () -> StreamSupport.stream(isnQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            {
                isnQs.get().forEach(snQ ->
                {
                    System.out.println("==> " +snQ.getName());
                    while( true )
                    {
                        // Get a bunch of SentenceNodes (Rs)
                        Iterable<SentenceNode> isnR = sess.loadAll(SentenceNode.class, new Pagination(SKIP1,LIMIT));
                        System.out.println("Iterable size inner: " +((Collection<?>)isnQ).size());
                        
                        // Default sorted() is on SentenceNode token list size.  Supplier: to obviate Stream closing (can only "use" Stream once)
                        Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(isnR.spliterator(),false).sorted();
                        
                        // Loop over sentenceR set
                        if( isnRs.get().iterator().hasNext() )
                        {
                            isnRs.get().forEach(snR ->
                            {
                                System.out.println("   --> " +snR.getName());
                            });
                        }
                        else
                        {
                            SKIP1 = 0;
                            break;
                        }
                        
                        ++SKIP1;
                    }
                });
            }
            else
                break;

            ++SKIP0;
        }
    }
}
