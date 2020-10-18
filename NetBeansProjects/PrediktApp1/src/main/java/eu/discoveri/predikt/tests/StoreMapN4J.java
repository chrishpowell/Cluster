/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;


import eu.discoveri.predikt.cluster.CWcount;
import eu.discoveri.predikt.cluster.QRscore;
import eu.discoveri.predikt.cluster.QRscoreCW;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphUtils;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreCWService;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import eu.discoveri.predikt.sentences.Nul;
import eu.discoveri.predikt.sentences.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.neo4j.ogm.cypher.query.Pagination;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class StoreMapN4J
{
    /**
     * Drop all constraints and indexes.
     * @param sess 
     */
    public static void dropIndexes( Session sess )
    {
        // Constraints
        Result res = sess.query("CALL db.constraints", Collections.EMPTY_MAP);
        Iterator<Map<String,Object>> iter = res.iterator();
        while( iter.hasNext() )
        {
            Map<String,Object> sn = iter.next();
            sn.entrySet().forEach(v -> {
                if( v.getKey().equals("name") )
                    sess.query( "drop constraint "+v.getValue(), Collections.EMPTY_MAP );
            });
        }
        
        // Indexes
        res = sess.query("CALL db.indexes", Collections.EMPTY_MAP);
        iter = res.iterator();
        while( iter.hasNext() )
        {
            Map<String,Object> sn = iter.next();
            sn.entrySet().forEach(v -> {
                if( v.getKey().equals("name") )
                    sess.query("drop index "+v.getValue(), Collections.EMPTY_MAP);
            });
        }
    }
    
    public static void indexQRscoreCW( Session sess )
    {
        sess.query("create index qrscwIdx for (q:QRscoreCW) on (q.namespace,q.name)", Collections.EMPTY_MAP);
        sess.query("create constraint on (c:QRscoreCW) assert c.qrscwIdx is unique", Collections.EMPTY_MAP);
    }
    
//    private static long LIMIT = 0, SKIP = 0;
//    public static void paginateSetup( long pageSize )
//    {
//        SKIP -= pageSize;
//        LIMIT = pageSize;
//    }
//    public static synchronized Result findByCypherPaginate( Session sess )
//    {
//        SKIP += LIMIT;
//        System.out.println("SKIP: " +SKIP+ ", LIMIT: " +LIMIT);
//        return sess.query( "MATCH (sn:SentenceNode) RETURN sn ORDER BY sn.name SKIP $skip LIMIT $limit", Map.of("skip", SKIP, "limit", LIMIT) );
//    }

        /**
     * Num. individual sentences in which lemma appears
     * @param sess
     * @return 
     */
    private static Map<String,Long> lemmaPairCount( Session sess )
    {
        Map<String,Long> lemmaCount = new HashMap<>();
        Result result = sess.query( "MATCH (cw:CWcount) RETURN count(cw.matchedWord) as cwc, cw.matchedWord as mw", Collections.EMPTY_MAP );
        result.forEach(entry -> lemmaCount.put((String)entry.get("mw"), (long)entry.get("cwc")));
        
        return lemmaCount;
    }
    
    /**
     * Number sentences
     * @param sess
     * @return 
     */
    private static long sentenceCount( Session sess )
    {
        return sess.countEntitiesOfType(SentenceNode.class);
    }
    
    /**
     * Dump: Sentence list in which lemma appears
     */
    private static void dumpLemmaSentList()
    {
        lemmaSentList.forEach((k,v) -> {
            System.out.println(" k: " +k);
            v.forEach((k1,v1) -> {
                System.out.println("  Sent: " +k1);
            });
        });
    }
    
    /**
     * Dump: Num. sentences in which each lemma appears
     */
    private static void dumpLemmaSentCount()
    {
        lemmaSentCount.forEach((k,v) -> {
            System.out.println("k: " +k+ ", count: " +v);
        });
    }
    
    /**
     * Check if pair already processed.
     * @param keyQ
     * @param keyR 
     */
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
    
    /**
     * See if this sentence pair already processed.
     * @param keyQ
     * @param keyR
     * @return 
     */
    public static boolean isStoredPair(String keyQ, String keyR)
    {
//        if(!noDups.containsKey(keyQ))
//        {
//            System.out.println("::> keyQ: " +keyQ+ " storedQ: false");
//            if(!noDups.containsKey(keyR))
//                System.out.println("    keyR: " +keyR+ " storedQ: false");
//            else
//                System.out.println("    keyR: " +keyR+ " storedQ: true");
//        }
//        else
//        {
//            if( !noDups.get(keyQ).containsKey(keyR) )
//            {
//                System.out.println("::> keyQ: " +keyQ+ " storedQ: true, keyR: " +keyR+ " storedR: false");
//            }
//        }
//        System.out.println("..> isStoredPair: " +(noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)));
        
        return (noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)) || (noDups.containsKey(keyR) && noDups.get(keyR).containsKey(keyQ));
    }
    
    public static boolean checkPair(String keyQ, String keyR)
    {
        return (noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)) || (noDups.containsKey(keyR) && noDups.get(keyR).containsKey(keyQ));
    }
    
    /**
     * 
     * @param wordsQ
     * @param wordsR
     * @param cqr
     * @return 
     */
    public static Map<String,CWcount> lemmaCompare( List<Token> wordsQ, List<Token> wordsR, CWcount cqr )
    {
        // Map lemma to count
        Map<String,CWcount> wCountQR = new HashMap<>();
        
        // For each lemma in sentenceQ (Note: Q lemma count >= R lemma count)
        for( Token tQ: wordsQ )
        {
            String wordQ = tQ.getLemma();       // *MATCH* this lemma
            if( wordQ.equals("") ) continue;    // Ignore blank lemmas

            if( wCountQR.containsKey(wordQ) )   // Is this lemma common between Q&R?
            { // Yes
                cqr = wCountQR.get(wordQ);      // Get current count for this token
                int count = cqr.getCountQ();         // Count in Q
                cqr.setCountQ(++count);              // Increment
                wCountQR.replace(wordQ, cqr);   // Update

                // Already counted all of R (where token key is created below), so skip R
                continue;
            }

            // Match Q token against each lemma in target sentenceR
            for( Token tR: wordsR )
            {
                String wordR = tR.getLemma();   // *MATCH* this lemma

                if( wordR.equals(wordQ) )                               // Words match between sentences?
                { //Yes
                    // First match for token in both Q&R?
                    if( !wCountQR.containsKey(wordQ) )                  // Create Map entry
                    {//Yes
                        wCountQR.put(wordQ, new CWcount(wordQ,1,1));    // Init count of both sentences
                    }
                    else
                    {//No
                        // Ok, get the counts for this lemma
                        cqr = wCountQR.get(wordR);
                        int count = cqr.getCountR();                    // Count for R sentence
                        cqr.setCountR(++count);                         // Increment R count
                        wCountQR.replace(wordR, cqr);                   // Update
                    }
                }
            }
        }
        
        return wCountQR;
    }


    /**
     * M A I N
     * =======
     * @param args 
     */
    static int          cnt = 0;
    static double       score = 0.d;
    private static long LIMIT = 0, SKIP = 0;
    static Map<String,Map<String,Nul>>  lemmaSentList = new HashMap<>();
    static Map<String,Long>             lemmaSentCount = new HashMap<>();
    static Map<String,Map<String,Nul>>  noDups = new HashMap<>();
    static Nul                          nul = new Nul();
    
    static int SKIP0 = 0, SKIP1 = 0;
    static int LIMIT0 = 4, MCNT = 0;
    
    public static void main(String[] args)
    {   
        SentenceNode s1 = new SentenceNode("S103","five and twenty blackbirds");
        SentenceNode s2 = new SentenceNode("S104","ten blackbirds baked in a pie");
        
        // GdB session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        // Indexes
        dropIndexes( sess );
        indexQRscoreCW( sess );
        
        // GdB access
        QRscoreCWService qrscws = new QRscoreCWService();
        SentenceNodeService sns = new SentenceNodeService();

        // Delete all scores
        QRscoreCW.deleteAll(qrscws);

        // Build some objects
//        CWcount cwc = new CWcount("blackbird",1,1);
//        List<CWcount> lcw = new ArrayList<>();
//        lcw.add(cwc);
//        QRscoreCW qrscw= new QRscoreCW(s1,s2,lcw,1.1d);
//        
//        // Persist
//        qrscw.persist(qrscws);
//        
//        // Get all scores
//        Iterable<QRscoreCW> iqrs = qrscw.findByCypher( qrscws, s1, s2 );
//        iqrs.forEach(q -> System.out.println("Score: "+q.getName()));

        // Note autocommit on
//        Map<String,Long> lemmaCount = new HashMap<>();
//        Result result = sess.query( "MATCH (cw:CWcount) RETURN count(cw.matchedWord) as cwc, cw.matchedWord as mw", Collections.EMPTY_MAP );
//        result.forEach( entry -> lemmaCount.put((String)entry.get("mw"), (long)entry.get("cwc")) );
//        lemmaCount.forEach((k,v)->System.out.println(k+":"+v));
//        

        /*
         * Sentence count on db
         */
        long sentCount = sentenceCount(sess);
        System.out.println("Sentence count: " +sentCount);
        
        /*
         * Load sentences with pagination
         */
        while( true )
        {
            // Get a bunch of SentenceNodes (Qs)
            Iterable<SentenceNode> isnQ = sess.loadAll(SentenceNode.class, new Pagination(SKIP0,LIMIT0));
            
            // Default sorted() is on SentenceNode token list size.  Supplier,. to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<SentenceNode>> isnQs = () -> StreamSupport.stream(isnQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            {
                SKIP1 = SKIP0;
                isnQs.get().forEach(snQ -> {
//                    System.out.println("  outer: " +sn0.getName()+ ", SKIP0|SKIP1: " +SKIP0+"|"+SKIP1);

                    // Count of common words in each sentence pair
                    CWcount cqr = null;
                    
                    // Tokens for sentence Q
                    List<Token> wordsQ = snQ.getTokens();

                    while( true )
                    {
//                        System.out.println("\r\n    ...inner chunk (<="+LIMIT0+")...");
                        // Get a bunch of SentenceNodes (Rs)
                        Iterable<SentenceNode> isnR = sess.loadAll(SentenceNode.class, new Pagination(SKIP1,LIMIT0));
                        
                        // Default sorted() is on SentenceNode token list size.  Supplier,. to obviate Stream closing (can only "use" Stream once)
                        Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(isnR.spliterator(),false).sorted();
                        
                        // Loop over sentenceR set
                        if( isnRs.get().iterator().hasNext() )
                        {
                            isnRs.get().forEach(snR -> {
//                                System.out.println("    inner: " +sn1.getName()+ ", SKIP0|SKIP1: " +SKIP0+"|"+SKIP1);
                                String keyQ = snQ.getNameNamespace(), keyR = snR.getNameNamespace();
                    
                                if( !snQ.equals(snR) && !isStoredPair(keyQ,keyR) )
                                {
//                                    System.out.println("     Unmatched: [" +keyQ+"-"+keyR+ "] UMcount: " +(++MCNT));
//                                    System.out.println("     ...Processing unmatched");
                                    // Got a candidate pair
                                    storePairKeys(keyQ,keyR);                   // Flag pair processed
                                    
                                    // ...Now to compare tokens/lemmas
                                                                    
                                    // Tokens for sentence R
                                    List<Token> wordsR = snR.getTokens();
                                    
                                    // Map of count of common words between two sentences <Word,count of Word per sentence pair>
                                    Map<String,CWcount> wCountQR = lemmaCompare(wordsQ,wordsR,cqr);
                                    
                                    // Ok, store the common words for the sentence pairs
                                    if( wCountQR.values().size() > 0 )
                                    {
                                        // Initial score is zero.
                                        QRscoreCW qrscw = new QRscoreCW( snQ,snR, new ArrayList<>(wCountQR.values()) );
                                        qrscw.persist(qrscws);
                                    }
                                }
                            });   // LOOP
                        }
                        else
                        {
//                            System.out.println("    End this inner loop");
                            SKIP1 = 0;
                            break;
                        }

                        ++SKIP1;
                    }
                });
//                System.out.println("... SKIP0: "+SKIP0);
            }
            else
                break;
            
            ++SKIP0;
        }

