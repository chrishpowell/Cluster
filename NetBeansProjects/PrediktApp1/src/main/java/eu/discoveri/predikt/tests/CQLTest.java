/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.cluster.CWcount;
import eu.discoveri.predikt.cluster.QRscoreCW;
import eu.discoveri.predikt.exceptions.MissingMatchedWordException;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.QRscoreCWService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CQLTest
{
    private static long    sentCount = 0L;
    
    /**
     * Number sentences to be processed.
     * @return 
     */
    public static long sentenceNodeCount(Session sess)
    {
        sentCount = sess.countEntitiesOfType(SentenceNode.class);
        return sentCount;
    }
    
    /**
     * Num. individual sentences in which lemma appears
     * Neo4J returns long, hence Long in Map.
     * @return 
     */
    static Map<String,Long> lemmaSentCount = new HashMap<>();
    private static Map<String,Long> lemmaSentCount(Session sess)
    {
        Result result = sess.query( "MATCH (cw:CWcount) RETURN count(cw.matchedWord) as cwc, cw.matchedWord as mw", Collections.EMPTY_MAP );
        result.forEach(entry -> lemmaSentCount.put((String)entry.get("mw"), (long)entry.get("cwc")));
        
        return lemmaSentCount;
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    static double score = 0.d;
    public static void main(String[] args)
            throws MissingMatchedWordException
    {
        int LIMIT = 5, SKIP = -5;

        // GdB session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        sentenceNodeCount(sess);
        lemmaSentCount(sess);
        
        QRscoreCWService qrscws = new QRscoreCWService();
        
        // Get scores
        while( true )
        {
            SKIP += LIMIT;
            
            Result result = sess.query( "match (q:QRscoreCW)-[:CWC]->(c) return q,collect(c.matchedWord) as mws,collect(c) as cc SKIP $skip LIMIT $limit", Map.of("skip",SKIP, "limit",LIMIT) );
            Iterator<Map<String,Object>> res = result.iterator();
            if( res.hasNext() )
            {
                while( res.hasNext() )
                {
                    score = 0.d;
                    Map<String,Object> entry = res.next();
                    
                    // QRscoreCW
                    QRscoreCW qrs = (QRscoreCW)entry.get("q");
                    System.out.println("Name: " +qrs.getName());

                    // CWcount
                    List<CWcount> cwcl = (List<CWcount>)entry.get("cc");
                    for( CWcount c: cwcl )
                    {
                        System.out.println("   -> " +c.getMatchedWord()+ ", cQ/cR: " +c.getCountQ()+"/"+c.getCountR());
                        // Just in case
                        if( !lemmaSentCount.containsKey(c.getMatchedWord()) )
                            throw new MissingMatchedWordException(c.getMatchedWord());

                        score += Math.log(c.getCountQ()+1.d)*Math.log(c.getCountR()+1.d)*Math.log((sentCount+1.d)/(lemmaSentCount.get(c.getMatchedWord())+0.5d));
                    }
                    
                    System.out.println("   Score: " +score);
                    qrs.setScore(score);
                    qrscws.createOrUpdate(qrs);
                }
            }
            else
                break;
        }
        
        System.out.println("Closing...");
        discSess.close();
    }
}
