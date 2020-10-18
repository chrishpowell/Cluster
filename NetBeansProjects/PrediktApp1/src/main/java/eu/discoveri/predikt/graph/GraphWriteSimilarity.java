/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.cluster.QRscoreCW;
import eu.discoveri.predikt.config.Constants;

import java.util.List;
import java.util.concurrent.Callable;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;


/**
 * Write scores (qrscw) in a thread.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GraphWriteSimilarity implements Callable
{
    private final int               tnum;
    private final List<ScoreNSim>   snsim;
    private static int              louvainIdx = 0;
    
    /**
     * Constructor.
     * @param tnum Thread num.
     * @param snsim
     */
    public GraphWriteSimilarity( int tnum, List<ScoreNSim> snsim )
    {
        this.tnum = tnum;
        this.snsim = snsim;
    }
    
    /**
     * Do the deed.
     */
    @Override
    public Integer call()
            throws Exception
    {
        // Get a new session
        Session sess = DiscoveriSessionFactory.getInstance().getNewSession();
        
        // Write the batch
        try( Transaction tx = sess.beginTransaction() )
        {
            for( int tries=Constants.THREADRETRIES; tries>0; tries-- )
            {
                try
                {
                    snsim.forEach( ss -> {
                        // Louvain index (determines sentences in a cluster)
                        SentenceNode sn0 = ss.getLsn().get(0), sn1 = ss.getLsn().get(1);
                        sn0.setLouvainIdx(++louvainIdx);
                        sn1.setLouvainIdx(++louvainIdx);
                        sess.save(sn0,Constants.DEPTH_ENTITY); sess.save(sn1,Constants.DEPTH_ENTITY);
                        
                        // Similarity (SIMILARTO) edge score
                        SentenceEdge se = new SentenceEdge(sn0,sn1,ss.getQrscw().getScore());
                        sess.save(se,Constants.DEPTH_ENTITY);
                    });

                    tx.commit();
                    break;
                }
                catch( RuntimeException rex )
                {
                    System.out.println("   ===> " +rex.getMessage()+ ", re-trying");
                    Thread.sleep( Constants.THREADPAUSEMSECS );
                }
            }
        }
        
        // Close the session
        // There is NO Session close!!
        return tnum;
    }
}
