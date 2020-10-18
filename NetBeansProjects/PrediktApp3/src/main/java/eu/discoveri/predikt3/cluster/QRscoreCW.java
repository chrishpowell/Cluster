/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.cluster;

import eu.discoveri.predikt3.graph.GraphEntity;
import eu.discoveri.predikt3.graph.SentenceNode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreCW extends GraphEntity
{
    private SentenceNode            nodeQ;
    private SentenceNode            nodeR;
    // Score
    private double                  score;
    // Count of matched words
    List<CWcount>                   cwCount;


    /**
     * Constructor.
     * @param nodeQ
     * @param nodeR
     * @param cwCount
     * @param score
     */
    public QRscoreCW(SentenceNode nodeQ, SentenceNode nodeR, List<CWcount> cwCount, Double score )
    {
        super(nodeQ.getName()+nodeR.getName(),"eu.discoveri.predikt");
        
        this.nodeQ = nodeQ;
        this.nodeR = nodeR;
        this.cwCount = cwCount;
        this.score = score;
    }
    
    /**
     * Constructor.  Initial score is zero.
     * @param nodeQ
     * @param nodeR
     * @param cwCount
     */
    public QRscoreCW(SentenceNode nodeQ, SentenceNode nodeR, List<CWcount> cwCount )
    {
        this( nodeQ,nodeR, cwCount, 0.d );
    }
    
    /**
     * Constructor.
     * @param nodeQ
     * @param nodeR
     * @param score
     */
    public QRscoreCW(SentenceNode nodeQ, SentenceNode nodeR, Double score )
    {
        this(nodeQ,nodeR,new ArrayList<CWcount>(),score);
    }

    /*
     * Getters
     */
    public SentenceNode getNodeQ() { return nodeQ; }
    public SentenceNode getNodeR() { return nodeR; }
    public Double getScore() { return score; }
    public void setScore( double score ) { this.score = score; }

    public List<CWcount> getCwCount() { return cwCount; }
    public void setCwCount(List<CWcount> cwCount) { this.cwCount = cwCount; }
    
    /**
     * Database methods
     */
    /**
     * Save on session
     * @param qrscws
     * @return 
     */
//    public QRscoreCW persist( QRscoreCWService qrscws )
//    {
//        return qrscws.createOrUpdate(this);
//    }

    /**
     * Delete on session
     * @param qrscws
     */
//    public void delete( QRscoreCWService qrscws )
//    {
//        qrscws.delete(getNid());
//    }
    
    /**
     * Find a Scores via nid
     * @param qrscws
     * @param nid
     * @return 
     */
//    public QRscoreCW find( QRscoreCWService qrscws, Long nid )
//    {
//        return qrscws.find(nid);
//    }
    
    /**
     * Delete all QRscoreCW and their relations.
     * @param qrscws 
     */
//    public static void deleteAll( QRscoreCWService qrscws )
//    {
//        qrscws.findByCypher("match (q:QRscoreCW) optional match (q)-[r]-() delete q,r",Collections.EMPTY_MAP);
//    }
    
    /**
     * Find Scores via Cypher.
     * @param qrscws
     * @param nodeQ
     * @param nodeR
     * @return 
     */
//    public synchronized Iterable<QRscoreCW> findByCypher( QRscoreCWService qrscws, SentenceNode nodeQ, SentenceNode nodeR )
//    {
//        System.out.println("*** Getting Outer: [" +nodeQ.getName()+ "], Inner: [" +nodeR.getName()+"]");
//        return qrscws.findByCypher( "MATCH (q:QRscoreCW {namespace: $namespace, name: $name}) RETURN q", Map.of("namespace",nodeQ.getNamespace(), "name",nodeQ.getName()+nodeR.getName()) );
//    }
    
    /**
     * Find Scores via Cypher.
     * @param qrscws
     * @param nodeQ
     * @param nodeR
     * @return 
     */
//    private static int PAGESIZE = 0, PAGECOUNT = 0;
//    public void paginateSetup( int pageSize )
//    {
//        PAGECOUNT -= pageSize;
//        PAGESIZE = pageSize;
//    }
//    public synchronized Iterable<QRscoreCW> findByCypherPaginate( QRscoreCWService qrscws )
//    {
//        PAGECOUNT += PAGESIZE;
//        return qrscws.findByCypher( "MATCH (q:QRscoreCW) RETURN q ORDER BY q.sUUID SKIP $skip LIMIT $limit", Map.of("skip", PAGECOUNT, "limit", PAGECOUNT+PAGESIZE) );
//    }
    
    /**
     * Show similarity score between sentences (QR).
     * @param sess
     * @param full true for full dump
     */
    public static void dumpDbQRscore( Connection conn, boolean full )
    {
//        if( !full )
//        {
//            Iterable<QRscoreCW> qrscw = sess.loadAll(QRscoreCW.class);
//            qrscw.forEach( q -> System.out.println(""+q.getName()+ ": "+q.getScore()) );
//        }
//        else
//        {
//            Result result = sess.query("MATCH (q:QRscoreCW)-[r:SCORE]->(s:SentenceNode) RETURN q.name as qname,q.score as qscore,collect(s.name) as sname,collect(s.origText) as origText",Collections.EMPTY_MAP);
//            result.forEach(entry -> {
//                System.out.println(""+entry.get("qname")+", " +(double)entry.get("qscore"));
//                System.out.println("   "+((String[])entry.get("sname"))[0] +", "+ ((String[])entry.get("origText"))[0]);
//                System.out.println("   "+((String[])entry.get("sname"))[1] +", "+ ((String[])entry.get("origText"))[1]);
//            });
//        }
    }
}
