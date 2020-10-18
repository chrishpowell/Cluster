/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.graph.service.SentenceEdgeService;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GraphUtils
{
    static int idx = 0;

    /**
     * Persist corpus from vertex/edge list of corpus.
     * @param sns
     * @param ses
     */
    public static void populateDbFromList(  SentenceNodeService sns,
                                            SentenceEdgeService ses )
    {
        Corpi.getVertices().forEach(s -> {
            s.persist(sns);
        });                                                                     // SentenceNodes
//        Corpi.getEdges().forEach((k,v) -> v.persist(ses));                      // SentenceEdges
    }

    /**
     * Get all nodes and their adjacencies from db.
     * @param sess 
     * @return  
     */
    public static List<Vertex> setAllAdjacencies(Session sess)
    {   
        String q1 = "MATCH (start:SentenceNode)-[rel:SIMILARTO]-(end) RETURN start,collect(end) as ends,collect(rel.weight) as weight";
        Result result = sess.query(q1,Collections.emptyMap());
        
        List<Vertex> ls = new ArrayList<>();
        result.queryResults().forEach( entry -> {
            List<Adjacency> la = new ArrayList<>();
            
            Vertex sn = (Vertex)entry.get("start");
            List<Vertex> le = (List<Vertex>)entry.get("ends");
            List<Double> lw = Arrays.asList((Double[])entry.get("weight"));
            
            idx = 0;
            le.forEach(v -> {
                la.add(new Adjacency(v,lw.get(idx)));
                ++idx;
            });
            
            sn.setAdjacencies(la);
            ls.add(sn);
        });
        
        return ls;
    }
    
    /**
     * Recurse over adjacencies of node.
     * @param v 
     */
    private static List<Vertex> recurseVisited( Vertex v, String compName, List<Vertex> vtxs )
    {   
        v.setVisited();
        v.setComponent(compName);
        vtxs.add(v);
        
        // Look at all adjacencies of this node
        v.getAdjacencies().forEach(a -> {
            if( !a.getENode().isVisited() ) recurseVisited(a.getENode(),compName,vtxs);
        });
        
        return vtxs;
    }
    
    /**
     * Find (connected) components of graph.
     * @param nodes 
     */
    static int cidx = 0;
    public static Map<String,List<Vertex>> findComponents( List<Vertex> nodes )
    {
        Map<String,List<Vertex>> comps = new HashMap<>();
        
        nodes.forEach( v -> {
            if( !v.isVisited() )
            {
                List<Vertex> vtxs = new ArrayList<>();
                comps.put("C"+cidx, recurseVisited(v,"C"+cidx,vtxs));
                ++cidx;
            }
        });
        
        return comps;
    }
    
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
    
    /**
     * QRscore index.
     * @param sess 
     */
    public static void indexQRscore( Session sess )
    {
        sess.query("create index qrsIdx for (q:QRscore) on (q.node1,q.node2)", Collections.EMPTY_MAP);
        sess.query("create constraint on (c:QRscore) assert c.qrsIdx is unique", Collections.EMPTY_MAP);
    }

    
    /**
     * Commonwords index.
     * @param sess 
     */
    public static void indexQRscoreCW( Session sess )
    {
        sess.query("create index qrscwIdx for (q:QRscoreCW) on (q.namespace,q.name)", Collections.EMPTY_MAP);
        sess.query("create constraint on (c:QRscoreCW) assert c.qrscwIdx is unique", Collections.EMPTY_MAP);
    }
    
        
    /**
     * Create index against sentence node
     * @param sess 
     */
    public static void indexSN( Session sess )
    {
        sess.query("create index snIdx for (sn:SentenceNode) on (sn.namespace,sn.name)", Collections.EMPTY_MAP);
        sess.query("create constraint on (c:snIdx) assert c.snIdx is unique", Collections.EMPTY_MAP);
    }
    
        
    /*
     * Get SentenceNodes by page
     */
    private static long LIMIT = 0, SKIP = 0;
    private static String n4jQuery = "";
    /**
     * Setup paginate size.
     * @param nodeName  Formal name for node (eg: SentenceNode)
     * @param nickName  Returned name (eg: sn)
     * @param pageSize 
     */
    public static void paginateSetup( String nodeName, String nickName, long pageSize )
    {
        n4jQuery = "MATCH (xx:"+nodeName+") RETURN xx AS "+nickName+" ORDER BY xx.sUUID SKIP $skip LIMIT $limit";
        SKIP -= pageSize;
        LIMIT = pageSize;
    }
    /**
     * Get next page.  Eg:
        static int cnt;
        paginateSetup( "SentenceNode","sn", 3 );
        Result result;
        while( true )
        {
            System.out.println("::> Page: " +cnt++);
            result = findByCypherPaginate( sess );
            if( result.iterator().hasNext() )
            {
                result.queryResults().forEach(entry -> {
                    SentenceNode sn = (SentenceNode)entry.get("sn");
                    System.out.print(" [" +sn.getName()+ "](" +sn.getNid()+ ")");
                });
                System.out.println("");
            }
            else
                break;
        }
     * @param sess
     * @return 
     */
    public static synchronized Result findByCypherPaginate( Session sess )
    {
        SKIP += LIMIT;
        return sess.query( n4jQuery, Map.of("skip", SKIP, "limit", LIMIT) );
    }
}
