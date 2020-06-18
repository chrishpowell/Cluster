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
}
