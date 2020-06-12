/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.main;

import eu.discoveri.predikt.graph.Adjacency;
import eu.discoveri.predikt.graph.CDs;
import eu.discoveri.predikt.graph.GraphUtils;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.SentenceNodeService;
import eu.discoveri.predikt.graph.SentenceEdgeService;
import eu.discoveri.predikt.graph.Vertex;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GraphTest2
{
    /**
     * Betweenness.
     * @param nodes must have had adjacencies set and components determined.
     * @TODO: Difficult to add/merge new nodes?
     * 
     * Brandes algorithm
     * -----------------
     * for all s in V:
     *      // Dists to inf., Sigma to zero
     *      init Preds, Dists, Sigmas (predecessors, dist to source, shortest paths)
     * 
     *      // Dist is dist v to s.  Hence set Dist(s) = 0
     *      push s onto Queue
     *      while Queue not empty:
     *          pop Queue into v; push v onto Stack
     *          for all remote vertices of edges of v (being w):
     *              if Dist(v) = inf.:  // First time for this node
     *                  dist(w) = dist(v) + weight (of this edge)
     *                  push w onto Stack
     *              else
     *              if Dist(w) = Dist(v) + weight (of this edge)
     *                  Sigma(w) += Sigma(v)
     *                  append v onto Pred(v)
     * 
     *      // "Accumulation"
     *      while Stack not empty:
     *          pop Stack into w
     *          for all Pred(w)
     *              calc dependencies // See Brandes algorithm
     *              calc Cb(w)        // Central Betweenness
     */
    private void betweenness( Map<String,List<Vertex>> components )
    {
        // For each disconnected subgraph...
        components.forEach((c,nodes) -> {
            System.out.println("\r\n=====> Component: " +c);
            
            // Limitless queue
            Queue<SentenceNode> q = new LinkedBlockingQueue();
            // Stack
            Deque<SentenceNode> stk = new LinkedBlockingDeque();

            // Check all nodes (NB: not all may have edges)
            nodes.forEach(node -> {
                SentenceNode snsrc = (SentenceNode)node;

                // Reset all each time
                nodes.forEach(n -> {
                    SentenceNode nn = (SentenceNode)n;
                    nn.getPreds().clear();
                    nn.setDist(Double.MAX_VALUE);
                    nn.setSpath(0.d);
                });
                // Init this node
                snsrc.setDist(0.d);                                                 // delta-s
                snsrc.setSpath(1.d);                                                // sigma-s (lowercase sigma)

                // Push on to Q
                q.add(snsrc);

                while( !q.isEmpty() )
                {          
                    // Pop queue
                    System.out.println("  Head of queue: " +q.peek());
                    SentenceNode snv = q.poll();                                    // Aroohagh, aroohagh!!  Returns null if nothing there.

                    stk.push(snv);                                                  // Push on to stack, exception if null

                    snsrc.getAdjacencies().forEach(adj -> {
                        SentenceNode sntgt = (SentenceNode)adj.getENode();          // Remote vertex
                        System.out.println("   Edge start: " +snsrc+ ", end: " +sntgt);
                        System.out.println("   Tgt dist: " +sntgt.getDist());
                        // Process against opposite vertex on edge 
                        if( sntgt.getDist() == Double.MAX_VALUE )                       // Not yet processed?
                        {
                            sntgt.setPrev(snv);                                         // snv is 'previous' vertex (on edge)
                            sntgt.setDist(snv.getDist() + adj.getWeight());             // Shortest weighted hop on from snv
                            q.add(sntgt);                                               // Push on to queue
                        }
                        // Still on shortest path?
                        if( sntgt.getDist() == snv.getDist() + adj.getWeight() )        // Shortest path
                        {
                            sntgt.setSpath(sntgt.getSpath()+snv.getSpath());
                            sntgt.getPreds().add(snv);                                  // Add 'other' end
                        }
                    });
                }

                // Accumulation
                while( !stk.isEmpty() )
                {
                    SentenceNode snw = stk.pop();
                    System.out.println(" > Accum.--> " +snw);
                    snw.getPreds().forEach(p -> System.out.println("  Preds: " +p.getName()));

                    // NB: Be careful with disconnected graph!!
                    // Got any Preds for target node?
                    if( snw.getPreds().size() > 0 && snw != snsrc )
                    {
                        snw.getPreds().forEach(v -> v.setBetween(v.getBetween()+(v.getSpath()/snw.getSpath())*(1.d+snw.getBetween())));
                        snw.setCbetween(snw.getCbetween()+snw.getBetween());
                    }
                    else
                    {
                        snw.setCbetween(0.d);
                    }
                }
            });
        });
    }
    
    /**
     * Calc. tuned central betweenness
     * @return 
     */
    private Map<String,CDs> calcCBw( List<Vertex> nodes )
    {
        Map<String,CDs> CDwtuned = new TreeMap<>();
        final double ALPHATUNING = 1.3d;                   // Must be positive
        
        nodes.forEach(n -> {
            double weight = 0.d, w = 0.d;
            
            for( Adjacency adj: n.getAdjacencies() )
                { weight += adj.getWeight(); }
            
            int size = n.getAdjacencies().size();
            CDwtuned.put( n.getName(),
                          new CDs(size, weight, Math.pow(size,1.d-ALPHATUNING)*Math.pow(weight,ALPHATUNING))  );
        });
        
        return CDwtuned;
    }
    
    /**
     * Tuned central betweenness and stats.
     * 
     * @param cbwTuned
     * @param nodes
     * @return 
     */
    public DescriptiveStatistics cbWeightOut( Map<String,CDs> cbwTuned, List<Vertex> nodes )
    {
        // Sum of tuned betweennesses
        double sum = 0.d;
        
        // Sum weighted/tuned
        sum = calcCBw(nodes).entrySet().stream().map((entry) -> {
            CDs cds = entry.getValue();
            System.out.println("> " +entry.getKey()+ " ("+cds.getCd()+") :("+cds.getCDw()+") ("+cds.getCDwtuned()+") ");
            return cds;
        }).map((cds) -> cds.getCDwtuned()).reduce(sum, (accumulator, _item) -> accumulator + _item);
        System.out.println("");
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        cbwTuned.forEach((key,cds) -> {
            stats.addValue(cds.getCDwtuned());
        });
        
        return stats;
    }
    
    /**
     * Dump adjacencies of set of nodes.
     * @param lv 
     */
    private void dumpAdjs( List<Vertex> lv )
    {
        lv.forEach(v -> {
            SentenceNode sn = (SentenceNode)v;
            System.out.println("Node:: "+sn.getName());
            sn.getAdjacencies().forEach(a -> System.out.println(" Rem. node: " +a.getENode().getName()+ ", weight: " +a.getWeight()));
        });
    }
    
    /**
     * Dump central betweenness numbers
     */
    private void dumpCB( List<Vertex> lv )
    {
        lv.forEach(v -> {
            SentenceNode n = (SentenceNode)v;
            System.out.println(n.getName()+":"+n.getPrev()+"- dist to start: "+n.getDist()+", SPath: "+n.getSpath()+", Betw: "+n.getBetween()+", CB: "+n.getCbetween());
        });
    }
    
    /**
     * Predecessors.
     * @param sNodes 
     */
    private void dumpPreds( List<Vertex> nodes )
    {
        nodes.forEach(v -> {
            SentenceNode n = (SentenceNode)v;
            n.getPreds().forEach(p -> System.out.print(n.getName()+", Pred: "+p+" Prev: "));
            System.out.println("" +(n.getPrev()!=null?n.getPrev():"<NULL>"));
         });
    }


    /**
     * =========================================================================
     *              M A I N
     * =========================================================================
     * @param args 
     */
    public static void main(String[] args)
    {
        // Init
        GraphTest2 gt2 = new GraphTest2();

        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        // Db service
        SentenceNodeService sns = new SentenceNodeService();
        SentenceEdgeService ses = new SentenceEdgeService();

        /*
         * Populate db (from corpus Maps)
         * ------------------------------
         */
        sess.purgeDatabase();                                                   // Clear database
//        GraphUtils.populateDbFromMaps( sns, ses );                              // Populate from Corpi
        
        // Set all adjacencies
        List<Vertex> lv = GraphUtils.setAllAdjacencies(sess);
        gt2.dumpAdjs(lv);
        System.out.println("");
        
        // Components
        Map<String,List<Vertex>> components = GraphUtils.findComponents(lv);
        components.forEach((c,lvs) -> {
            System.out.println("\r\nComponent: " +c);
            lvs.forEach(v -> System.out.print("  "+v.getName()));
        });
        System.out.println("");

        // Betweenness
        System.out.println("B2--------------");
        gt2.betweenness(components);
        System.out.println("----------------\r\n");
        
        // Dump betweenness stuff
        components.forEach((c,lvs) -> {
            System.out.println("\r\nComponent: " +c);
            gt2.dumpCB(lvs);
        });

        // Central betweenness
        final double ALPHATUNING = 1.3d;                                        // Tuning betweenness, must be positive

        // Calc. CB tuned
        components.forEach((c,lvs) -> {
            System.out.println("\r\n-------[CB tuned]-----------------\r\nComponent: " +c);
            
            Map<String,CDs> cbwTuned = gt2.calcCBw(lvs);
            System.out.println("\r\nCentral Betweenness (weighted):");
            System.out.println("Node (CD):(CDw) (CDw tuned ["+ALPHATUNING+"])");

            // Stats for filtering
            DescriptiveStatistics stats = gt2.cbWeightOut( cbwTuned, lvs );

            System.out.println("Stats, 20th percentile: " +stats.getPercentile(20.d));        
            cbwTuned.forEach((key,cds) -> {
                if( cds.getCDwtuned() >= stats.getPercentile(20.d) )
                    System.out.println("Kept: " +key);
            });
        });

        // Close
        System.out.println("");
        discSess.close();
    }
}
