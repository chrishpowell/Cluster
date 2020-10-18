/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.main;


import cwts.networkanalysis.Clustering;

import eu.discoveri.documents.DocumentDb;
import eu.discoveri.predikt.cluster.DocumentCategory;
import eu.discoveri.predikt.cluster.RawDocument;
import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.exceptions.NumberOfNodesException;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import eu.discoveri.predikt.graph.Corpi;
import eu.discoveri.predikt.sentences.CorpusProcessDb;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ProcessDocs1
{
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
     * Test docs to test sentences.
     * @param popl
     * @return 
     * @throws URISyntaxException 
     */
    public static long getSentsFromTestDocs( Populate popl )
            throws URISyntaxException
    {
        // Sentence count
        int totSents = 0;
        
        System.out.println("... Get sentences from List of docs");
        List<RawDocument> lrd = Corpi.testDocuments();
        
        // Get sentences of the docs
        System.out.println("... Form sentences from docs");
        totSents = lrd.stream().map((rd) -> {
            eIdx++;
            return rd;            
        }).map((rd) -> {
            // Sentences per document
            List<SentenceNode> lsn = popl.extractSentences(rd.getText(),rd.getDocCategory());
            System.out.println("Num. sentences in doc: " +rd.getDocCategory().getCategoryNum()+ " is " +lsn.size());
            return lsn;
        }).map((lsn) -> {
            if( lsn.size() > largeDoc )
                largeDoc = lsn.size();
            return lsn;
        }).map((lsn) -> {
            if( lsn.size() < smallDoc )
                smallDoc = lsn.size();
            return lsn;            
        }).map((lsn) -> {
            // Write to Db
            lsn.forEach(sn -> sn.persist(sns));
            // Total size
            return lsn;
        }).map((lsn) -> lsn.size()).reduce(totSents, Integer::sum);
    
        System.out.println("    > Num. documents: " +eIdx+ ", Num. sentences written: " +totSents);
        System.out.println("    > Smallest doc contains " +smallDoc+ " sentences, largest contains " +largeDoc+ " sentences.  Average doc. size: " +(int)(totSents/eIdx));
        
        return totSents;
    }
    
    /**
     * Populate graph db from test sentences.
     * @param sns 
     * @return  
     */
    public static int populateGraphDbFromMemory( SentenceNodeService sns )
    {
        List<SentenceNode> lsn = getSentsOfList();
        lsn.forEach(sn -> sn.persist(sns));
        
        return lsn.size();
    }
    
    /**
     * Read docs from (MySQL) database, generate sentences.
     * 
     * @param conn1
     * @param popl
     * @param sns
     * @throws SQLException 
     */
    static int  eIdx = -1, largeDoc = 0, smallDoc = Integer.MAX_VALUE;
    public static long populateGraphWithSents( Connection conn, Populate popl, SentenceNodeService sns )
            throws SQLException
    {
        // Count of sentences written
        long totSents = 0;
        System.out.println("      Get all documents from Rdb");
        
        // Exclude documents with given titles
        String excl[] = {"%Privacy Policy%", "%Terms of Service%"};
        PreparedStatement docs = conn.prepareStatement("select title,content from Document where lower(title) NOT LIKE ? AND lower(title) NOT LIKE ?");
        docs.setString(1, excl[0].toLowerCase());
        docs.setString(2, excl[1].toLowerCase());
        ResultSet rsDocs = docs.executeQuery();
        
        // Get sentences of the docs
        System.out.println("... Form sentences from docs (Stream of List)");
        
        while( rsDocs.next() )
        {
            eIdx++;
//            Stream.Builder<List<SentenceNode>> llsn = Stream.builder();
            
            // (Sentences per document, memory should be OK)
            List<SentenceNode> lsn = popl.extractSentences(rsDocs.getString("content"),new DocumentCategory());  // ***** @TODO: Get DCat from db
            System.out.println("Num. sentences in doc: " +eIdx+ " is " +lsn.size());
            if( lsn.size() > largeDoc )
                largeDoc = lsn.size();
            if( lsn.size() < smallDoc )
                smallDoc = lsn.size();

            // Write to Graph Db
            lsn.forEach(sn -> sn.persist(sns));

            // Total size
            totSents += lsn.size();
        }

        // Note: Doc. index starts at zero
        System.out.println("    > Num. documents: " +(eIdx+1)+ ", Num. sentences written: " +totSents);
        System.out.println("    > Smallest doc contains " +smallDoc+ " sentences, largest contains " +largeDoc+ " sentences.  Average doc. size: " +(int)(totSents/eIdx));
        
        return totSents;
    }
    
    
    /**
     * M A I N
     * =======
     * 
     * A. Setup for sentence processing (database & NLP)
     * B. Get and process the sentences
     * C. Determine sentence similarity
     * D. Calculate clusters
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
            throws Exception
    {
        /*
         * A. Setup
         * --------
         */
        // Timer
        long totSecs = 0l;
        Instant st = Instant.now();
        
        // Language/locale setup
        System.out.println("... Setup [English]");
        EnSetup enSetup = new EnSetup();
        
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer)
        System.out.println("... Setup NLP");
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );

        // Connect to lemma/doc RdB
        System.out.println("... Connect to databases");
        Connection conn = DocumentDb.lemmaDb();
        // Connect to document db
        Connection conn1 = DocumentDb.docDb();
        
        // Pagination
        System.out.println("    > Pagination size: " +Constants.PAGSIZE);

        // Timer
        Instant en = Instant.now();
        long secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> A. Setup time (secs): " + secs);

    
        /*
         * B. Process sentences
         * --------------------
         */
        // Timer
        st = Instant.now();
        
        System.out.println("---------------------------------------------------");
        System.out.println("... Get sentences from source");
        // 1a. Get sentences from raw text