//        Iterable<SentenceNode> isn0 = sess.loadAll(SentenceNode.class, new Pagination(0,20));
//        Stream<SentenceNode> isnos = StreamSupport.stream(isn0.spliterator(),false).sorted();
//        isnos.forEach(sn -> {
//            System.out.println(""+sn+": "+sn.getTokens().size());
//        });

        

        /*
         * Num. sentences in which lemma appears (from CWcount::matchedWord)
         */
        // Get scores into qrScore
        // for each sentence pair
        // | for each common word in that pair
        // | |   get tot. num. pairs common word appears
        // | |   calc score:
        // | |     get count in sent Q, get count in sent R
        // | |     apply algo
        // | end loop
        // | store similarity score against sentence pair
        // end loop
//        SKIP = -5;
//        LIMIT = 5;
//        while( true )
//        {
//            SKIP += LIMIT;
//            // For each sentence pair get the sentences (for the names) and the list of matched words for each pair
//            // "q" is used only to form the correct collection, q itself is not processed whereas q's List (CWcount) is.
//            // Returns: q:QRscoreCW[0] ["T0","S6" : "matchword"], q[1] [..:..], ...
//            Result result = sess.query("MATCH (q:QRscoreCW)-[:SCORE|CW_COUNT]->(s) RETURN q, collect(s.matchedWord) as mws, collect(s.name) as snames SKIP $skip LIMIT $limit", Map.of("skip",SKIP,"limit",LIMIT));
//
//            // Now determine sentences in which matched word is found
//            if( result.iterator().hasNext() )
//            {
//                result.forEach( entry -> {
//                    // Sentence pair node names @TODO: Add namespace
//                    String[] snames = (String[])entry.get("snames");
//                    
//                    // Matched ('common') words for this pair (CWcount)
//                    Stream<String> mwsl = Arrays.stream((String[])entry.get("mws"));
//                    
//                    // For each matched lemma
//                    mwsl.forEach(word -> {
//                        // Word/lemma appeared before?
//                        if( !lemmaSentList.containsKey(word) )
//                        {// No
//                            Map<String,Nul> nulMap = new HashMap<>();
//                            nulMap.put(snames[0],nul);
//                            nulMap.put(snames[1], nul);
//                            lemmaSentList.put(word, nulMap);
//                        }
//                        else
//                        {// Yes
//                            if( !lemmaSentList.get(word).containsKey(snames[0]) )
//                            {
//                                lemmaSentList.get(word).put(snames[0], nul);
//                            }
//                            if( !lemmaSentList.get(word).containsKey(snames[1]) )
//                            {
//                                lemmaSentList.get(word).put(snames[1], nul);
//                            }
//                        }
//                    });
//                });
//            }
//            else
//                break;
//        }
//
//        System.out.println("---------------> Lemma/sentence list");
//        dumpLemmaSentList();
//        
//        /**
//         * Lemma/sentence count
//         */
//        lemmaSentList.forEach((k,v) -> {
//            lemmaSentCount.put( k, lemmaSentList.get(k).values().stream().count() );
//        });
//        System.out.println("---------------> Lemma/sentence count");
//        dumpLemmaSentCount();
//        
//        /**
//         * Similarity
//         */
//        System.out.println("Similarity....");
//        SKIP = -5;
//        LIMIT = 5;
//        while( true )
//        {
//            SKIP += LIMIT;
//            Result result = sess.query( "MATCH (q:QRscoreCW)-[:CW_COUNT]->(s) RETURN q,collect(s.matchedWord) as mws,collect(s.countQ) as cQ, collect(s.countR) as cR SKIP $skip LIMIT $limit",Map.of("skip",SKIP, "limit",LIMIT));
//            if( result.iterator().hasNext() )
//            {
//                result.forEach( entry -> {
//                    // QRscoreCW
//                    score = 0.d;
//                    QRscoreCW qrs = (QRscoreCW)entry.get("q");
//                    
//                    // Matched words
//                    String[] mwsl = (String[])entry.get("mws");
//                    
//                    // Count in each sentence of sentence pair
//                    Long[] cQl = (Long[])entry.get("cQ");
//                    Long[] cRl = (Long[])entry.get("cR");
//                    
//                    for( int ii=0; ii<mwsl.length; ii++ )
//                        score += Math.log(cQl[ii]+1.d)*Math.log(cRl[ii]+1.d)*Math.log((sentCount+1.d)/(lemmaSentCount.get(mwsl[ii])+0.5d));
//                    
//                    qrs.setScore(score);
////                        sess.save(qrs);
//                    System.out.println("   Score: " +qrs.getName()+": "+score);
//                });
//            }
//            else
//                break;
//        }

        
        // Sentence paginate  USE NEO4J Pagination for loadAll() Eg:
        // Iterable<World> worlds = session.loadAll(World.class,
        //                                          new Pagination(pageNumber,itemsPerPage), depth)
