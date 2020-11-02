/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.main;


import com.zaxxer.hikari.HikariDataSource;
import cwts.networkanalysis.Clustering;
//import org.apache.commons.math3.stat.StatUtils;

import eu.discoveri.predikt3.cluster.DocumentCategory;
import eu.discoveri.predikt3.cluster.RawDocument;
import eu.discoveri.predikt3.config.Constants;
import eu.discoveri.predikt3.config.EnSetup;
import eu.discoveri.predikt3.exceptions.NumberOfNodesException;
import eu.discoveri.predikt3.graph.Populate;
import eu.discoveri.predikt3.graph.SentenceNode;
import eu.discoveri.predikt3.graph.Corpi;
import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.utils.DbUtils;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ProcessDocs
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
     * @param conn
     * @param popl
     * @return 
     * @throws URISyntaxException 
     * @throws java.sql.SQLException 
     */
    static int  dIdx = 0;
    static int largeDoc = 0, smallDoc = Integer.MAX_VALUE;
    public static long getSentsFromTestDocs( Connection conn, Populate popl )
            throws URISyntaxException, SQLException
    {
        // Sentence count
        int totSents = 0;
        
        System.out.println("... Get sentences from List of test docs");
        List<RawDocument> lrd = Corpi.testDocuments();
        
        // Get sentences of the docs
        System.out.println("... Form sentences from docs");
        totSents = lrd.stream().map((rd) -> {
            dIdx++;
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
            try {
                // Write to Db
                SentenceNode.persistListSentences(conn, lsn);
            } catch (SQLException ex) {
                Logger.getLogger(ProcessDocs.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Total size
            return lsn;
        }).map((lsn) -> lsn.size()).reduce(totSents, Integer::sum);
    
        System.out.println("    > Num. documents: " +dIdx+ ", Num. sentences written: " +totSents);
        System.out.println("    > Smallest doc contains " +smallDoc+ " sentences, largest contains " +largeDoc+ " sentences.  Average doc. size: " +(int)(totSents/dIdx));
        
        return totSents;
    }
    
    /**
     * Populate graph db from test sentences.
     * @param conn
     * @return  
     * @throws java.sql.SQLException  
     */
    public static int populateGraphDbFromMemory( Connection conn )
            throws SQLException
    {
        List<SentenceNode> lsn = getSentsOfList();
        SentenceNode.persistListSentences(conn, lsn);
        
        return lsn.size();
    }
    
    /**
     * Read docs from (MySQL) database, generate sentences.
     * 
     * @param conn
     * @param popl
     * @return 
     * @throws SQLException 
     */
    public static int populateSentsFromDbDocs( Connection conn, Populate popl )
            throws SQLException
    {
        int  eIdx = 0, largeDoc1 = 0, smallDoc1 = Integer.MAX_VALUE;
        
        // Count of sentences written
        int totSents = 0;
        System.out.println("      Get all documents from Rdb");
        
        // Exclude documents with given titles
        String excl[] = {"%Privacy Policy%", "%Terms of Service%"};
        PreparedStatement docs = conn.prepareStatement("select dId,title,content from Document where lower(title) NOT LIKE ? AND lower(title) NOT LIKE ?");
        docs.setString(1, excl[0].toLowerCase());
        docs.setString(2, excl[1].toLowerCase());
        ResultSet rsDocs = docs.executeQuery();
        
        // Get sentences of the docs
        System.out.println("... Form sentences from docs");
        
        while( rsDocs.next() )
        {
            ++eIdx;
            
            // (Sentences per document, memory should be OK)
            List<SentenceNode> lsn = popl.extractSentences(rsDocs.getString("content"),new DocumentCategory(rsDocs.getLong("dId")));  // ***** @TODO: Get DCat from db
            System.out.println("Num. sentences in doc: " +eIdx+ " is " +lsn.size());
            if( lsn.size() > largeDoc1 )
                largeDoc1 = lsn.size();
            if( lsn.size() < smallDoc1 )
                smallDoc1 = lsn.size();

            // Write to Db
            SentenceNode.persistListSentences(conn, lsn);

            // Total size
            totSents += lsn.size();
        }

        // Note: Doc. index starts at 1.
        System.out.println("    > Num. documents: " +eIdx+ ", Num. sentences written: " +totSents);
        System.out.println("    > Smallest doc contains " +smallDoc1+ " sentences, largest contains " +largeDoc1+ " sentences.  Average doc. size: " +(int)(totSents/eIdx));
        
        return totSents;
    }
    
    /**
     * Dump the clusters array.
     * @param npc 
     */
    public static void dumpClusterArray( int[][] npc )
    {
        System.out.println(Arrays.deepToString(npc));
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
        Populate popl = new Populate( LangCode.en );
        
        // Get a Db connection from pools
        System.out.println("... Connect to databases");
        HikariDataSource lemmaDbPool = DbUtils.getPooledDocDbConnection();
        HikariDataSource docDbPool = DbUtils.getPooledDocDbConnection();
//        Connection lemmaDb = lemmaDbPool.getConnection();                       // "Permanent" connection
        Connection docDb1 =  docDbPool.getConnection();                         // "Permanent" connection

        // Now empty tables
        System.out.println("*** Empty tables: Sentence, Token, CWcount, QRscoreCW");
        DbUtils.emptySentenceTables(docDb1);
        
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
	CompletionService<Integer> cs = new ExecutorCompletionService<>(execsrv);
        
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
//        int numSents = populateSentsFromDbDocs( docDb1, popl );
        // 1c. Get sentences from test docs
        int numSents = (int)getSentsFromTestDocs(docDb1,popl);
        System.out.println("...> Num. sentences: " +numSents);
        
        // 2. Set up sentence analysis on above sentences/language/locale
        System.out.println("... Start sentence analysis");
        CorpusProcessDb cp = new CorpusProcessDb( docDbPool, enSetup );
        cp.setSentCount(numSents);                                              // Set count of sentences

        // 3. ??
        // 4. Tokenize sentences (and remove OOB chars)
        // 5. Clean up tokens (punctuation,numbers,unwanted POStags etc.)
        // 6. Get lemmas
        System.out.println("... Process and clean sentences");
//        cp.processSentencesByPage(docDb1, popl.getPme());
        cp.processSentencesById(docDb1, cs, popl.getPme(), Constants.PAGSIZE);

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
        cp.countLemmas(lemmaDbPool,cs,numSents);
        // Timer
        Instant en0 = Instant.now();
        secs = Duration.between(st,en0).toSeconds();
        System.out.println("  ..> Count and match lemmas time (secs): " + secs);
        
        // 8. Calculate the similarity score between sentences.  Determines edge weights.
        System.out.println("... Similarity calculation");
        cp.similarity(docDbPool.getConnection(),cs,numSents);
        // Timer
        Instant en1 = Instant.now();
        secs = Duration.between(en0,en1).toSeconds();
        System.out.println("  ..> Similarity time (secs): " + secs );

        // 9. Form weighted edges ("SIMILARTO") between similar sentences. And
        // add Louvain index
//        System.out.println("... Form weighted similarity edges");
//        cp.writeClusterIndices(docDbPool.getConnection(),cs,numSents);
        // Timer
//        Instant en2 = Instant.now();
//        secs = Duration.between(en1,en2).toSeconds();
//        System.out.println("  ..> Weighted similarity time (secs): " + secs );

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
        // 10a. Stats
//        System.out.println(".................[Score stats]...................................");
//        double[] stats = cp.adjustScores(docDb1);
//        System.out.println("Max: " +StatUtils.max(stats)+ ", min: " +StatUtils.min(stats)+ ", mean: " +StatUtils.mean(stats)+ ", var: " +StatUtils.variance(stats));
//        double[] modes = StatUtils.mode(stats);
//        System.out.println("Mode(s): ");
//        for( double mode: modes ) { System.out.print(mode+""); }
//        System.out.println("\r\n.................................................................");
        
        // 10b. Clustering
        try
        {
            System.out.println("... Generate sentence clusters");
            Clustering c = cp.clustersGen(docDbPool.getConnection(),numSents);
                    
            System.out.println("  > Num. clusters and 'non' clusters: " +c.getNClusters());
            int[][] npc = c.getNodesPerCluster();
            dumpClusterArray(npc);
            
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
            cp.updateSentenceCluster(docDbPool.getConnection(),c);
        }
        catch( NumberOfNodesException nnx )
        {
            System.out.println("Clustering ERROR! " +nnx.getMessage());
            System.exit(-1);
        }
        
        // 11. Trim sentences not in clusters
        

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
        docDbPool.close();
        lemmaDbPool.close();
    }
}