//        long numSents = populateGraphDbFromMemory(sns);
        // 1b. Get sentences from document db. (What if 250k sentences?)
//        long numSents = populateGraphWithSents( conn1, popl, sns );
        // 1c. Get sentences from test docs
        long numSents = getSentsFromTestDocs(popl,sns);
        
        
        // 2. Set up sentence analysis on above sentences/language/locale
        System.out.println("... Start sentence analysis");
        CorpusProcessDb cp = new CorpusProcessDb( enSetup, discSess, qrscws );

        // 3. Count the sentences and edges on the graph
        System.out.println("... GraphDb, Num. sentence nodes: " +cp.sentenceNodeCount()+ ", Sentence edge count: " +cp.sentenceEdgeCount());
        
        // 4. Tokenize sentences (and remove OOB chars)
        // 5. Clean up tokens (punctuation,numbers,unwanted POStags etc.)
        // 6. Get lemmas
        System.out.println("... Process and clean sentences");
        cp.processSentences(conn, popl.getPme(), sns);

        // Timer
        en = Instant.now();
        secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> B. Process sentences (secs): " + secs);


        /*
         * C. Determine sentence similarity
         * --------------------------------
         */
        // Timer
        st = Instant.now();
        
        System.out.println("---------------------------------------------------");
        System.out.println("... Determine similarities");

        // 7. Now calculate common word count between sentences and
        // do the token/lemma counting per sentence pair. Store sentence pair
        // results on graph db
        // NB: Count either tokens or lemmas but not both.
        // NB: Runs thread(s).  Do not continue until all threads complete.
        System.out.println("... Count & match lemmas. Please wait...");
        cp.countingLemmas(sess,numSents);
        // Timer
        Instant en0 = Instant.now();
        secs = Duration.between(st,en0).toSeconds();
        System.out.println("  ..> Count and match lemmas time (secs): " + secs);
        
        // 8. Calculate the similarity score between sentences.  Determines edge
        // weights.
        System.out.println("... Similarity calculation");
        cp.similarity();
        // Timer
        Instant en1 = Instant.now();
        secs = Duration.between(en0,en1).toSeconds();
        System.out.println("  ..> Similarity time (secs): " + secs );

        // 9. Form weighted edges ("SIMILARTO") between similar sentences. And
        // add Louvain index
        System.out.println("... Form weighted similarity edges");
        cp.formSimilarityEdges();
        // Timer
        Instant en2 = Instant.now();
        secs = Duration.between(en1,en2).toSeconds();
        System.out.println("  ..> Weighted similarity time (secs): " + secs );

        // Timer
        en = Instant.now();
        secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> C. Similarity (secs): " + secs);
        
    
        /*
         * D. Clustering
         * -------------
         */
        // Timer
        st = Instant.now();
        
        System.out.println("---------------------------------------------------");
        // 10. Clustering
        try
        {
            System.out.println("... Generate sentence clusters");
            Clustering c = cp.clustersGen();
                    
            System.out.println("  > Num. clusters and 'non' clusters: " +c.getNClusters());
            int[][] npc = c.getNodesPerCluster();
            // Check for 'real' (len>1) clusters
            int ll = 0;
            for( int[] eachRow: npc )
            {
                if( eachRow.length > 1 )
                    ++ll;
            }
            System.out.println("  > Num. useful clusters: " +ll);

            // 11. Update db with clusters
            System.out.println("... Update graph db with cluster num. [-1: no cluster, n=0..m: cnumber]");
            cp.updateSentenceCluster(c);
        }
        catch( NumberOfNodesException nnx )
        {
            System.out.println("Clustering ERROR! " +nnx.getMessage());
            discSess.close();
            System.exit(-1);
        }

        // Timer
        en = Instant.now();
        secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> D. Clustering (secs): " + secs);
        
        long s = totSecs % 60;
        long h = totSecs / 60;
        long m = h % 60;
        h /= 60;
        System.out.println("Total processing time: " +h +":"+ m +":"+ s);

        
        /*
         * E. Close
         * --------
         */
        System.out.println("---------------------------------------------------");
        System.out.println("\r\n... Closing");
        discSess.close();
        conn.close();
        conn1.close();
    }
}
