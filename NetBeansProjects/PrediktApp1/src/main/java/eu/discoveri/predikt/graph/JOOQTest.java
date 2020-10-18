/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;


import java.sql.Statement;
import java.util.concurrent.ExecutionException;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cwts.networkanalysis.Clustering;
import eu.discoveri.louvaincluster.Clusters;

import eu.discoveri.predikt.config.Constants1;
import eu.discoveri.predikt.exceptions.MissingMatchedWordException;
import eu.discoveri.predikt.exceptions.NumberOfNodesException;
import eu.discoveri.predikt.sentences.Nul;
import eu.discoveri.predikt.tests.DbWriteBatch;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class JOOQTest
{
    private static Map<Integer,Map<Integer,Nul>>    noDups = new HashMap<>();
    private static Nul                              nul = new Nul();
    
    /**
     * See if this sentence pair already processed.
     * @param keyQ
     * @param keyR
     * @return 
     */
    public static boolean isStoredPair(int keyQ, int keyR)
    {
        return (noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)) || (noDups.containsKey(keyR) && noDups.get(keyR).containsKey(keyQ));
    }
    
    /**
     * Check if pair already processed.
     * @param keyQ
     * @param keyR 
     */
    public static void storePairKeys(int keyQ, int keyR)
    {
        if( !noDups.containsKey(keyQ) )
        {
            if( !noDups.containsKey(keyR) )
            {
                // Start this sentence pair Q:R
                Map<Integer,Nul> nulMap = new HashMap<>();
                nulMap.put(keyR,nul); 
                noDups.put(keyQ,nulMap);
            }
            // Next in LOOP
        }
        else
        {
            if( !noDups.get(keyQ).containsKey(keyR) )
            {
                noDups.get(keyQ).put(keyR, nul);
            }
        }
    }
    
    /**
     * Data source/connection
     */
    private static HikariDataSource connection = null;
    static
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/documents?useSSL=false&serverTimezone=CET");
        config.setUsername("chrispowell");
        config.setPassword("karabiner");
        // MySQL settings: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("cachePrepStmts", "true");                 // Only one PS here?
        config.addDataSourceProperty("prepStmtCacheSize", "200");               // Only one PS here?
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");          // Size of PS
        config.addDataSourceProperty("useServerPrepStmts", "true");             // Does this help?
        //
        config.setMaximumPoolSize(100);                                         // Default pool size for MySQL
        config.setConnectionTimeout(10000);
 
        connection = new HikariDataSource(config);
    }
    
    /**
     * Lemma counting.
     * @param wordsQ
     * @param wordsR
     * @param cqr
     * @return 
     */
    public static Map<String,FakeCWcount> lemmaCompare( List<FakeToken> wordsQ, List<FakeToken> wordsR, FakeCWcount cqr )
    {
        // Map lemma to count
        Map<String,FakeCWcount> wCountQR = new HashMap<>();
        
        // For each lemma in sentenceQ (Note: Q lemma count >= R lemma count)
        for( FakeToken tQ: wordsQ )
        {
            String wordQ = tQ.getLemma();       // *MATCH* this lemma
            if( wordQ.equals("") ) continue;    // Ignore blank lemmas

            if( wCountQR.containsKey(wordQ) )   // Is this lemma common between Q&R?
            { // Yes
                cqr = wCountQR.get(wordQ);      // Get current count for this token
                int count = cqr.getCountQ();         // Count in Q
                cqr.setCountQ(++count);              // Increment
                wCountQR.replace(wordQ, cqr);   // Update

                // Already counted all of R (where token key is created below), so skip R
                continue;
            }

            // Match Q token against each lemma in target sentenceR
            for( FakeToken tR: wordsR )
            {
                String wordR = tR.getLemma();   // *MATCH* this lemma

                if( wordR.equals(wordQ) )                               // Words match between sentences?
                { //Yes
                    // First match for token in both Q&R?
                    if( !wCountQR.containsKey(wordQ) )                  // Create Map entry
                    {//Yes
                        wCountQR.put(wordQ, new FakeCWcount(wordQ,1,1));    // Init count of both sentences
                    }
                    else
                    {//No
                        // Ok, get the counts for this lemma
                        cqr = wCountQR.get(wordR);
                        int count = cqr.getCountR();                    // Count for R sentence
                        cqr.setCountR(++count);                         // Increment R count
                        wCountQR.replace(wordR, cqr);                   // Update
                    }
                }
            }
        }
        
        return wCountQR;
    }
    
    /**
     * Get a page of sentences.
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    private static List<FakeSentenceNode> sentencePage( Connection conn, int offset, int pageSize )
            throws SQLException
    {
        // What to get
        PreparedStatement sent = conn.prepareStatement("select Sentence.id as sid, sentence from Sentence limit ?,?");
        PreparedStatement tkn = conn.prepareStatement("select token from Token where sn = ?");
        
        // Get a page of SentenceNodes (Qs), create list of Sentence objects
        List<FakeSentenceNode> lsSN = new ArrayList<>();
        try
        {
            sent.setInt(1,offset); sent.setInt(2,pageSize);
            ResultSet ress = sent.executeQuery();
            while( ress.next() )
            {
                List<FakeToken> ltkn = new ArrayList<>();
                tkn.setInt(1,ress.getInt("sid"));
                ResultSet restk = tkn.executeQuery();
                while( restk.next() )
                    { ltkn.add(new FakeToken(restk.getString("token"),restk.getString("token"))); }
                lsSN.add(new FakeSentenceNode(ress.getInt("sid"),ress.getString("sentence"),ltkn));
            }
        } catch (SQLException ex) {
            Logger.getLogger("JOOQTest").log(Level.SEVERE, null, ex);
        }
        
        return lsSN;
    }
    
    /**
     * Count lemmas in each sentence pair.  Long process time as pairs amount to
     * numSents*(numSents-1)/2.  50001 sentences are 1.25 billion pairs.
     * 
     * @param connection
     * @param numSents
     * @throws SQLException
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    static int SKIP0 = 0, SKIP1 = 0, LIMIT = Constants1.PAGSIZE, PCNT = 0;
    static int threadCount = 0;
    public static void countLemmas( HikariDataSource connection, int numSents )
            throws SQLException, InterruptedException, ExecutionException
    {
        System.out.println("Counting lemmas...");
        // Calc. num. pairs
        long numPairs = numSents*(numSents-1)/2;
        
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
	CompletionService<Integer> cs = new ExecutorCompletionService<>(execsrv);

        /*
         * Sort sentences by tokens' count (long to short hence s2 - s1,
         * see SentenceNode::compare())
         * Allows sentence matching to work efficiently/properly
         * @TODO:   HMMM...  What if lemmas exceeds tokens (blank/null lemmas in Token)?
         *
         * Count common words per sentence pair (Q,R). Q is 'source' sentence,
         * R is 'target' sentence.
         * [Tot. num. sentence pairs = sents.size*(sents.size-1)/2.]
         */
        // Let's get a connection to the db
        Connection conn = connection.getConnection();
        
        // First lock the Sentence table until all scores have been written
        // BUT!! *** Hikari hangs if I lock tables and a subsequent PreparedStatement (on an unlocked table) is used ***
