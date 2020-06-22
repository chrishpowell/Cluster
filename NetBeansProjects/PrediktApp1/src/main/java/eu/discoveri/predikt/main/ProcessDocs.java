/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.main;


import com.hazelcast.core.Hazelcast;
import cwts.networkanalysis.Clustering;

import eu.discoveri.louvaincluster.Clusters;
import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceEdge;
import eu.discoveri.predikt.graph.service.SentenceEdgeService;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import eu.discoveri.predikt.graph.Vertex;
import eu.discoveri.predikt.graph.Corpi;
import eu.discoveri.predikt.sentences.CorpusProcess;
import eu.discoveri.instrumentation.InstrumentationAgent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neo4j.ogm.model.Result;

import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ProcessDocs
{
    static long idx = -1;
    static int  eIdx = -1;
    
    /**
     * Connection for Lemma db
     * @return
     * @throws Exception 
     */
    public static Connection lemmaDb()
            throws Exception
    {
        String URL = "jdbc:mysql://localhost:3306/lemma?useSSL=false&serverTimezone=CET";
        String USER = "chrispowell";
        String PWD = "karabiner";
        
        return DriverManager.getConnection(URL,USER,PWD);
    }
    
    /**
     * Connection for documents db
     * @return
     * @throws Exception 
     */
    public static Connection docDb()
            throws Exception
    {
        String URL = "jdbc:mysql://localhost:3306/documents?useSSL=false&serverTimezone=CET";
        String USER = "chrispowell";
        String PWD = "karabiner";
        
        return DriverManager.getConnection(URL,USER,PWD);
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
    public static void indexCWs( Session sess )
    {
//        sess.query("create index qrsIdx for (q:QRscore) on (q.node1,q.node2)", Collections.EMPTY_MAP);
//        sess.query("create constraint on (c:QRscore) assert c.qrsIdx is unique", Collections.EMPTY_MAP);
    }
            
    
    /**
     * Test sentences.
     * 
     * @return 
     */
    public static List<SentenceNode> getSentsOfList()
    {
        System.out.println("... Get sentences from test map");
        return Corpi.getVertices();
    }
    
    /**
     * Read sentences from database.
     * 
     * @param conn1
     * @param popl
     * @return
     * @throws SQLException 
     */
    public static List<SentenceNode> getSentsOfDb( Connection conn1, Populate popl )
            throws SQLException
    {
        System.out.println("... Get all documents from db");
        PreparedStatement docs = conn1.prepareStatement("select * from Document");
        ResultSet rsDocs = docs.executeQuery();
        
        // Get sentences of docs
        System.out.println("... Form sentences of docs");
        Stream.Builder<List<SentenceNode>> llsn = Stream.builder();
        while( rsDocs.next() )
        {
            eIdx++;
            List<SentenceNode> lsn = popl.extractSentences(rsDocs.getString("content"),eIdx);
            llsn.add(lsn);
        }
        System.out.println("    > Num. documents: " +eIdx);
        
        // Merge to one list
        System.out.println("... Form complete sentence list");
        return llsn.build().flatMap(x -> x.stream()).collect(Collectors.toList());
    }
    
    /**
     * Form graph db edges from QRscores (nodeQ-nodeR edge weighting).
     * 
     * @param cp
     * @return 
     */
    public static List<SentenceEdge> formEdges( CorpusProcess cp )
    {
        // Db service for edges
        SentenceEdgeService ses = new SentenceEdgeService();
        // Edges
        List<SentenceEdge> ledges = new ArrayList<>();
        
        // Get all edges from QRscore and persist
        cp.getQRscores().forEach((k,v) -> {
//            System.out.println("-----> Weight edge "+k.getKey()+"-"+k.getValue()+": " +v);
            if( v.getScore() > Constants.EDGEWEIGHTMIN )
            {
                SentenceEdge se = new SentenceEdge(k.getKey(),k.getValue(),v.getScore());
                ledges.add(se);
                se.persist(ses);
            }
        });
        
        return ledges;
    }
    
    /**
     * Update Louvain on nodes.
     * 
     * @param lsents
     * @param ledges 
     */
    public static void louvain( List<SentenceNode> lsents, List<SentenceEdge> ledges )
    {
        // Db service for nodes
        SentenceNodeService sns = new SentenceNodeService();
        
        // Check for dups (multiple edges)
        Map<Vertex,Long> vi = new HashMap<>();

        // Set the Louvain index (for later clustering)
        ledges.forEach(e -> {
            Vertex v1 = e.getN1();
            if( !vi.containsKey(v1) )
            {
                vi.put(v1, ++idx);
                v1.setLouvainIdx(idx);
            }

            Vertex v2 = e.getN2();
            if( !vi.containsKey(v2) )
            {
                vi.put(v2, ++idx);
                v2.setLouvainIdx(idx);
            }
        });

        // Nodes, update Louvain index
        System.out.println("  > Update nodes with Louvain index");
        lsents.forEach(s -> s.persist(sns));
    }
    
    /**
     * Generate Louvain clusters.  @TODO: To LouvainClsuter
     * 
     * @param numNodes
     * @param ledges
     * @return 
     */
    public static Clustering clustersGen( int numNodes, List<SentenceEdge> ledges )
    {
                // Edge and edge weights for Cluster library:
        //   Num. nodes, edges[0][index]/edges[1][index], edgeWeights[index]
        System.out.println("  > Edge weighting");
        double[] edgeWeights = new double[ledges.size()];
        int[][] edges = new int[2][ledges.size()];
        ledges.forEach(e -> {
//            System.out.println(""+e.getN1().getLouvainIdx()+"\t"+e.getN2().getLouvainIdx()+"\t"+e.getWeight());
            edgeWeights[++eIdx] = e.getWeight();
            edges[0][eIdx] = (int)e.getN1().getLouvainIdx();
            edges[1][eIdx] = (int)e.getN2().getLouvainIdx();
        });
        
        return Clusters.generate( numNodes, edges, edgeWeights );
    }
    
    /**
     * Instrumentation object size (useful?)
     * @param object 
     */
    public static void printObjectSize(Object object)
    {
        System.out.println("Object type: " + object.getClass() +
          ", size: " + InstrumentationAgent.getObjectSize(object) + " bytes");
    }
    
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
            throws Exception
    {
        // Language/locale setup
        System.out.println("... Setup [English]");
        EnSetup enSetup = new EnSetup();
        
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer) 
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );

        // Connect to lemma db
        System.out.println("... Connect to databases");
        Connection conn = lemmaDb();
        // Connect to document db
        Connection conn1 = docDb();
        
        /*
         * Process sentences
         * -----------------
         */
        // Get sentences from raw text
        List<SentenceNode> lsents = getSentsOfList();
        // Get sentences from document db
//        List<SentenceNode> lsents = getSentsOfDb( conn1, popl );
        System.out.println("    > Num. sentences: " +lsents.size());
        
        // Set up sentence analysis on above sentences/language/locale
        System.out.println("... Sentence analysis");
        CorpusProcess cp = new CorpusProcess( lsents, enSetup );
        
        // Tokenize sentences
        System.out.println("... Tokenize sentences");
        cp.rawTokenizeSentenceCorpus(popl.getPme());

        // Clean up tokens (punctuation,numbers,unwanted POStags etc.)
        System.out.println("... Clean up tokens");
        cp.cleanTokensSentenceCorpus();
        
        // Get lemmas
        System.out.println("... Lemmatize");
        cp.lemmatizeSentenceCorpusViaDb(conn);
        
        // Now calculate common word count between sentences and
        // do the token/lemma counting per sentence pair
        System.out.println("... Count lemmas");
        cp.countingLemmas();
        
        // Calculate the similarity score between sentences.  Determines edge weights.
        // (QRscore) [Updates each sentence]
        System.out.println("... Similarity calculation");
        cp.similarity();
        
        /*
         * Populate db (from corpus Maps)
         * ------------------------------
         */
        System.out.println("\r\n... Build graph db");
        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();

        // Clear database
        System.out.println("... Purge db");
        sess.purgeDatabase();
        
        // Drop and create indexes
        System.out.println("    > Drop and create indexes");
        dropIndexes( sess );
        // QRscore index
        indexQRscore( sess );
        // Commonwords index
        //indexCWs( sess )

        // Edges
        System.out.println("... Form edges and nodes from Sentences");
//        printObjectSize(cp.getQRscores());
        List<SentenceEdge> ledges = formEdges( cp );
//        System.out.println("    > Num. nodes: " +lsents.size()+ ", num. edges: " +ledges.size());
        
        /*
         * Clustering
         * ----------
         */
        // Get nodes/edges for Louvain (cluster) analysis and update nodes
        System.out.println("... Louvain clustering");
        louvain( lsents, ledges );
        
        // Clustering
        System.out.println("... Generate sentence clusters");
        Clustering c = clustersGen( lsents.size(), ledges );
        
        System.out.println("  > Num. clusters: " +c.getNClusters());
        int[][] npc = c.getNodesPerCluster();
        System.out.println(Arrays.deepToString(npc));
        
        // Update db with clusters
        // ...
        System.out.println("... Update graph db with cluster info (TBD)");
        
        // Close
        System.out.println("\r\n... Closing");
        Hazelcast.shutdownAll();
        discSess.close();
        conn.close();
        conn1.close();
    }
}
