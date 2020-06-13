/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.main;

import eu.discoveri.louvaincluster.Clusters;
import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceEdge;
import eu.discoveri.predikt.graph.SentenceEdgeService;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.SentenceNodeService;
import eu.discoveri.predikt.graph.Vertex;
import eu.discoveri.predikt.sentences.CorpusProcess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        // Edges
        List<SentenceEdge> ledges = new ArrayList<>();
        
        // Get sentences from raw text
//        List<SentenceNode> lsents = Corpi.getVertices();
        // Get sentences from document db
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
        System.out.println("    > Num. documents" +eIdx);
        
        // Merge to one list
        System.out.println("... Form complete sentence list");
        List<SentenceNode> lsents = llsn.build().flatMap(x -> x.stream()).collect(Collectors.toList());
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
        // do the token/lemma counting per sentence pair (QRscore) [Updates each sentence]
        System.out.println("... Count lemmas");
        cp.countingLemmas();
        
        // Calculate the similarity score between sentences (QRscore) [Updates each sentence]
        System.out.println("... Similarity calculation");
        cp.similarity();
        
        /*
         * Populate db (from corpus Maps)
         * ------------------------------
         */
        System.out.println("... Build graph db");
        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        // Db service
        SentenceNodeService sns = new SentenceNodeService();
        SentenceEdgeService ses = new SentenceEdgeService();

        // Clear database
        System.out.println("... Purge db");
        sess.purgeDatabase();

        // Edges
        System.out.println("... Form edges and nodes");
        cp.getQRscores().forEach((k,v) -> {
//            System.out.println("-----> Weight edge ["+k.getKey()+"]-["+k.getValue()+"]: " +v);
            if( v > Constants.EDGEWEIGHTMIN )
            {
                SentenceEdge se = new SentenceEdge(k.getKey(),k.getValue(),v);
                ledges.add(se);
                se.persist(ses);
            }
        });
        
        // Dump nodes/edges for cluster analysis
        System.out.println("... Louvain clustering");
        Map<Vertex,Long> vi = new HashMap<>();
        System.out.println("Num. nodes: " +lsents.size()+ ", num. edges: " +ledges.size());
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
                e.getN2().setLouvainIdx(idx);
            }
        });
        
        // Edge and edge weights
        System.out.println("... Edge weighting");
        double[] edgeWeights = new double[ledges.size()];
        int[][] edges = new int[2][ledges.size()];
        ledges.forEach(e -> {
            System.out.println(""+e.getN1().getLouvainIdx()+"\t"+e.getN2().getLouvainIdx()+"\t"+e.getWeight());
            edgeWeights[++eIdx] = e.getWeight();
            edges[0][eIdx] = (int)e.getN1().getLouvainIdx();
            edges[1][eIdx] = (int)e.getN2().getLouvainIdx();
        });
        
        // Nodes, update Louvain index
        System.out.println("... Update nodes");
        lsents.forEach(s -> s.persist(sns));
        
        // Clustering
        System.out.println("... Generate sentence clusters");
        Clusters.generate(lsents.size(), edges, edgeWeights);
        
        // Close
        System.out.println("");
        discSess.close();
        conn.close();
        conn1.close();
    }
}