//        Statement lock = conn.createStatement();
//        lock.execute("LOCK TABLES documents.Sentence WRITE,documents.Token WRITE");
        
        // Now read the Sentence table and update scores
        while( true )
        {
            // Get a page of SentenceNodes (Qs), create list of Sentence objects
            List<FakeSentenceNode> lsQ = sentencePage(conn,SKIP0,LIMIT);

            /*
             * Sort sentences by tokens' count (long to short hence s2 - s1)
             * Allows sentence matching to work efficiently/properly
             * @TODO:   HMMM...  What if lemmas exceeds tokens (blank/null lemmas in Token)?
             */
            // Default sorted() is on SentenceNode token list size.  "Supplier" to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<FakeSentenceNode>> isnQs = () -> StreamSupport.stream(lsQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            { 
                // Batch scores for thread handling
                List<FakeQRscoreCW> lqrscw = new ArrayList<>();

                // Loop
                isnQs.get().forEach(snQ -> {
                    // Count of common words in each sentence pair
                    FakeCWcount cqr = null;
                    
                    // Tokens for sentence Q
                    List<FakeToken> wordsQ = snQ.getTokens();

                    // Sentence R set
                    SKIP1 = SKIP0;                                              // Don't need to go right back to beginning
                    while( true )
                    {
                        List<FakeSentenceNode> lsR;
                        try {
                            // Get a page of SentenceNodes (Rs)
                            lsR = sentencePage(conn,SKIP1,LIMIT);
                            
                            // Default sorted() is on SentenceNode token list size.  Supplier, to obviate Stream closing (can only "use" Stream once)
                            Supplier<Stream<FakeSentenceNode>> isnRs = () -> StreamSupport.stream(lsR.spliterator(),false).sorted();

                            // Loop over sentenceR set
                            if( isnRs.get().iterator().hasNext() )
                            {
                                isnRs.get().forEach(snR -> {
                                    int keyQ = snQ.getId(), keyR = snR.getId();

                                    // Check to see if we should process this pair
                                    if( !snQ.equals(snR) && !isStoredPair(keyQ,keyR) )
                                    {
//                                        System.out.print(" [" +keyQ+ "/" +keyR+ "]");
                                        // Num. pairs processed
                                        ++PCNT;

                                        // Got a candidate pair, flag being processed
                                        storePairKeys(keyQ,keyR);

                                        // Tokens for sentence R
                                        List<FakeToken> wordsR = snR.getTokens();

                                        /*
                                         * Now to compare tokens/lemmas
                                         */
                                        // Map of count of common words between two sentences <Word,count of Word per sentence pair>
                                        Map<String,FakeCWcount> wCountQR = lemmaCompare(wordsQ,wordsR,cqr);

                                        // Ok, store the common (non stop,VB,NN) words counts for the sentence pairs
                                        if( wCountQR.values().size() > 0 )
                                        {
                                            cs.submit(new DbWriteScores(threadCount++,connection,new FakeQRscoreCW(snQ,snR, new ArrayList<>(wCountQR.values()))));
                                        }
                                    }
                                });
                            }
                            else
                            {
                                break;
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(JOOQTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        SKIP1 += LIMIT;
                    } // R loop
                });

                // How far along are we?
                System.out.format(" %6.2f%s", (float)PCNT*100./numPairs,"%");
            }
            else
                break;
            
            SKIP0 += LIMIT;
        }  // Q loop
        System.out.println("\r\nPCNT: " +PCNT+ ", threadCount: " +threadCount);
        
        // Timer
        Instant st = Instant.now();

        // For each completion thread
        for( int ii=0; ii<threadCount; ii++ )
        {
            int l = cs.take().get();
        }

        Instant en = Instant.now();
        long secs = Duration.between(st,en).toMillis();
        System.out.println("\r\n  -> All pair threads completion wait time (secs): " + (float)secs/1000.);
        
        // Shutdown the service
        execsrv.shutdown();
        // Unlock Sentence table
//        lock.execute("UNLOCK TABLES");
    }
    
    /**
     * Similarity scores. Determines topically similar sentences (pairs) and is
     * used as the weight of connecting edge between pairs in graph (IDF-weighted
     * word overlap).
     * 
     * For algorithm, see Allan et al."Retrieval and Novelty Detection at the Sentence Level" and
     * Metzler et al, "Similarity Measures for Tracking Information Flow".
     * 
     * @param conn
     * @param sentCount
     * @throws java.sql.SQLException
     */
    public static void similarity(Connection conn, int sentCount)
            throws SQLException
    {
        // CWcount begin/end
        long idBegin=-1, idEnd=-1;
        
        // Get QR matched words
        PreparedStatement qrsc = conn.prepareStatement("select matchWord,countQ,countR,qrscwId from CWcount where qrscwId=? order by qrscwId");
        // Update QRscore
        PreparedStatement up = conn.prepareStatement("update QRscoreCW set score = ? where id = ?");
        
        // Lemma count (per sentence)
        Map<String,Integer> lemmaSentCount = lemmaSentCount(conn);
        
        // Statements
        Statement st = conn.createStatement();
        // Start/End ids (Saves having to form a massive ResultSet [20 billion!] as id is auto_increment)
        ResultSet idSt = st.executeQuery("select min(qrscwId) idBegin from CWcount");
        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
        ResultSet idEn = st.executeQuery("select max(qrscwId) idEnd from CWcount");
        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
        
        // Update similarity scores (batch these?)
        // for each sentence pair in QRscoreCW
        // | for each common/matched word in that pair
        // | |   calc score:
        // | |     get count in sent Q, get count in sent R
        // | |     get tot. num. sents common word appears
        // | |     Apply algo: SumOf(QR):log(cQ+1)log(cR+1)log((lemSentCnt+1)/lemmaCnt+0.5))
        // | end loop
        // | store similarity score against sentence pair
        // end loop
        for( long id=idBegin; id<=idEnd; id++ )
        {
            double score = 0.d;

            qrsc.setLong(1, id);
            ResultSet res = qrsc.executeQuery();
            while( res.next() )
            {
                String mw = res.getString("matchWord");
                score += Math.log(res.getInt("countQ")+1.d)*Math.log(res.getInt("countR")+1.d)*Math.log((sentCount+1.d)/(lemmaSentCount.get(mw)+0.5d));
            }
            
            // Update the score (if significant) [and index the relevant Sentences]
            if( score > Constants1.EDGEWEIGHTMIN )
            {
                up.setDouble(1,score);
                up.setLong(2,id);
                up.execute();
            }
        }
    }
    
    
    /**
     * Determine Louvain index for Sentences based on edge weights between those
     * sentences. That is, where edge weight is significant (EDGEWEIGHTMIN) give
     * each sentence pair a Louvain cluster index number.
     * 
     * @param conn
     * @param numSents
     * @throws java.lang.InterruptedException 
     * @throws java.util.concurrent.ExecutionException 
     * @throws java.sql.SQLException 
     */
    public static void louvainIdx(Connection conn, int numSents)
            throws InterruptedException, ExecutionException, SQLException
    {
        // QRscoreCW begin/end
        long idBegin=-1, idEnd=-1;
        // Louvain index
        int louvainIdx = 0;
        
        // Get QR matched words
        PreparedStatement qrsc = conn.prepareStatement("select score, sIdQ, sIdR from QRscoreCW where id = ?");
        // Update Sentence
        PreparedStatement up = conn.prepareStatement("update Sentence set louvainIdx = ? where id = ?");
        
        // Statements
        Statement st = conn.createStatement();
        // Start/End ids (Saves having to form a massive ResultSet [upto 5 billion!] as id is auto_increment)
        ResultSet idSt = st.executeQuery("select min(id) idBegin from QRscoreCW");
        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
        ResultSet idEn = st.executeQuery("select max(id) idEnd from QRscoreCW");
        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
        
        // List of Sentence ids
        boolean sids[] = new boolean[numSents];
        Arrays.fill(sids,false);                    // Make sure all false to start
        
        for( long id=idBegin; id<=idEnd; id++ )
        {
            qrsc.setLong(1, id);
            ResultSet res = qrsc.executeQuery();
            
            while( res.next() )
            {
                double score = res.getDouble("score");
                if( score > Constants1.EDGEWEIGHTMIN )
                {
                    int snQ = res.getInt("sIdQ");
                    int snR = res.getInt("sIdR");
                    
                    // Check if already set. NB: Array idx from zero, SQL idx from 1.
                    if( !sids[snQ-1] ) // Q of pair
                    {
                        up.setInt(2, snQ);
                        up.setInt(1,++louvainIdx);
                        up.execute();
                        sids[snQ-1] = true;
                    }
                    if( !sids[snR-1] ) // R of pair
                    {
                        up.setInt(2, snR);
                        up.setInt(1,++louvainIdx);
                        up.execute();
                        sids[snR-1] = true;
                    }
                }
            }
        }
    }
    
    /**
     *
     * @returnselect * from QRscoreCW where
     * @throws NumberOfNodesException
     */
    public static Clustering clustersGen(Connection conn)
            throws NumberOfNodesException, SQLException
    {
        int lQ = 0, lR = 0;
        int clc = 0;
        int eIdx = -1;

        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet totScrs = st.executeQuery("select count(*) as c from QRscoreCW");
        while( totScrs.next() ) { clc = totScrs.getInt("c"); }
        
        // Clustering
        double[] edgeWeights = new double[clc];
        int[][] edges = new int[2][clc];
        
        // Sentence
        PreparedStatement sent = conn.prepareStatement("select louvainIdx from Sentence where id = ?");
        // Similar "edges" between sentences (nodes) *****---> MAY exceed Integer.MAX ---> May need to change source code cwts.networkanalysis
        ResultSet scrs = st.executeQuery("select score,sIdQ,sIdR from QRscoreCW where score>0.0");
        
        while( scrs.next() )
        {
            int sIdQ = scrs.getInt("sIdQ"); int sIdR = scrs.getInt("sIdR");
            double score = scrs.getDouble("score");
            
            sent.setInt(1, sIdQ);
            ResultSet s = sent.executeQuery();
            while( s.next() ) { lQ = s.getInt("louvainIdx"); }
            sent.setInt(1, sIdR);
            s = sent.executeQuery();
            while( s.next() ) { lR = s.getInt("louvainIdx"); }
            
            edgeWeights[++eIdx] = score;
            edges[0][eIdx] = lQ;
            edges[1][eIdx] = lR;
        }
        
        // Leiden is supposedly more accurate than Louvain
        return Clusters.generateLeiden( (int)clc, edges, edgeWeights );
    }
    
        
    /**
     * Update those sentences in a cluster.
     * @param conn
     * @param c 
     * @throws java.sql.SQLException 
     */
    public static void updateSentenceCluster( Connection conn, Clustering c )
            throws SQLException
    {
        int clusterNum = 0;
        int[][] npc = c.getNodesPerCluster();
        //        System.out.println(Arrays.deepToString(npc));
        
        // Set clusters against sentences
        PreparedStatement ups = conn.prepareStatement("update Sentence set clusterNum = ? where louvainIdx = ?");
        for( int[] npc1: npc )
        {
            if( npc1.length > 1 )
            {
                for( int jj = 0; jj < npc1.length; jj++)
                {
                    ups.setInt(2, npc1[jj]);
                    ups.setInt(1,clusterNum);
                    ups.execute();
                }
                ++clusterNum;
            }
        }
    }

    
    /**
     * Write tokens
     * @param conn
     * @throws SQLException 
     */
    private static void writeTokens( HikariDataSource connection, CompletionService<Integer> cs, int numSents )
            throws SQLException, InterruptedException, ExecutionException
    {
        System.out.println("Writing tokens...");
        
        int tCnt = 0;
        try
        {
            Connection conn1 = connection.getConnection();
            PreparedStatement sent = conn1.prepareStatement("select Sentence.id as sid, sentence from Sentence");
            ResultSet ress = sent.executeQuery();
            while( ress.next() )
            {
                System.out.print(".");
                String tknList = ress.getString("sentence");
                List<String> ls = Arrays.asList(tknList.split("\\s*[ ]\\s*"));
                
                Connection conn = connection.getConnection();
                PreparedStatement tkn = conn.prepareStatement("insert into documents.Token values(default,?,default,default,default,?)");
                ls.forEach(t -> {
                    try
                    {
                        tkn.setString(1, t);
                        tkn.setInt(2, ress.getInt("sid"));
                        tkn.addBatch();
                    } catch (SQLException ex) {
                        Logger.getLogger("JOOQTest").log(Level.SEVERE, null, ex);
                    }
                });

                cs.submit(new DbWriteBatch(tCnt++,tkn));
            }
        } catch (SQLException ex) {
            Logger.getLogger("JOOQTest").log(Level.SEVERE, null, ex);
        }
        
        // For each completion thread
        System.out.println("\r\nWrite tokens: Threads completing...");
        for( int ii=0; ii<tCnt; ii++ )
        {
            Integer p = cs.take().get();
        }
    }
    
    /**
     * Number of individual sentences in which lemma appears
     * Neo4J returns long, hence Long in Map.
     * @return 
     */
    private static Map<String,Integer> lemmaSentCount( Connection conn )
            throws SQLException
    {
        System.out.println("Lemma sentence count...");
        Map<String,Integer> lemmaSentCount = new HashMap<>();

        PreparedStatement tok = conn.prepareStatement("select token,count(distinct(sn)) as sc from Token,CWcount where token=matchWord group by matchWord");
        ResultSet toks = tok.executeQuery();
        while( toks.next() )
            { lemmaSentCount.put(toks.getString("token"),toks.getInt("sc")); }
        
        return lemmaSentCount;
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
        int NUMSENTSGEN = 4;
        
        // Empty Sentence table
        DbUtils.emptyDocumentsTable(connection.getConnection(), "Sentence");
        DbUtils.emptyDocumentsTable(connection.getConnection(), "QRscoreCW");
        DbUtils.emptyDocumentsTable(connection.getConnection(), "CWcount");
        DbUtils.emptyDocumentsTable(connection.getConnection(), "Token");
        
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
	CompletionService<Integer> cs = new ExecutorCompletionService<>(execsrv);
        
        // Timer
        Instant st = Instant.now();
                
        // Write sentence tokens (autocommit=true)
        for( int ii=0; ii<NUMSENTSGEN; ii++ )
        {
            // Connect to document db.  *** Note connection 'hides' SQL errors in ps below (eg: wrong number cols in values()).
            Connection conn = connection.getConnection();
            PreparedStatement ps = conn.prepareStatement("insert into documents.Sentence values(default,?,default,default,default,default,0,default)");
        
//            ps.setString(2,"quick brown fox " +ii);
            ps.setString(1,"The quick brown fox " +ii);
            ps.addBatch();
//            ps.setString(2,"argle bargle gargle " +ii);
            ps.setString(1,"argle fox bargle said the gargle fox " +ii);
            ps.addBatch();
//            ps.setString(2,"compare summer day " +ii);
            ps.setString(1,"Should I compare thee to a summer's day " +ii);
            ps.addBatch();
//            ps.setString(2,"entry form mark asterisk " +ii);
            ps.setString(1,"Complete all entries in this form marked with an asterisk " +ii);
            ps.addBatch();
            
            // Run a thread
            cs.submit(new DbWriteBatch(ii,ps));
        }
        
        // For each completion thread
        System.out.println("Sentence threads completing...");
        for( int ii=0; ii<NUMSENTSGEN; ii++ )
        {
            Integer p = cs.take().get();
//            System.out.print(" " +p);
        }

        // How many sentences got written?  Should be threads*4
        int numSents = 0;
        try( Connection conn = connection.getConnection() )
        {
            numSents = DbUtils.countDocumentsTableRows(conn, "Sentence");
            System.out.println("...> Num. sentences: " +numSents);
        }
        
        /*
         * Write tokens, needs multiple connections
         */
        writeTokens( connection, cs, NUMSENTSGEN );

        /*
         * Process sentences from db and write counts
         */
        countLemmas( connection, numSents );
        
        /*
         * Similarity score
         */
        similarity( connection.getConnection(), numSents );
        
        /*
         * Louvain index
         */
        louvainIdx( connection.getConnection(), numSents );
        
        /*
         * Clusters
         */
        System.out.println("... Generate sentence clusters");
        Clustering c = clustersGen(connection.getConnection());

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
        
        /*
         * Set cluster num against sentences
         */
        updateSentenceCluster(connection.getConnection(),c);
        
        // How long did all this take?
        Instant en = Instant.now();
        long secs = Duration.between(st,en).toSeconds();
        System.out.println("\r\n...> Write time sentences (secs): " + secs);
        
        // Closing
        System.out.println("Closing...");
        execsrv.shutdown();
        connection.close();
    }
    
/*
 * Utils
 * -----
 */
    /**
     * Dump lemma count map
     * @param wCountQR 
     */
    public static void dumpLemmaCountMap(Map<String,FakeCWcount> wCountQR)
    {
        wCountQR.forEach((k,v) -> {
            System.out.println("Lemma: " +k);
            System.out.println("   Matched word: " +v.getMatchedWord()+ ", countQ/countR: " +v.getCountQ()+"/"+v.getCountR());
        });
    }
}