//        GraphUtils.paginateSetup( "MATCH (sn:SentenceNode) RETURN sn ORDER BY sn.sUUID SKIP $skip LIMIT $limit",3 );
//        Result result;
//        while( true )
//        {
//            System.out.println("::> Page: " +cnt++);
//            result = GraphUtils.findByCypherPaginate( sess );
//            if( result.iterator().hasNext() )
//            {
//                result.queryResults().forEach(entry -> {
//                    SentenceNode sn = (SentenceNode)entry.get("sn");
//                    System.out.print(" [" +sn.getName()+ "](" +sn.getNid()+ ")");
//                });
//                System.out.println("");
//            }
//            else
//                break;
//        }
        
        // Get and dump scores
//        MCNT = 0;
//        System.out.println("--------------------------------------------------");
//        noDups.forEach((k,v) -> {
//            System.out.println("keyQ: " +k);
//            v.forEach((k1,v1) -> {
//                System.out.println("  keyQ: " +k+ ", keyR: " +k1+ " -- " +checkPair(k,k1));
//                ++MCNT;
//            });
//        });
//        System.out.println("Expected/Actual Pair count: " +(sentCount*(sentCount-1))/2+"/"+MCNT);
//        System.out.println("--------------------------------------------------");
//        System.out.println("  keyQ: eu.discoveri.prediktS4, keyR: fred -- " +checkPair("eu.discoveri.prediktS4","fred"));
//        System.out.println("  keyQ: fred, keyR: eu.discoveri.prediktS4 -- " +checkPair("fred","eu.discoveri.prediktS4"));
//        System.out.println("--------------------------------------------------");
//        QRscoreCW.dumpDbQRscore(sess, false);
 
        // Close
        discSess.close();
    }
}
