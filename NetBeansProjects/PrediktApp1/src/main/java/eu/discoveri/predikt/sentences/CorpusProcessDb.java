/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.sentences;

import cwts.networkanalysis.Clustering;

import eu.discoveri.predikt.exceptions.EmptySentenceListException;
import eu.discoveri.predikt.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;
import eu.discoveri.predikt.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt.exceptions.NoLanguageSetEntriesException;
import eu.discoveri.predikt.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt.exceptions.NumberOfNodesException;

import eu.discoveri.predikt.cluster.CWcount;
import eu.discoveri.predikt.cluster.QRscoreCW;
import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.exceptions.MissingMatchedWordException;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceEdge;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.DbWriteScores;
import eu.discoveri.predikt.graph.service.QRscoreCWService;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import eu.discoveri.predikt.lemmatizer.Lemmatizer;
import eu.discoveri.predikt.config.LangSetup;
import eu.discoveri.louvaincluster.Clusters;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Collection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CorpusProcessDb
{
    // Number sentences in which each word appears.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<String,Map<String,Nul>>  tokSentCount = new HashMap<>();
    
    // Prevent sentence process duplication, ie: A:B obviates B:A.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<String,Map<String,Nul>>  noDups = new HashMap<>();
    private static Nul                          nul = new Nul();
    
    // Language/Locale
    private final LangSetup                     setup;
    private final Language                      l;
    
    // Graph database service
    private final DiscoveriSessionFactory       discSess;
    private final Session                       sess;
    private final QRscoreCWService              qrscws;
    
    // Num. sentences to process
    private long                                sentCount = 0L,
                                                edgeCount = 0L;

    
    /**
     * Constructor.
     * @param setup 
     * @param discSess 
     * @param qrscws 
     */
    public CorpusProcessDb( LangSetup setup, DiscoveriSessionFactory discSess, QRscoreCWService qrscws )
    {
        // LangSetup
        this.setup = setup;
        // LangSetup language/locale            
        l = setup.getLanguage();
        
        // GraphDb session
        this.discSess = discSess;
        this.sess = discSess.getNewSession();
        
        // Scoring
        this.qrscws = qrscws;
    }
    
    /**
     * Number sentences to be processed.
     * @return 
     */
    public long sentenceNodeCount()
    {
        sentCount = sess.countEntitiesOfType(SentenceNode.class);
        return sentCount;
    }
    
    /**
     * Number sentence edges.  @TODO: Count all edges of all types??
     * @return 
     */
    public long sentenceEdgeCount()
    {
        edgeCount = sess.countEntitiesOfType(SentenceEdge.class);
        return edgeCount;
    }
    
    /**
     * Process sentences by page.
     * @param conn
     * @param pme
     * @param sns
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws IOException 
     * @throws java.sql.SQLException 
     */
    public void processSentences( Connection conn, POSTaggerME pme, SentenceNodeService sns )
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException, SQLException
    {
        int SKIP = 0;
        int LIMIT = Constants.PAGSIZE;

        System.out.println("     Tokenize, clean tokens then and lemmatize by page:");
        while( true )
        {
            Iterable<SentenceNode> isn = sess.loadAll(SentenceNode.class, new Pagination(SKIP,LIMIT));

            if( isn.iterator().hasNext() )
            {
                System.out.print("     ... Page ("+(SKIP+1)+")");
                rawTokenizeSentenceCorpus(pme,isn);

                cleanTokensSentenceCorpus(isn);

                lemmatizeSentenceCorpusViaRdb(conn, isn);
                
                isn.forEach(sn -> sn.persist(sns));
            }
            else
                break;
            
            ++SKIP;
        }
        
        System.out.println("");
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
        
        // Tokenize and clean
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB characters, update the sentence string
            s.updateSentence( l.remOOBChars(s.getSentence()) );
            // Tokenize
            s.rawTokPosThisSentence(pme);
        }
        
        return sents;
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
        PreparedStatement ps = conn.prepareStatement("select * from Word,Lemma where Word.word=? and Word.POSId=? and Word.lemmaId=Lemma.id");
        
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
     * @param sents
     * @return
     * @throws EmptySentenceListException 
     * @throws TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public Iterable<SentenceNode> cleanTokensSentenceCorpus( Iterable<SentenceNode> sents )
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
            s.cleanTokens();

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
     * Token matching/counting per sentence pair.Note Token class contains
     * both token and lemma.
     * 
     * @param sents
     * @return 
     * @throws EmptySentenceListException 
     */
    public Iterable<SentenceNode> countingTokens(Iterable<SentenceNode> sents)
            throws EmptySentenceListException
    {
        // Check we have sentences
        if( sentCount == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:countingTokens()");
        
        /*
         * Sort sentences by tokens' count (long to short hence s2 - s1)
         * Allows sentence matching to work efficiently/properly
         */
        List<SentenceNode> nodeList = StreamSupport.stream(sents.spliterator(),false)
                                .sorted((s1,s2) -> s2.getTokens().size()-s1.getTokens().size())
                                .collect(Collectors.toList());

        /*
         * Count common words per sentence pair (Q,R).
         * [Num sentence pairs = (sents.size*sents.size-1)/2.]
         */
        // ---> For each sentence (Q) 'Source' sentence
        for( SentenceNode nodeQ: nodeList )
        {
            // Count of common words in each sentence of pair
            CWcount cqr; 
            
            // 'Name of source Sentence(Node) Q
            String nameQ = nodeQ.getName();
            
            // Get words/tokens of sentenceQ (of NodeQ)
            List<Token> wordsQ = nodeQ.getTokens();

            // ---> For each sentence (R) 'Target' sentence
            for( SentenceNode nodeR: nodeList )
            {
                // Name of target Sentence(Node) R
                String nameR = nodeR.getName();

                /*
                 * Do not process sentences if...
                 */
                // ...same sentence
                if( nameR.equals(nameQ) ) continue;
                
                // ...pair already processed. That is, processing B:A but A:B done already
                if( !noDups.containsKey(nameQ) )
                {
                    if( !noDups.containsKey(nameR) )
                    {
                        // Start this sentence pair Q:R
                        Map<String,Nul> nulMap = new HashMap<>();
                        nulMap.put(nameR,nul); 
                        noDups.put(nameQ,nulMap);
                    }
                    else
                        // Key already exists, in future process as else below
                        continue;
                }
                else
                    if( !noDups.get(nameQ).containsKey(nameR) )
                    {
                        noDups.get(nameQ).put(nameR, nul);
                    }

                /*
                 * Ok, got two candidate sentences.  Do matching token counting.
                 */
                // Get words/tokens of sentenceR (of NodeR)
                List<Token> wordsR = nodeR.getTokens();

                /*
                 * Now compare tokens of sentences Q:R
                 */
                // Map of count of common words between two sentences <Word,count of Word per sentence pair>
                Map<String,CWcount> wCountQR = new HashMap<>();
                
                // For each token in sentenceQ (Note: Q token count >= R token count)
                for( Token tQ: wordsQ )
                {
                    String wordQ = tQ.getToken();           // *MATCH* this token

                    if( wCountQR.containsKey(wordQ) )       // Is this token common between Q&R?
                    { // Yes
                        cqr = wCountQR.get(wordQ);          // Get current count for this token
                        int count = cqr.getCountQ();        // Count in Q
                        cqr.setCountQ(++count);             // Increment
                        wCountQR.replace(wordQ, cqr);       // Update
                        
                        // Already counted all of R (where token key is created below), so skip R
                        continue;
                    }

                    // Match Q token against each token in target sentenceR
                    for( Token tR: wordsR )
                    {
                        String wordR = tR.getToken();       // *MATCH* this token
                        
                        if( wordR.equals(wordQ) )                               // Words match between sentences?
                        { //Yes
                            // First match for token in both Q&R?
                            if( !wCountQR.containsKey(wordQ) )                  // Create Map entry
                            {//Yes
                                wCountQR.put(wordQ, new CWcount(wordQ,1,1));    // Init count of both sentences
                            }
                            else
                            {//No
                                // Ok, get the counts for this token
                                cqr = wCountQR.get(wordR);
                                int count = cqr.getCountR();        // Count for R sentence
                                cqr.setCountR(++count);             // Increment R count
                                wCountQR.replace(wordR, cqr);       // Update
                            }
                        }
                    }
                }
                
                // Ok, store the common words for the sentence pairs
                if( wCountQR.values().size() > 0 )
                {
                    // Initial score is zero.
                    QRscoreCW qrscw = new QRscoreCW( nodeQ,nodeR, new ArrayList<>(wCountQR.values()) );
                    qrscw.persist(qrscws);
                }
            }
        }
        
        return sents;
    }
    
    /**
     * Check if pair already processed.
     * @param keyQ
     * @param keyR 
     */
    public static void storePairKeys(String keyQ, String keyR)
    {
        if( !noDups.containsKey(keyQ) )
        {
            if( !noDups.containsKey(keyR) )
            {
                // Start this sentence pair Q:R
                Map<String,Nul> nulMap = new HashMap<>();
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
    public static boolean isStoredPair(String keyQ, String keyR)
    {
        return (noDups.containsKey(keyQ) && noDups.get(keyQ).containsKey(keyR)) || (noDups.containsKey(keyR) && noDups.get(keyR).containsKey(keyQ));
    }
    
    /**
     * Lemma counting.
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
     * Lemma matching/counting per sentence pair.Note Token class contains both
     * token and lemma.
     * 
     * @param sess
     * @throws EmptySentenceListException 
     */
    // Pagination
    static int SKIP0 = 0, SKIP1 = 0, LIMIT = Constants.PAGSIZE, PCNT = 0;
    static int threadCount = 0;
    public void countingLemmas( Session sess, long numSents )
            throws EmptySentenceListException, InterruptedException, ExecutionException
    {
        // Check we have sentences
        if( numSents == 0L )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:countingLemmas()");
        
        // Calc. num. pairs
        long numPairs = numSents*(numSents-1)/2;
        
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
	CompletionService<Long> cs = new ExecutorCompletionService<>(execsrv);
        
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
        while( true )
        {
            // New session
//            Session sesscl = discSess.getNewSession();
            
            // Get a page of SentenceNodes (Qs)
            Iterable<SentenceNode> isnQ = sess.loadAll(SentenceNode.class, new Pagination(SKIP0,LIMIT));
            
            /*
             * Sort sentences by tokens' count (long to short hence s2 - s1)
             * Allows sentence matching to work efficiently/properly
             * @TODO:   HMMM...  What if lemmas exceeds tokens (blank/null lemmas in Token)?
             */
            // Default sorted() is on SentenceNode token list size.  "Supplier" to obviate Stream closing (can only "use" Stream once)
            Supplier<Stream<SentenceNode>> isnQs = () -> StreamSupport.stream(isnQ.spliterator(),false).sorted();
            
            // Loop over SentenceQ set
            if( isnQs.get().iterator().hasNext() )
            { 
                // Batch scores for thread handling
                List<QRscoreCW> lqrscw = new ArrayList<>();

                // Loop
                isnQs.get().forEach(snQ -> {
                    // Count of common words in each sentence pair
                    CWcount cqr = null;
                    
                    // Tokens for sentence Q
                    List<Token> wordsQ = snQ.getTokens();

                    while( true )
                    {
                        // Get a page of SentenceNodes (Rs)
                        Iterable<SentenceNode> isnR = sess.loadAll(SentenceNode.class, new Pagination(SKIP1,LIMIT));
                        
                        // Default sorted() is on SentenceNode token list size.  Supplier, to obviate Stream closing (can only "use" Stream once)
                        Supplier<Stream<SentenceNode>> isnRs = () -> StreamSupport.stream(isnR.spliterator(),false).sorted();
                        
                        // Loop over sentenceR set
                        if( isnRs.get().iterator().hasNext() )
                        {
                            isnRs.get().forEach(snR -> {
                                String keyQ = snQ.getNameNamespace(), keyR = snR.getNameNamespace();
                    
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
                                     * Now to compare tokens/lemmas
                                     */
                                    // Map of count of common words between two sentences <Word,count of Word per sentence pair>
                                    Map<String,CWcount> wCountQR = lemmaCompare(wordsQ,wordsR,cqr);
                                    
                                    // Ok, store the common (non stop,VB,NN) words counts for the sentence pairs
                                    if( wCountQR.values().size() > 0 )
                                    {
//                                        sesscl.save( new QRscoreCW(snQ,snR, new ArrayList<CWcount>(wCountQR.values())), Constants.DEPTH_ENTITY );
                                        lqrscw.add( new QRscoreCW(snQ,snR, new ArrayList<CWcount>(wCountQR.values())) );
                                    }
                                }
                            });
                        }
                        else
                        {
                            SKIP1 = 0;                                          // Back to beginning
                            break;
                        }

                        ++SKIP1;
                    } // R loop
                });

                // How far along are we?
                System.out.format(" %6.2f%s", (float)PCNT*100./numPairs,"%: ");
                
                /*
                 * Submit batch (pagination)
                 */
                if( !lqrscw.isEmpty() )                                         // Set of scores could be zero
                {
                    final AtomicInteger ctr = new AtomicInteger(0);
                    int lsize = lqrscw.size();
                    System.out.print("[Pairs size: " +lsize);
                    
                    // Split into "n" lists
                    Collection<List<QRscoreCW>> llc = lqrscw.stream()
                                                .collect(Collectors.groupingBy(s -> ctr.getAndIncrement()/Constants.THREADLISTSIZE))
                                                .values();
                    List<List<QRscoreCW>> llqrscw = new ArrayList(llc);

                    llqrscw.forEach(le ->
                    {
                        System.out.print(", thread: " +threadCount);
                        cs.submit(new DbWriteScores(threadCount++,le));
                    });
                    
                    System.out.println("]");
                }
            }
            else
                break;
            
            ++SKIP0;
        }  // Q loop
        
        // Timer
        Instant st = Instant.now();

        // For each completion thread
        for( int ii=0; ii<threadCount; ii++ )
        {
            long l = cs.take().get();
            System.out.print("    Thread done: " +l);
        }

        Instant en = Instant.now();
        long secs = Duration.between(st,en).toMillis();
        System.out.println("\r\n  -> All pair threads completion wait time (secs): " + (float)secs/1000.);
        
        // Shutdown the service
        execsrv.shutdown();
    }
    
    /**
     * Num. individual sentences in which lemma appears
     * Neo4J returns long, hence Long in Map.
     * @return 
     */
    static Map<String,Long> lemmaSentCount = new HashMap<>();
    private Map<String,Long> lemmaSentCount()
    {
        Result result = sess.query( "MATCH (cw:CWcount) RETURN count(cw.matchedWord) as cwc, cw.matchedWord as mw", Collections.EMPTY_MAP );
        result.forEach(entry -> lemmaSentCount.put((String)entry.get("mw"), (long)entry.get("cwc")));
        
        return lemmaSentCount;
    }
    
    /**
     * @TODO: **** Do pairs
     * Num. paired sentences in which lemma appears
     * @return 
     */
    static Map<String,Long> lemmaPairCount = new HashMap<>();
    private Map<String,Long> lemmaPairCount()
    {
        Result result = sess.query( "MATCH (cw:CWcount) RETURN count(cw.matchedWord) as cwc, cw.matchedWord as mw", Collections.EMPTY_MAP );
        result.forEach(entry -> lemmaPairCount.put((String)entry.get("mw"), (long)entry.get("cwc")));
        
        return lemmaPairCount;
    }
    
    /**
     * Similarity scores. Determines topically similar sentences (pairs) and is
     * used as the weight of connecting edge between pairs in graph.IDF-weighted word overlap.
     * For algorithm, see Allan et al.
     * "Retrieval and Novelty Detection at the Sentence Level" and
     * Metzler et al, "Similarity Measures for Tracking Information Flow"
     */
    static double score = 0.d;
    public void similarity()
            throws MissingMatchedWordException
    {
        int LIMIT = Constants.PAGSIZE, SKIP = -Constants.PAGSIZE;
        
        // Num. sentences
        lemmaSentCount();

        // Get scores from qrScore.
        // for each sentence pair in QRscoreCW
        // | for each common/matched word in that pair
        // | |   get tot. num. sents common word appears
        // | |   calc score:
        // | |     get count in sent Q, get count in sent R
        // | |     apply algo
        // | end loop
        // | store similarity score against sentence pair
        // end loop
        while( true )
        {
            SKIP += LIMIT;
            
            // Count per sentence of common words between sentence pairs (QRscoreCW[pair]:CWcount[common word])
            Result result = sess.query( "match (q:QRscoreCW)-[:CWC]->(c) return q, collect(c) as cc SKIP $skip LIMIT $limit", Map.of("skip",SKIP, "limit",LIMIT) );
            Iterator<Map<String,Object>> res = result.iterator();
            if( res.hasNext() )
            {
                while( res.hasNext() )
                {
                    score = 0.d;
                    Map<String,Object> entry = res.next();
                    
                    // QRscoreCW (need whole object)
                    QRscoreCW qrscw = (QRscoreCW)entry.get("q");

                    // CWcount (need whole object)
                    List<CWcount> cwcl = (List<CWcount>)entry.get("cc");
                    for( CWcount c: cwcl )
                    {
                        // Just in case
                        if( !lemmaSentCount.containsKey(c.getMatchedWord()) )
                            throw new MissingMatchedWordException(c.getMatchedWord());

                        score += Math.log(c.getCountQ()+1.d)*Math.log(c.getCountR()+1.d)*Math.log((sentCount+1.d)/(lemmaSentCount.get(c.getMatchedWord())+0.5d));
                    }

                    qrscw.setScore(score);
                    sess.save(qrscw, Constants.DEPTH_ENTITY);
                }
            }
            else
                break;
        }
    }
    
    /**
     * Form SIMILARTO edges from QRscores (nodeQ-nodeR edge weighting).
     * 
     * @param cp
     * @return 
     */
    static long louvainIdx = -1;
    static int SCNT = 0;
    public void formSimilarityEdges()
            throws InterruptedException, ExecutionException
    {
        int LIMIT = Constants.PAGSIZE, SKIP = -Constants.PAGSIZE;
        long numScores = 0;
        
        // Get the count of scores-sentences
        Result results = sess.query("match(q:QRscoreCW) return count(*) as c", Collections.EMPTY_MAP);
        for(Map<String, Object> row: results)
            { numScores = (long) row.get("c"); }
        
        while( true )
        {
            SKIP += LIMIT;
            
            // Get a bunch of scores
            results = sess.query( "match(q:QRscoreCW)-[r:SCORE]->(s:SentenceNode) return q,collect(s) as s SKIP $skip LIMIT $limit", Map.of("skip",SKIP, "limit",LIMIT) );
            Iterator<Map<String,Object>> res = results.iterator();
            
            if( res.hasNext() )
            {
                while( res.hasNext() )
                {
                    // Number processed
                    ++SCNT;
                    
                    // Destructure query
                    Map<String,Object> entry = res.next();
                    
                    // QRscoreCW
                    QRscoreCW qrscw = (QRscoreCW)entry.get("q");
                    // SentenceNode
                    List<SentenceNode> lsn = (List<SentenceNode>)entry.get("s");

                    // Get all edges from QRscore, set Louvain idx on nodes and persist
                    // Not using threads because locking is high with this set.
                    if( qrscw.getScore() > Constants.EDGEWEIGHTMIN )
                    {
                        // Louvain index (determines sentences in a cluster)
                        SentenceNode sn0 = lsn.get(0), sn1 = lsn.get(1);
                        sn0.setLouvainIdx(++louvainIdx);
                        sn1.setLouvainIdx(++louvainIdx);
                        sess.save(sn0,Constants.DEPTH_ENTITY); sess.save(sn1,Constants.DEPTH_ENTITY);
                        
                        // Similarity (SIMILARTO) edge score
                        SentenceEdge se = new SentenceEdge(sn0,sn1,qrscw.getScore());
                        sess.save(se,Constants.DEPTH_ENTITY);
                    }
                }
                
                // How far along are we?
                System.out.format(" %6.2f%s", (float)SCNT*100./numScores,"%");
            }
            else
                break;
        }
        
        System.out.println("");
    }


    /**
     *
     * @return
     * @throws NumberOfNodesException
     */
    static int  eIdx = -1;
    static long clc = 0;
    public Clustering clustersGen()
            throws NumberOfNodesException
    {
        int LIMIT = Constants.PAGSIZE, SKIP = -Constants.PAGSIZE;
        
        // Determine dimensiion for cluster
        Result result = sess.query("MATCH ()-[r:SIMILARTO]->() RETURN count(*) as cr",Collections.EMPTY_MAP);
        result.forEach(e -> {
            clc = (long)e.get("cr") * 2;    // Two nodes for each edge
        });
        
        // Can we cluster?
        if( clc == 0 )
            throw new NumberOfNodesException("Cannot form cluster with zero nodes!");
        
        // Clustering
        double[] edgeWeights = new double[(int)clc];
        int[][] edges = new int[2][(int)clc];
        
        while( true )
        {
            SKIP += LIMIT;
            
            // Get a bunch of scores
            result = sess.query( "match (sa:SentenceNode)-[r:SIMILARTO]->(sb:SentenceNode) return r,sa,sb SKIP $skip LIMIT $limit", Map.of("skip",SKIP, "limit",LIMIT) );
            Iterator<Map<String,Object>> res = result.iterator();
            if( res.hasNext() )
            {
                while( res.hasNext() )
                {
                    Map<String,Object> entry = res.next();

                    // SentenceNodes
                    SentenceNode sa = (SentenceNode)entry.get("sa");
                    SentenceNode sb = (SentenceNode)entry.get("sb");
                    // Edge
                    SentenceEdge se = (SentenceEdge)entry.get("r");
                    
                    edgeWeights[++eIdx] = se.getWeight();
                    edges[0][eIdx] = (int)sa.getLouvainIdx();
                    edges[1][eIdx] = (int)sb.getLouvainIdx();
                }
            }
            else
                break;
        }
        
        // Leiden is supposedly more accurate than Louvain
        return Clusters.generateLeiden( (int)clc, edges, edgeWeights );
    }
    
    /**
     * Update those sentences in a cluster.
     * @param c 
     */
    public void updateSentenceCluster( Clustering c )
    {
        int clusterNum = 0;
        int[][] npc = c.getNodesPerCluster();
        //        System.out.println(Arrays.deepToString(npc));

        for( int ii=0; ii<npc.length; ii++ )
        {
            if( npc[ii].length > 1 )
            {
                for( int jj=0; jj<npc[ii].length; jj++ )
                {
                    Result result = sess.query("match (s:SentenceNode {louvainIdx:$louvainIdx})-[]->(sc:SentenceClusterNum) set sc.clusterNum=$clusterNum return sc ", Map.of("louvainIdx",npc[ii][jj],"clusterNum",clusterNum));
                }
                ++clusterNum;
            }
        }
    }
    
    
//=================================[Dump stuff]==========================================
    /**
     * From db: Score of sentence pair check
     */
    public void score2Sent()
    {
        System.out.println("...Score to Sents check");
        Result result = sess.query("MATCH (q:QRscoreCW)-[]->(s:SentenceNode) return q,collect(s) as s", Collections.EMPTY_MAP);
        result.forEach(sim -> {
            QRscoreCW qrs = (QRscoreCW)sim.get("q");
            List<SentenceNode> sn = (List<SentenceNode>)sim.get("s");
            System.out.println("....> " +qrs.getName());
            System.out.println("    > Score: " +qrs.getScore());
            sn.forEach(snode -> {
                System.out.println("   > Sent: " +snode.getName());
            });
        });
    }
    
    /**
     * From db: Score to CWcount check
     */
    public void score2Count()
    {
        System.out.println("...Score to CWcounts check");
        Result result = sess.query("MATCH (q:QRscoreCW)-[]->(c:CWcount) return q, collect(c) as c", Collections.EMPTY_MAP);
        result.forEach(sim -> {
            QRscoreCW qrs = (QRscoreCW)sim.get("q");
            List<CWcount> c = (List<CWcount>)sim.get("c");
            System.out.println("....> " +qrs.getName());
            System.out.println("    > Score: " +qrs.getScore());
            c.forEach(cnt -> {
                System.out.println("   > Count: " +cnt.getMatchedWord()+", Qcnt/Rcnt: "+cnt.getCountQ()+"/"+cnt.getCountR());
            });
        });
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
     * Dump lemma sentence pair count.
     */
    public void dumpTLemmaPairCount()
    {
        lemmaPairCount().forEach((k,v) -> {System.out.println(":::> MWrd: " +k+ ", Pair cnt: " +v);});
    }
    
    /**
     * Dump lemma sentence count. Number of sentences in which each lemma appears.
     */
    public void dumpTLemmaSentCount()
    {
        lemmaSentCount().forEach((k,v) -> {System.out.println(":::> MWrd: " +k+ ", Sent cnt: " +v);});
    }
    
        
    /**
     * Dump: Sentence list in which lemma appears
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
    private static void dumpLemmaSentCount()
    {
        lemmaSentCount.forEach((k,v) -> {
            System.out.println("k: " +k+ ", count: " +v);
        });
    }
    
    /**
     * Get info about sentence clusters
     * @param sess 
     */
    public static void dumpClusterInfo(Session sess)
    {
        Result res = sess.query("MATCH (s:SentenceNode)-[r:CLUSTER]->(c:SentenceClusterNum) where c.clusterNum<>-1 RETURN c.clusterNum as cn,count(s) as cs", Collections.EMPTY_MAP);
        System.out.println("Cluster\t\tSentence count");
        System.out.println("-------\t\t--------------");
        res.forEach(entry -> {
            long cNum = (long)entry.get("cn");
            long sCnt = (long)entry.get("cs");
            System.out.println(cNum+"\t\t"+sCnt);
        });
    }
    
    /**
     * Get sentence (SentenceNode) / Cluster data.
     * @return 
     */
    public static Result getClusterSentences(Session sess)
    {
        return sess.query("MATCH (s:SentenceNode)-[r:CLUSTER]->(c:SentenceClusterNum), (s:SentenceNode)-[:DCATEGORY]->(d:DocumentCategory) where c.clusterNum<>-1 RETURN c.clusterNum as cn, collect(d.categoryNum) as dn, collect(s) as cs ORDER BY c.clusterNum", Collections.EMPTY_MAP);
    }
    
    /**
     * Count sentences per cluster from Result.  Result expects a count (long)
     * and a list of sentences (SentenceNode).
     * @param res 
     */
    public static void dumpClusterSentCount(Result res)
    {
        System.out.println("Cluster\t\tSentence(DocCat)s");
        System.out.println("-------\t\t------------------------------------------------------------");
        res.forEach(entry -> {
            long cNum = (long)entry.get("cn");
            System.out.print(cNum+"\t\t");
            
            Long[] dnl = (Long[])entry.get("dn");
            List<SentenceNode> sents = (List<SentenceNode>)entry.get("cs");
            
            for( int ii=0; ii < dnl.length; ii++ )
            {
                System.out.print(sents.get(ii).getName()+"("+dnl[ii]+") ");
            }
            System.out.println("");
        });
    }
    
    /**
     * Show sentences per cluster from Result.  Result expects a count (long)
     * and a list of sentences (SentenceNode).
     * @param res 
     */
    public static void dumpClusterSentences(Result res)
    {
        res.forEach(entry -> {
            long cNum = (long)entry.get("cn");
            List<SentenceNode> sents = (List<SentenceNode>)entry.get("cs");
            sents.forEach(sn -> {
                System.out.println(cNum+"\t\t"+sn.getName()+":"+sn.getSentence());
            });
        });
    }
    
    /**
     * Dump sentences in each cluster.
     * @param sess 
     */
    public static void dumpClusterSents(Session sess)
    {
        Result res = getClusterSentences(sess);
        dumpClusterSentCount(res);
        System.out.println("-------------------------------------------------");
        dumpClusterSentences(res);
    }
    
    /**
     * Dump lemmas per cluster.
     * @param sess 
     */
    public static void dumpClusterLemmas(Session sess)
    {
        Result res = sess.query("match (s:SentenceNode)-[:CLUSTER]-(c), (s:SentenceNode)-[:TOKEN]-(t) where t.lemma <> \"\" return c.clusterNum as cn, collect(t.lemma) as lt", Collections.EMPTY_MAP);
        System.out.println("Cluster\t\tLemmas");
        System.out.println("-------\t\t-----------------------------------------");
        res.forEach(entry -> {
            long cn = (long)entry.get("cn");
            System.out.print(cn+"\t\t");
            String[] lt = (String[])entry.get("lt");
            System.out.println(List.of(lt));
        });
    }
}