/*
 * Sentence
 * --------
 */
class FakeSentenceNode implements Comparator<FakeSentenceNode>, Comparable<FakeSentenceNode>
{
    private final int       id;
    private final String    sentence;
    private String          tokens = "";
    private List<FakeToken> lt = new ArrayList<>();

    public FakeSentenceNode(int id, String sentence, String tokens)
    {
        this.id = id;
        this.sentence = sentence;
        this.tokens = tokens;
        List<String> ls = Arrays.asList(tokens.split("\\s*[ ]\\s*"));
        ls.forEach(s -> {
            lt.add(new FakeToken(s,s));
        });
    }
    
    public FakeSentenceNode(int id, String sentence, List<FakeToken> lt)
    {
        this.id = id;
        this.sentence = sentence;
        this.lt = lt;
        lt.forEach(t -> {
            tokens.concat(t.getToken());
        });
    }

    public int getId() { return id; }
    public String getSentence() { return sentence; }
    public String getRawTokens() { return tokens; }
    public List<FakeToken> getTokens(){ return lt; }
    
    /**
     * Comparator: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param s1
     * @param s2
     * @return 
     */
    @Override
    public int compare(FakeSentenceNode s1, FakeSentenceNode s2)
    {
        return s1.tokens.length() - s2.tokens.length();
    }
    
