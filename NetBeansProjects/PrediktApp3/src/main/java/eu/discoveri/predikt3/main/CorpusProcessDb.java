/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.main;

import com.zaxxer.hikari.HikariDataSource;
import cwts.networkanalysis.Clustering;

import eu.discoveri.predikt3.exceptions.EmptySentenceListException;
import eu.discoveri.predikt3.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt3.exceptions.TokensListIsEmptyException;
import eu.discoveri.predikt3.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt3.exceptions.NoLanguageSetEntriesException;
import eu.discoveri.predikt3.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt3.exceptions.NumberOfNodesException;
import eu.discoveri.predikt3.exceptions.MissingMatchedWordException;

import eu.discoveri.predikt3.cluster.CWcount;
import eu.discoveri.predikt3.cluster.QRscoreCW;
import eu.discoveri.predikt3.config.Constants;
import eu.discoveri.predikt3.config.LangSetup;
import eu.discoveri.predikt3.exceptions.NotEnoughSentencesException;
import eu.discoveri.predikt3.exceptions.SentenceNotPersistedException;
import eu.discoveri.predikt3.graph.Populate;
import eu.discoveri.predikt3.graph.SentenceNode;
import eu.discoveri.predikt3.graph.DbWriteScores;
import eu.discoveri.predikt3.graph.DbWriteSimilarity;
import eu.discoveri.predikt3.lemmatizer.Lemmatizer;
import eu.discoveri.predikt3.sentences.Language;
import eu.discoveri.predikt3.sentences.Nul;
import eu.discoveri.predikt3.sentences.Token;
import eu.discoveri.predikt3.graph.DbWriteClusterNum;
import eu.discoveri.louvaincluster.Clusters;
import eu.discoveri.louvaincluster.NoClustersException;
import eu.discoveri.predikt3.graph.DbSentenceDelete;
import eu.discoveri.predikt3.graph.DbWriteSentenceTokens;
import eu.discoveri.predikt3.utils.Utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CorpusProcessDb
{
    // Number sentences in which each word appears.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<String,Map<String,Nul>>      tokSentCount = new HashMap<>();
    
    // Prevent sentence process duplication, ie: A:B obviates B:A.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<Integer,Map<Integer,Nul>>    noDups = new HashMap<>();
    private static Map<String,Map<String,Nul>>      noDupsTok = new HashMap<>();
    private static Nul                              nul = new Nul();
    
    // Language/Locale
    private final LangSetup     setup;
    private final Language      l;

    // Data source
    HikariDataSource            connection;
    
    // Num. sentences to process
    private long                sentCount = 0L,
                                edgeCount = 0L;

    
    /**
     * Constructor.
     * @param setup 
     * @param connection 
     * @throws java.sql.SQLException 
     */
    public CorpusProcessDb( HikariDataSource connection, LangSetup setup )
            throws SQLException
    {
        // LangSetup
        this.setup = setup;
        // LangSetup language/locale            
        l = setup.getLanguage();
        
        // GraphDb session
        this.connection = connection;
    }
    
    /**
     * Set sentence count (otherwise many methods will throw exception).
     * @param sentCount 
     */
    public void setSentCount( int sentCount ) { this.sentCount = sentCount; }
    
    /**
     * Process sentences by page (LIMIT/OFFSET) [slow].
     * 
     * @param conn
     * @param pme
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws IOException 
     * @throws java.sql.SQLException 
     * @throws eu.discoveri.predikt3.exceptions.SentenceNotPersistedException 
     */
    public void processSentencesByPage( Connection conn, POSTaggerME pme )
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException, SQLException, SentenceNotPersistedException
    {
        int SKIP = 0;
        int LIMIT = Constants.PAGSIZE;

        System.out.println("     Tokenize, clean tokens then lemmatize by page.  Please wait...");
        System.out.print("     ...Pages: ");
        
        while( true )
        {
            List<SentenceNode> lsn = SentenceNode.sentenceByPage( conn, SKIP, LIMIT );
            if( lsn.isEmpty() ) break;

            System.out.print(" ("+(SKIP+1)+")");
            
            // Process this set of sentences
            processSentenceList(conn,pme,lsn);
            
            // Persist and set the Sentence (node/MySQL) id
            for( SentenceNode sn: lsn )
            {
                sn.updateDbWithTokens(conn);
            }
            
            SKIP += LIMIT;
        }
        
        System.out.println("");
    }
    
    /**
     * Process sentences by Id.
     * 
     * @param conn
     * @param cs
     * @param pme
     * @param pageSiz 
     * @throws java.sql.SQLException 
     * @throws eu.discoveri.predikt3.exceptions.EmptySentenceListException 
     * @throws eu.discoveri.predikt3.exceptions.SentenceIsEmptyException 
     * @throws eu.discoveri.predikt3.exceptions.TokensListIsEmptyException 
     * @throws java.io.IOException 
     * @throws eu.discoveri.predikt3.exceptions.SentenceNotPersistedException 
     * @throws java.lang.InterruptedException 
     * @throws java.util.concurrent.ExecutionException 
     */
    public void processSentencesById( Connection conn, CompletionService<Integer> cs, POSTaggerME pme, int pageSiz )
            throws SQLException, EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException, SentenceNotPersistedException, InterruptedException, ExecutionException
    {
        int threadCount1 = 0;
        if( sentCount < 1 )
            throw new SentenceNotPersistedException("No sentences on database?");
        
        System.out.println("     Tokenize, clean tokens then lemmatize by page.  Please wait...");
        System.out.print("     ...Pages: ");
        
        // Form statement
        PreparedStatement ss = SentenceNode.ps4SentenceById(conn,pageSiz);
        PreparedStatement ts = SentenceNode.ps4TokenLemma(conn);
        
        // Get pages of Sentences
        for( int ii=1; ii<=sentCount; ii+=pageSiz )
        {
            // Get a bunch of sentences (page: pageSiz)
            List<SentenceNode> lsn = SentenceNode.sentenceById(ss,ts,pageSiz,ii);
            
            System.out.print(" ("+ii+")");
            
            // Process this set of sentences: Tokenize, clean, lemmatize
            processSentenceList(conn,pme,lsn);
            
            // Persist and set the Sentence (node/MySQL) id
            for( SentenceNode sn: lsn )
            {
                // Enough tokens for similarity test?
                if( sn.getTokens().size() < Constants.MINPAIRCOUNT )
                {
                    // Too few Tokens for processing the sentence
                    cs.submit(new DbSentenceDelete(threadCount1++,connection,sn.getNid()));
                }
                else
                {
                    // A good sentence it seems
                    cs.submit(new DbWriteSentenceTokens(threadCount1++,connection,sn.getTokens(),sn.getNid()));
                }
            }
        }
        
        // For each completion thread
        for( int ii=0; ii<threadCount1; ii++ )
        {
            int l1 = cs.take().get();
        }
        
        System.out.println("");
    }
    
    /**
     * Tokenize a list of sentences, clean the tokens, lemmatize.
     * 
     * @param conn
     * @param pme 
     * @param lsn 
     * @throws eu.discoveri.predikt3.exceptions.EmptySentenceListException 
     * @throws eu.discoveri.predikt3.exceptions.SentenceIsEmptyException 
     * @throws eu.discoveri.predikt3.exceptions.TokensListIsEmptyException 
     * @throws java.io.IOException 
     * @throws java.sql.SQLException 
     * @throws eu.discoveri.predikt3.exceptions.SentenceNotPersistedException 
     */
    public void processSentenceList( Connection conn, POSTaggerME pme, List<SentenceNode> lsn )
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException, SQLException, SentenceNotPersistedException
    {
        rawTokenizeSentenceCorpus(pme, lsn);

        cleanTokensSentenceCorpus(pme, lsn);

        lemmatizeSentenceCorpusViaRdb(conn, lsn);
    }
    
    /**
     * Get POS for each token.
     * 
     * @param pme POSTagger
     * @param sents
     * @return
     * @throws EmptySentenceListException
     * @throws TokensListIsEmptyException 
     */
    public Iterable<SentenceNode> posTagSentenceCorpus(POSTaggerME pme, Iterable<SentenceNode> sents)
            throws EmptySentenceListException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:posTagSentenceCorpus");
        
        for( SentenceNode s: sents )
            s.posTagThisSentence(pme);
        
        return sents;
    }
    
    /**
     * Clean and tokenize all sentences.
     * 
     * @param tme Tokenizer
     * @param pme
     * @param sents
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException 
     * @throws NoLanguageSetEntriesException 
     * @throws TokensListIsEmptyException 
     */
    public Iterable<SentenceNode> tokenizeSentenceCorpus(TokenizerME tme, POSTaggerME pme, Iterable<SentenceNode> sents)
            throws EmptySentenceListException, SentenceIsEmptyException, NoLanguageSetEntriesException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:tokenizeSentenceCorpus()");
        
        // Tokenize and POS
        for( SentenceNode s: sents )
            s.tokPosThisSentence(tme,pme);

        return sents;
    }
    
    /**
     * Simple tokenization.
     * 
     * @param st Tokenizer
     * @param pme
     * @param sents
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     */
    public Iterable<SentenceNode> simpleTokenizeSentenceCorpus(SimpleTokenizer st, POSTaggerME pme, Iterable<SentenceNode> sents)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:simpleTokenizeSentenceCorpus()");
        
        // Tokenize and clean
        for( SentenceNode s: sents )
            s.simpleTokPosThisSentence(st,pme);
        
        return sents;
    }
    
    /**
     * Raw tokenization of the sentence.
     * 
     * @param pme
     * @param sents
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public Iterable<SentenceNode> rawTokenizeSentenceCorpus(POSTaggerME pme, Iterable<SentenceNode> sents)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:rawTokenizeSentenceCorpus()");
        
        Iterator<SentenceNode> iSents = sents.iterator();
        
        // Tokenize and clean
        while( iSents.hasNext() )
        {
            SentenceNode s = iSents.next();
            
            // 1. Remove OOB characters ('nulls' etc.), update the sentence string
            s.updateSentence( l.remOOBChars(s.getSentence()) );
            
            // 2. Tokenize
            List<Token> lt = s.rawTokPosThisSentence(pme);
            if( lt.isEmpty() )
                { iSents.remove(); }
        }
        
        return Utils.iteratorToIterable(iSents);
    }
    
    /**
     * Lemmatize sentence corpus.
     * 
     * @param lemmer
     * @param sents
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws ListLengthsDifferException
     * @throws POSTagsListIsEmptyException 
     */
    public Iterable<SentenceNode> lemmatizeSentenceCorpus(Lemmatizer lemmer, Iterable<SentenceNode> sents, boolean match2NN)
             throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, ListLengthsDifferException, POSTagsListIsEmptyException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! Corpusprocess:lemmatizeSentenceCorpus");
        
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB characters
            s.updateSentence( l.remOOBChars(s.getSentence()) );
            // Tokenize
            s.lemmatizeThisSentence(lemmer,match2NN);
        }
        
        return sents;
    }
    
    /**
     * Lemmatize sentences using default lemmatizer in Populate.
     * 
     * @param popl
     * @param sents
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws ListLengthsDifferException
     * @throws POSTagsListIsEmptyException 
     */
    public Iterable<SentenceNode> lemmatizeSentenceCorpus(Populate popl, Iterable<SentenceNode> sents, boolean match2NN)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, ListLengthsDifferException, POSTagsListIsEmptyException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:lemmatizeSentenceCorpus");
        
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB characters
            s.updateSentence( l.remOOBChars(s.getSentence()) );
            // Tokenize
            popl.lemmasOfSentence(s,match2NN);
        }
        
        return sents;
    }
    
    /**
     * Lemmatize tokens via Db lookup.
     * 
     * @param conn
     * @param sents
     * @return 
     * @throws java.sql.SQLException 
     */
    public Iterable<SentenceNode> lemmatizeSentenceCorpusViaRdb( Connection conn, Iterable<SentenceNode> sents )
            throws SQLException
    {
        // Lemmas SQL
        PreparedStatement ps = conn.prepareStatement("select * from lemma.Word,lemma.Lemma where Word.word=? and Word.POSId=? and Word.lemmaId=Lemma.id");
        
        for( SentenceNode s: sents )
        {
            // Lemmatize
            for( Token t: s.getTokens() )
            {
                ps.setString(1, t.getToken());
                ps.setString(2, t.getPOS());

                ResultSet rs = ps.executeQuery();
                while( rs.next() )
                {
                    t.setPos(rs.getString("Word.POSId"));
                    t.setLemma(rs.getString("Lemma.lemma"));
                }
            }
        }
        
        return sents;
    }
    
    /**
     * *** MAIN CLEAN UP ***
     * Clean up punctuation, apostrophes, stopwords, numbers.
     * 
     * @param pme
     * @param sents
     * @return
     * @throws EmptySentenceListException 
     * @throws TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public Iterable<SentenceNode> cleanTokensSentenceCorpus( POSTaggerME pme, Iterable<SentenceNode> sents )
            throws EmptySentenceListException, TokensListIsEmptyException, IOException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:cleanTokensSentenceCorpus");
        
        // NB: Order is important!
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB chars (before tokenization takes place, see rawTokenize etc.)
            // 2. Remove apostrophes (NB: based on language).  Note: generates more tokens
            List<Token> lts = l.unApostrophe(setup.loadApostrophesProperties(), s.getTokens());
            s.setTokens(lts);

            // 3. All tokens now to lowercase and remove dashes, punctuation etc. (Calls clean()).
            s.cleanTokens(pme);

            // 4. Remove stopwords
            lts = l.remStopWords(setup,lts);
            s.setTokens(lts);

            // 5. Remove numbers
            if( setup.getNoNumbers() )
                { s.removeNumbers(s.getLocale()); }
            
            // 6. Remove peculiarities (owing to eccentric tokenization)
            s.removeUnwanted();

            // 7. Remove unwanted tokens (not matching POS tag list)
            s.keepTokens(setup.getKeepNodes());
        }
        
        return sents;
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
     * Lemma counting between sentence pairs.
     * @param wordsQ
     * @param wordsR
     * @param cqr
     * @return 
     */
    public static Map<String,CWcount> lemmaCompare( List<Token> wordsQ, List<Token> wordsR, CWcount cqr )
    {
        // Map lemma to count
        Map<String,CWcount> wCountQR = new HashMap<>();
        
        // For each lemma in sentenceQ (Note: Q lemma count >= R lemma count)
        for( Token tQ: wordsQ )
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
            for( Token tR: wordsR )
            {
                String wordR = tR.getLemma();   // *MATCH* this lemma

                if( wordR.equals(wordQ) )                               // Words match between sentences?
                { //Yes
                    // First match for token in both Q&R?
                    if( !wCountQR.containsKey(wordQ) )                  // Create Map entry
                    {//Yes
                        wCountQR.put(wordQ, new CWcount(wordQ,1,1));    // Init count of both sentences
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
     * Lemma matching/counting per sentence pair [slow]. Note Token class contains both
     * token and lemma.
     * 
     * @param connection
     * @param cs
     * @param numSents
     * @throws java.sql.SQLException
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws eu.discoveri.predikt3.exceptions.NotEnoughSentencesException
     */
    // Pagination
    static int SKIP0 = 0, SKIP1 = 0, PCNT = 0;
    static int threadCount = 0;
    public void countLemmasByPage( HikariDataSource connection, CompletionService<Integer> cs, int numSents )
            throws SQLException, InterruptedException, ExecutionException, NotEnoughSentencesException
    {
        // Anything to pair process?
        if( numSents < 2 )
            throw new NotEnoughSentencesException("Need at least two sentences to analyse pairs.");
        
        // Tell what's happening
        System.out.println("    Counting by PAGE");
        
        // Calc. num. pairs
        long numPairs = numSents*(numSents-1)/2;

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
        // BUT!! *** Hikari hangs if I lock tables and a subsequent PreparedStatement (on a separate unlocked table!) is used ***
//        Statement lock = conn.createStatement();
//        lock.execute("LOCK TABLES documents.Sentence WRITE");
        
        // Now read the Sentence table and update scores
        while( true )
        {
            // Get a page of SentenceNodes (Qs), create list of Sentence objects
            List<SentenceNode> lsQ = SentenceNode.sentenceByPage(conn,SKIP0,Constants.PAGSIZE);  //********* Slow, if Sentences count large

            /*
             * Sort sentences by tokens' count (long to short hence s2 - s1)
             * Allows sentence matching to work efficiently/properly
             */
            // Default sorted() is on SentenceNode token list size.  "Supplier" to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<SentenceNode>> isnQs = () -> StreamSupport.stream(lsQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            { 
                // Loop over each sentence
                isnQs.get().forEach(snQ -> {
                    // Count of common words in each sentence pair
                    CWcount cqr = null;
                    
                    // Tokens for sentence Q
                    List<Token> wordsQ = snQ.getTokens();

                    // Sentence R set
                    SKIP1 = SKIP0;                                              // Don't need to go right back to beginning
                    while( true )
                    {
                        List<SentenceNode> lsR;
                        try {
                            // Get a page of SentenceNodes (Rs)
                            lsR = SentenceNode.sentenceByPage(conn,SKIP1,Constants.PAGSIZE);// ****************** CHANGE TO ID ***********************
                            
                            // Default sorted() is on SentenceNode token list size.  Supplier, to obviate Stream closing (can only "use" Stream once)
                            Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(lsR.spliterator(),false).sorted();

                            // Loop over sentenceR set
                            if( isnRs.get().iterator().hasNext() )
                            {
                                isnRs.get().forEach(snR -> {
                                    int keyQ = snQ.getNid(), keyR = snR.getNid();
//                                    System.out.print(" [" +keyQ+ "/" +keyR+ "]");

                                    // Check to see if we should process this pair
                                    if( !snQ.equals(snR) && !isStoredPair(keyQ,keyR) )
                                    {
                                        // Num. pairs processed
                                        ++PCNT;

                                        // Got a candidate pair, flag being processed
                                        storePairKeys(keyQ,keyR);

                                        // Tokens for sentence R
                                        List<Token> wordsR = snR.getTokens();

                                        /*
                                         * Now to compare Token:lemmas
                                         */
                                        // Map of count of common lemmas between two sentences <Word,count of Word per sentence pair>
                                        Map<String,CWcount> wCountQR = lemmaCompare(wordsQ,wordsR,cqr);
//                                        dumpLemmaCountMap(wCountQR);

                                        // Ok, store the common (non stop,VB,NN) words counts for the sentence pairs
                                        if( wCountQR.values().size() > Constants.MINPAIRCOUNT )
                                        {
                                            cs.submit(new DbWriteScores(threadCount++,connection,new QRscoreCW(snQ,snR, new ArrayList<>(wCountQR.values()))));
                                        }
                                    }
                                });
                            }
                            else
                            {
                                break;
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger("CorpusProcessDb").log(Level.SEVERE, null, ex);
                        }
                        SKIP1 += Constants.PAGSIZE;
                    } // R loop
                });

                // How far along are we?
                System.out.format(" %6.2f%s", (float)PCNT*100./numPairs,"%");
            }
            else
                break;
            
            SKIP0 += Constants.PAGSIZE;
        }  // Q loop
        System.out.println("\r\nPCNT: " +PCNT+ ", threadCount: " +threadCount+ " waiting for completion...");

        // For each completion thread
        for( int ii=0; ii<threadCount; ii++ )
        {
            int l1 = cs.take().get();
        }

        // Unlock Sentence table
//        lock.execute("UNLOCK TABLES");
    }
    
    
    /**
     * Lemma matching/counting per sentence pair. Note Token class contains both
     * token and lemma.
     * 
     * @param connection
     * @param cs
     * @param numSents
     * @throws SQLException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NotEnoughSentencesException 
     */
    public void countLemmasById( HikariDataSource connection, CompletionService<Integer> cs, int numSents )
            throws SQLException, InterruptedException, ExecutionException, NotEnoughSentencesException
    {   
        // Anything to pair process?
        if( numSents < 2 )
            throw new NotEnoughSentencesException("Need at least two sentences to analyse pairs.");

        // Tell what's happening
        System.out.println("    Counting by ID, percentage complete:");

        // Calc. num. pairs
        long numPairs = numSents*(numSents-1)/2;

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
        // And now prepare a statement for Id call
        PreparedStatement ss = SentenceNode.ps4SentenceById(conn,Constants.PAGSIZE);
        PreparedStatement ts = SentenceNode.ps4TokenLemma(conn);
        
        // Sentence range
        int sid = -1, lid = -1;                                                 // Start/Last sentence ids
        Statement st = conn.createStatement();
        ResultSet sents = st.executeQuery("select min(id) as sid, max(id) as lid from Sentence");
        while( sents.next() )
        {
            sid = sents.getInt("sid");
            lid = sents.getInt("lid");
        }
        
        /*
         * Now read the Sentence table and update scores
         */
        for( int ii=sid; ii<=lid; ii+=Constants.PAGSIZE )
        {
            // Get a page of SentenceNodes (Qs), create list of Sentence objects
            List<SentenceNode> lsQ = SentenceNode.sentenceById(ss,ts,Constants.PAGSIZE,ii);

            /*
             * Sort sentences by tokens' count (long to short hence s2 - s1)
             * Allows sentence matching to work efficiently/properly
             */
            // Default sorted() is on SentenceNode token list size.  "Supplier" to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<SentenceNode>> isnQs = () -> StreamSupport.stream(lsQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            { 
                // Loop over each sentence
                for( SentenceNode snQ: (Iterable<SentenceNode>)isnQs.get()::iterator )
                {
                    // Count of common words in each sentence pair
                    CWcount cqr = null;
                    
                    // Tokens for sentence Q
                    List<Token> wordsQ = snQ.getTokens();

                    // Sentence R set
                    for( int jj=snQ.getNid()+1; jj<=lid; jj+=Constants.PAGSIZE )
                    {
                        List<SentenceNode> lsR;
                        try {
                            // Get a page of SentenceNodes (Rs)
                            lsR = SentenceNode.sentenceById(ss,ts,Constants.PAGSIZE,jj);
                            
                            // Default sorted() is on SentenceNode token list size.  Supplier, to obviate Stream closing (can only "use" Stream once)
                            Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(lsR.spliterator(),false).sorted();

                            // Loop over sentenceR set
                            if( isnRs.get().iterator().hasNext() )
                            {
                                isnRs.get().forEach(snR -> {
                                    // Tokens for sentence R
                                    List<Token> wordsR = snR.getTokens();

                                    // Map of count of common lemmas between two sentences <Word,count of Word per sentence pair>
                                    Map<String,CWcount> wCountQR = lemmaCompare(wordsQ,wordsR,cqr);
//                                    dumpLemmaCountMap(wCountQR);

                                    // Ok, store the common (non stop,VB,NN) words counts for the sentence pairs (if two or more words match)
                                    if( wCountQR.values().size() > 1 )
                                    {
                                        cs.submit(new DbWriteScores(threadCount++,connection,new QRscoreCW(snQ,snR, new ArrayList<>(wCountQR.values()))));
                                    }
                                    
                                    ++PCNT;                                     // Num. pairs processed
                                });
                            }
                            else
                            {
                                break;
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger("CorpusProcessDb").log(Level.SEVERE, null, ex);
                        }
                    } // R loop
                }

                // How far along are we?
                System.out.format(" %6.2f%s", (float)PCNT*100./numPairs,"%");
            }
            else
                break;
        }  // Q loop
        
        System.out.println("\r\nPCNT: " +PCNT+ ", threadCount: " +threadCount+ " waiting for completion...");

        // For each completion thread
        for( int ii=0; ii<threadCount; ii++ )
        {
            int l1 = cs.take().get();
        }
    }
    
    
    /**
     * Number of individual sentences in which lemma appears.
     * @return 
     */
    private static Map<String,Integer> lemmaSentCount( Connection conn )
            throws SQLException
    {
        System.out.println("Lemma sentence count (being count of individual sentences in which each lemma occurs)...");
        Instant start = Instant.now();
        Map<String,Integer> lemmaSentCount = new HashMap<>();

        // Staggeringly slow and memory hungry, hence...
//        PreparedStatement tok = conn.prepareStatement("select lemma,count(distinct(sn)) as sc from Token,CWcount where lemma=matchWord group by matchWord");

        // ...use loop in code instead
        PreparedStatement sCount = conn.prepareStatement("select count(distinct(sn)) as sc from Token where lemma=?");
        Statement st = conn.createStatement();
        ResultSet lemmas = st.executeQuery("select matchWord from CWcount group by matchWord");
        while( lemmas.next() )
        {
            String lemma = lemmas.getString("matchWord");
            sCount.setString(1, lemma);
            ResultSet toks = sCount.executeQuery();
            while( toks.next() )
                { lemmaSentCount.put(lemma,toks.getInt("sc")); }
        }

        System.out.println("  ..> Lemma sentence count: " +lemmaSentCount.size()+ " complete (secs): " +Duration.between(start,Instant.now()).toSeconds());
        return lemmaSentCount;
    }
    
    /**
     * Similarity scores. Determines topically similar sentences (pairs) and is
     * used as the weight of the connecting "edge" between pairs in graph (IDF-weighted 
     * word overlap).For algorithm, see Allan et al."Retrieval and Novelty Detection
     * at the Sentence Level" and Metzler et al, "Similarity Measures for Tracking
     * Information Flow".
     * 
     * @param conn
     * @param cs
     * @param sentCount
     * @throws eu.discoveri.predikt3.exceptions.MissingMatchedWordException
     * @throws java.sql.SQLException
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public void similarity(Connection conn, CompletionService<Integer> cs, int sentCount)
            throws MissingMatchedWordException, SQLException, InterruptedException, ExecutionException
    {
        // CWcount begin/end
        long idBegin=-1, idEnd=-1;
        int threadCount1 = 0;
        
        // Get QR matched words
        PreparedStatement qrsc = conn.prepareStatement("select id,matchWord,countQ,countR,qrscwId from CWcount where qrscwId=? order by qrscwId");
        
        // Lemma count (per sentence)
        Map<String,Integer> lemmaSentCount = lemmaSentCount(conn);
        
        // Statements
        Statement st = conn.createStatement();
        // Start/End ids (Saves having to form a massive ResultSet [20 billion!] as id is auto_increment)
        ResultSet idSt = st.executeQuery("select min(qrscwId) idBegin from CWcount");
        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
        ResultSet idEn = st.executeQuery("select max(qrscwId) idEnd from CWcount");
        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
        st.close();
        
        // Update similarity scores:
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
            
            // Update the score (if significant) and index the relevant Sentences
//            if( score < Constants.EDGEWEIGHTMIN )
//            {
//                score = Constants.EDGEWEIGHTNRZERO;
//            }

            cs.submit(new DbWriteSimilarity(threadCount1++,connection,score,id));
        }
        
        System.out.println("Similarity threadCount: " +threadCount1+ " waiting for thread completion...");

        // For each completion thread
        for( int ii=0; ii<threadCount1; ii++ )
        {
            int l1 = cs.take().get();
        }
    }
    
    /**
     * Scores statistics.  Output used by Apache Commons Maths StatUtils.
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    public double[] adjustScores(Connection conn)
            throws SQLException
    {
        // Num. entries
        long    sCnt = 0;
        int     sIdx = 0;
        
        // Get QR matched words
        Statement qrsc = conn.createStatement();
        ResultSet sc = qrsc.executeQuery("select count(*) as sc from QRscoreCW");
        while( sc.next() ) { sCnt = sc.getLong("sc"); }
        double[] scEntries = new double[(int)sCnt];
        
        ResultSet scores = qrsc.executeQuery("select score from QRscoreCW");
        while( scores.next() )
        {
            scEntries[sIdx++] = scores.getDouble("score");
        }
        
        return scEntries;
    }
    
    /**
     * Form "edge weights" from QRscores (nodeQ-nodeR edge weighting). Where
     * edge weight is significant (EDGEWEIGHTMIN) give relevant Sentences a
     * Louvain cluster index number.
     * 
     * @param conn
     * @param cs
     * @param numSents
     * @throws java.lang.InterruptedException 
     * @throws java.util.concurrent.ExecutionException 
     * @throws java.sql.SQLException 
     */
    public void writeClusterIndices(Connection conn, CompletionService<Integer> cs, int numSents)
            throws InterruptedException, ExecutionException, SQLException
    {
        // QRscoreCW begin/end
        long idBegin=-1, idEnd=-1;
        int threadCount1 = 0;
        // Louvain index
        int louvainIdx = 0;
        
        // Get QR matched words
        PreparedStatement qrsc = conn.prepareStatement("select score, sIdQ, sIdR from QRscoreCW where id = ?");
        
        // Statements
        Statement st = conn.createStatement();
        // Start/End ids (Saves having to form a massive ResultSet [upto 5 billion!] as id is auto_increment)
        ResultSet idSt = st.executeQuery("select min(id) idBegin from QRscoreCW");
        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
        ResultSet idEn = st.executeQuery("select max(id) idEnd from QRscoreCW");
        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
        
        // List of Sentence ids (array goes from zero ids from 1, hence +1)
        boolean sids[] = new boolean[numSents+1];
        Arrays.fill(sids,false);                    // Make sure all false to start
        
        for( long id=idBegin; id<=idEnd; id++ )
        {
            qrsc.setLong(1, id);
            ResultSet res = qrsc.executeQuery();
            
            while( res.next() )
            {
                double score = res.getDouble("score");
//                if( score > Constants.EDGEWEIGHTMIN )
//                {
                    int snQ = res.getInt("sIdQ");
                    int snR = res.getInt("sIdR");
                    
                    // Check if already set
                    if( !sids[snQ] ) // Q of pair
                    {
                        cs.submit(new DbWriteClusterNum(threadCount1++,connection,++louvainIdx,snQ));
                        sids[snQ] = true;
                    }
                    if( !sids[snR] ) // R of pair
                    {
                        cs.submit(new DbWriteClusterNum(threadCount1++,connection,++louvainIdx,snR));
                        sids[snR] = true;
                    }
//                }
            }
        }
            
        System.out.println("Cluster num. threadCount: " +threadCount1+ " waiting for thread completion...");

        // For each completion thread
        for( int ii=0; ii<threadCount1; ii++ )
        {
            int l1 = cs.take().get();
        }
    }

    /**
     * Generate sentence clusters.
     * 
     * @param conn
     * @param numSents
     * @return
     * @throws NumberOfNodesException
     * @throws java.sql.SQLException
     * @throws eu.discoveri.louvaincluster.NoClustersException
     */
    public Clustering clustersGen(Connection conn, int numSents)
            throws NumberOfNodesException, SQLException, NoClustersException
    {
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
        
        // Similar "edges" between sentences (nodes) *****---> MAY exceed Integer.MAX ---> May need to change source code cwts.networkanalysis
        ResultSet scrs = st.executeQuery("select score,sIdQ,sIdR from QRscoreCW");
        
        while( scrs.next() )
        {
            int sIdQ = scrs.getInt("sIdQ"); int sIdR = scrs.getInt("sIdR");
            double score = scrs.getDouble("score");
            
            edgeWeights[++eIdx] = score;
            edges[0][eIdx] = sIdQ-1;
            edges[1][eIdx] = sIdR-1;
        }
        
        // Leiden is supposedly more accurate than Louvain
        return Clusters.generateLeiden( numSents, edges, edgeWeights );
    }
    
    /**
     * Update those sentences in a cluster.
     * @param conn
     * @param c 
     * @throws java.sql.SQLException 
     */
    public void updateSentenceCluster( Connection conn, Clustering c )
            throws SQLException
    {
        int clusterNum = 0;
        int[][] npc = c.getNodesPerCluster();
        //        System.out.println(Arrays.deepToString(npc));
        
        // Set clusters against sentences
        PreparedStatement ups = conn.prepareStatement("update Sentence set clusterNum = ? where id = ?");
        for( int[] npc1: npc )
        {
            if( npc1.length > 1 )
            {
                for( int jj = 0; jj < npc1.length; jj++ )
                {
                    // Offset from Leiden cluster num to Sentence id (+1)
                    ups.setInt(2, npc1[jj]+1);
                    ups.setInt(1,clusterNum);
                    ups.execute();
                }
                ++clusterNum;
            }
        }
    }

    
    
//=================================[Dump stuff]==========================================
    /**
     * From db: Score of sentence pair check
     * @param conn
     * @throws java.sql.SQLException
     */
    public void score2Sent(Connection conn)
            throws SQLException
    {
        System.out.println("...Score to Sents check");
        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet scores = st.executeQuery("select id,score,sIdQ,sIdR from QRscoreCW");
        while( scores.next() )
        {
            System.out.format( "[%d] %6.3f Q%d:R%d", scores.getLong("id"), scores.getDouble("score"), scores.getLong("sIdQ"), scores.getLong("sIdR") );
        }
    }
    
    /**
     * Count of "edges".
     * @param conn
     * @return
     * @throws SQLException 
     */
    public int sentenceEdgeCount(Connection conn)
            throws SQLException
    {
        int c = 0;
        Statement st = conn.createStatement();
        ResultSet cnt = st.executeQuery("select count(*) as c from QRscoreCW");
        while( cnt.next() ) { c = cnt.getInt("c"); }
        
        return c;
    }
    
    /**
     * Count of sentence "nodes".
     * @param conn
     * @return
     * @throws SQLException 
     */
    public int sentenceNodeCount(Connection conn)
            throws SQLException
    {
        int c = 0;
        Statement st = conn.createStatement();
        ResultSet cnt = st.executeQuery("select count(*) as c from Sentence");
        while( cnt.next() ) { c = cnt.getInt("c"); }
        
        return c;
    }
    
    /**
     * From db: Score to CWcount check
     * @param conn
     * @throws java.sql.SQLException
     */
    public void score2Count(Connection conn)
            throws SQLException
    {
        System.out.println("...Score to CWcounts check");
        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet scores = st.executeQuery("select CWcount.id as cid, QRscoreCW.id as sid, matchWord as mw, countQ, countR from QRscoreCW, CWcount where qrscwid=QRscoreCW.id");
        while( scores.next() )
        {
            System.out.format( "QR[%d] CW[%d] %s Q%d:R%d", scores.getLong("cid"), scores.getLong("sid"), scores.getString("mw"), scores.getLong("countQ"), scores.getLong("countR") );
        }
    }

    /**
     * What's in noDups (stored pair)?
     */
    static int numDups = 0;
    public static void dumpStoredPair()
    {
        System.out.println("-----------------[noDups (stored pairs)]------------------");
        noDups.forEach((k,v) -> {
            System.out.println("SentQ: " +k);
            numDups += v.size();
            v.forEach((k0,v0) -> {
                System.out.println("  SentR: " +k0);
            });
        });
        System.out.println("Total entries: " +numDups);
        System.out.println("----------------------------------------------------------");
    }
    
    /**
     * Dump lemma count map
     * @param wCountQR 
     */
    public static void dumpLemmaCountMap(Map<String,CWcount> wCountQR)
    {
        wCountQR.forEach((k,v) -> {
            System.out.println("Lemma: " +k);
            System.out.println("   Matched word: " +v.getMatchedWord()+ ", countQ/countR: " +v.getCountQ()+"/"+v.getCountR());
        });
    }
    
    /**
     * Dump lemma sentence count.Number of sentences in which each lemma appears.
     * @param lemmaSentCount
     */
    public void dumpTLemmaSentCount(Map<String,Integer> lemmaSentCount)
    {
        lemmaSentCount.forEach((k,v) -> {System.out.println(":::> MWrd: " +k+ ", Sent cnt: " +v);});
    }
    
        
    /**
     * Dump: Sentence list in which lemma appears  ???????
     */
    static Map<String,Map<String,Nul>>  lemmaSentList = new HashMap<>();
    private static void dumpLemmaSentList()
    {
        lemmaSentList.forEach((k,v) -> {
            System.out.println(" k: " +k);
            v.forEach((k1,v1) -> {
                System.out.println("  Sent: " +k1);
            });
        });
    }
    
    /**
     * Dump: Num. sentences in which each lemma appears
     */
    private static void dumpLemmaSentCount(Map<String,Integer> lemmaSentCount)
    {
        lemmaSentCount.forEach((k,v) -> {
            System.out.println("k: " +k+ ", count: " +v);
        });
    }
    
    /**
     * Count sentences per cluster.
     * @param conn
     * @throws java.sql.SQLException
     */
    public static void dumpClusterSentCount(Connection conn)
            throws SQLException
    {
        System.out.println("Cluster sentence count");
        
        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet sent = st.executeQuery("select distinct clusterNum,count(sentence) as cs from Sentence group by clusterNum");
        while( sent.next() )
        {
            System.out.format( "%d: %d", sent.getInt("clusterNum"), sent.getInt("cs") );
        }
    }
    
    /**
     * Show sentences per cluster.
     * @param conn
     * @throws java.sql.SQLException
     */
    public static void dumpClusterSentences(Connection conn)
            throws SQLException
    {
        System.out.println("...Cluster sentences");
        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet sent = st.executeQuery("select id, sentence,clusterNum from Sentence order by clusterNum");
        while( sent.next() )
        {
            System.out.format( "[%d] %d: %s", sent.getLong("tid"), sent.getInt("clusterNum"), sent.getString("sentence") );
        }
    }
    
    /**
     * Dump lemmas per cluster.
     * @param conn
     * @throws java.sql.SQLException
     */
    public static void dumpClusterLemmas(Connection conn)
            throws SQLException
    {
        System.out.println("...Cluster tokens/lemmas");
        // Statements
        Statement st = conn.createStatement();

        // Number scores
        ResultSet tkns = st.executeQuery("select Token.id as tid,token,lemma,sn,clusterNum from Token,Sentence where Sentence.id=sn and clusterNum>=0 order by clusterNum");
        while( tkns.next() )
        {
            System.out.format( "[%d] T:%s L:%s in %d, C:%d", tkns.getLong("tid"), tkns.getString("token"), tkns.getString("lemma"), tkns.getLong("sn"), tkns.getLong("clusterNum") );
        }
    }
}
