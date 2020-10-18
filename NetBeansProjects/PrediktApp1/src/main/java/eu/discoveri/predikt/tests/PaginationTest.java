/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.sentences.Nul;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
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
public class PaginationTest
{
    private static Map<String,Map<String,Nul>>  noDups = new HashMap<>();
    private static Nul                          nul = new Nul();
    
    public static void storePairKeys(String keyQ, String keyR)
    {
        if( !noDups.containsKey(keyQ) )
        {
            if( !noDups.containsKey(keyR) )
            {
                // Start this sentence pair Q:R
                Map<String,Nul> nulMap = new HashMap<>();
                nulMap.put(keyR,nul); 
                noDups.put(keyQ,nulMap);
            }
            // Next in LOOP
        }
        else
        {
            if( !noDups.get(keyQ).containsKey(keyR) )
            {
                noDups.get(keyQ).put(keyR, nul);
            }
        }
    }
        
    public static boolean isStoredPair(String keyQ, String keyR)
    {
        return (noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)) || (noDups.containsKey(keyR) && noDups.get(keyR).containsKey(keyQ));
    }

    /**
     * M A I N
     * =======
     */
    static int SKIP0 = 0, SKIP1 = 0, LIMIT = 8, PCNT = 0;
    public static void main(String[] args)
    {
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        while( true )
        {
            // Get a bunch of SentenceNodes (Qs)
            Iterable<SentenceNode> isnQ = sess.loadAll(SentenceNode.class, new Pagination(SKIP0,LIMIT));

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
                isnQs.get().forEach(snQ -> {

                    while( true )
                    {
                        // Get a bunch of SentenceNodes (Rs)
                        Iterable<SentenceNode> isnR = sess.loadAll(SentenceNode.class, new Pagination(SKIP1,LIMIT));
                        
                        // Default sorted() is on SentenceNode token list size.  Supplier: to obviate Stream closing (can only "use" Stream once)
                        Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(isnR.spliterator(),false).sorted();
                        
                        // Loop over sentenceR set
                        if( isnRs.get().iterator().hasNext() )
                        {
                            isnRs.get().forEach(snR -> {
                                
                                String keyQ = snQ.getNameNamespace(), keyR = snR.getNameNamespace();
                    
                                // Check to see if we should process this pair
                                if( !snQ.equals(snR) && !isStoredPair(keyQ,keyR) )
                                {
                                    ++PCNT;
                                    // Got a candidate pair, flag being processed
                                    storePairKeys(keyQ,keyR);
                                    System.out.println("Processing: " +keyQ+":"+keyR);
                                }
                            });   // LOOP
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
        
        System.out.println("Process counts: " +PCNT);
        discSess.close();
    }
}