    /**
     * Comparable: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param other
     * @return 
     */
    @Override
    public int compareTo(FakeSentenceNode other)
    {
        return this.tokens.length() - other.tokens.length();
    }
    
    /**
     * Equals.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        final FakeSentenceNode other = (FakeSentenceNode) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        return hash;
    }
}

/*
 * Token
 * -----
 */
class FakeToken
{
    private String  token;
    private String  lemma;

    public FakeToken(String token, String lemma)
    {
        this.token = token;
        this.lemma = lemma;
    }

    public String getToken() { return token; }
    public String getLemma() { return lemma; }
}

/*
 * QRscoreCW
 * ---------
 */
class FakeQRscoreCW
{
    private FakeSentenceNode        nodeQ;
    private FakeSentenceNode        nodeR;
    // Score
    private double                  score;
    // Count of matched words
    List<FakeCWcount>               cwCount;


    /**
     * Constructor.
     * @param nodeQ
     * @param nodeR
     * @param cwCount
     * @param score
     */
    public FakeQRscoreCW(FakeSentenceNode nodeQ, FakeSentenceNode nodeR, List<FakeCWcount> cwCount, Double score )
    {
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
    public FakeQRscoreCW(FakeSentenceNode nodeQ, FakeSentenceNode nodeR, List<FakeCWcount> cwCount )
    {
        this( nodeQ,nodeR, cwCount, 0.d );
    }
    
    /**
     * Constructor.
     * @param nodeQ
     * @param nodeR
     * @param score
     */
    public FakeQRscoreCW(FakeSentenceNode nodeQ, FakeSentenceNode nodeR, Double score )
    {
        this(nodeQ,nodeR,new ArrayList<FakeCWcount>(),score);
    }

    /*
     * Getters
     */
    public FakeSentenceNode getNodeQ() { return nodeQ; }
    public FakeSentenceNode getNodeR() { return nodeR; }
    public Double getScore() { return score; }
    public void setScore( double score ) { this.score = score; }

    public List<FakeCWcount> getCwCount() { return cwCount; }
    public void setCwCount(List<FakeCWcount> cwCount) { this.cwCount = cwCount; }
}

/*
 * CWcount
 * -------
 */
class FakeCWcount
{
    private String  matchedWord;
    private int     countQ,                                 // Count of matched word sentence Q
                    countR;                                 // Count sentence R

    /**
     * Constructor.
     * @param matchedWord
     * @param countQ
     * @param countR 
     */
    public FakeCWcount( String matchedWord, int countQ, int countR )
    {
        this.matchedWord = matchedWord;
        this.countQ = countQ;
        this.countR = countR;
    }

    /**
     * Mutators.
     * @return 
     */
    public String getMatchedWord() { return matchedWord; }
    public void setMatchedWord(String matchedWord) { this.matchedWord = matchedWord; }

    public int getCountQ() { return countQ; }
    public void setCountQ(int countQ) { this.countQ = countQ; }

    public int getCountR() { return countR; }
    public void setCountR(int countR) { this.countR = countR; }
}
