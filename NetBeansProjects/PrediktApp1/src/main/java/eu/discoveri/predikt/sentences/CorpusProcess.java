/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.sentences;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.discoveri.predikt.cluster.QRscore;

import eu.discoveri.predikt.config.Setup;
import eu.discoveri.predikt.exceptions.EmptySentenceListException;
import eu.discoveri.predikt.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt.exceptions.TokensCountInSentencesIsZeroException;
import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;
import eu.discoveri.predikt.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt.exceptions.NoLanguageSetEntriesException;
import eu.discoveri.predikt.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.lemmatizer.Lemmatizer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CorpusProcess
{
    // Hazelcast mapping
    private static Config cfg = new Config();
    private static HazelcastInstance hi = Hazelcast.newHazelcastInstance(cfg);

    // Map of common words per sentence pair:<String,CountQR> (Word/token + counts)
    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>  commonWords = new HashMap<>();
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Map<String,CountQR>>  commonWords = hi.getMap("commonwords");
    // Sentence similarity score
    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,QRscore>              qrScore = new HashMap<>();
//    private static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,Double>               qrScore = hi.getMap("qrScore");
    // Similarity score
    private static double                       score = 0.d;
    // Converged? flag
    private static double                       totConverge = Double.MAX_VALUE;
    
    // Number sentences in which each word appears.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<String,Map<String,Nul>>  tokSentCount = new HashMap<>();
    
    // Prevent sentence process duplication, ie: A:B obviates B:A.  The mapping allows multikey, eg: A:B, A:C etc.
    private static Map<String,Map<String,Nul>>  noDups = new HashMap<>();
    private static Nul                          nul = new Nul();
    
    // Sentences to analyze
    private final List<SentenceNode>            sents;
    // Language/Locale
    private final Setup                         setup;
    
    private final Language                      l;

    
    /**
     * Constructor.
     * @param sents 
     * @param setup 
     */
    public CorpusProcess( List<SentenceNode> sents, Setup setup )
    {
        this.sents = sents;
        this.setup = setup;

        // Setup language/locale            
        l = setup.getLanguage();
    }
    
    /**
     * Get POS for each token.
     * 
     * @param pme POSTagger
     * @return
     * @throws EmptySentenceListException
     * @throws TokensListIsEmptyException 
     */
    public List<SentenceNode> posTagSentenceCorpus(POSTaggerME pme)
            throws EmptySentenceListException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sents.isEmpty() )
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
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException 
     * @throws NoLanguageSetEntriesException 
     * @throws TokensListIsEmptyException 
     */
    public List<SentenceNode> tokenizeSentenceCorpus(TokenizerME tme,POSTaggerME pme)
            throws EmptySentenceListException, SentenceIsEmptyException, NoLanguageSetEntriesException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sents.isEmpty() )
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
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     */
    public List<SentenceNode> simpleTokenizeSentenceCorpus(SimpleTokenizer st, POSTaggerME pme)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException
    {
        // Check we have sentences
        if( sents.isEmpty() )
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
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public List<SentenceNode> rawTokenizeSentenceCorpus(POSTaggerME pme)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, IOException
    {
        // Check we have sentences
        if( sents.isEmpty() )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:rawTokenizeSentenceCorpus()");
        
        // Tokenize and clean
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB characters
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
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws ListLengthsDifferException
     * @throws POSTagsListIsEmptyException 
     */
    public List<SentenceNode> lemmatizeSentenceCorpus(Lemmatizer lemmer, boolean match2NN)
             throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, ListLengthsDifferException, POSTagsListIsEmptyException
    {
        // Check we have sentences
        if( sents.isEmpty() )
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
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return
     * @throws EmptySentenceListException
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException
     * @throws ListLengthsDifferException
     * @throws POSTagsListIsEmptyException 
     */
    public List<SentenceNode> lemmatizeSentenceCorpus(Populate popl,boolean match2NN)
            throws EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException, ListLengthsDifferException, POSTagsListIsEmptyException
    {
        // Check we have sentences
        if( sents.isEmpty() )
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
     * @return 
     * @throws java.sql.SQLException 
     */
    public List<SentenceNode> lemmatizeSentenceCorpusViaDb(Connection  conn)
            throws SQLException
    {
        // Lemmas SQL
        PreparedStatement ps = conn.prepareStatement("select * from Word,Lemma where Word.word=? and Word.POSId=? and Word.lemmaId=Lemma.id");
        
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB characters
            s.updateSentence( l.remOOBChars(s.getSentence()) );

            // Tokenize
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
     * Clean up apostrophes, stopwords, numbers.
     * 
     * @return
     * @throws EmptySentenceListException 
     * @throws TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public List<SentenceNode> cleanTokensSentenceCorpus()
            throws EmptySentenceListException, TokensListIsEmptyException, IOException
    {
        // Check we have sentences
        if( sents.isEmpty() )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:cleanTokensSentenceCorpus");
        
        // NB: Order is important!
        for( SentenceNode s: sents )
        {
            // 1. Remove OOB chars (before tokenization takes place)
            // 2. Remove apostrophes (NB: based on language).  Note: generates more tokens
            List<Token> lts = l.unApostrophe(setup.loadApostrophesProperties(), s.getTokens());
            s.setTokens(lts);

            // 3. All tokens now to lowercase and remove dashes, punctuation etc.
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
     * Token matching/counting per sentence pair.  Note Token class contains
     * both token and lemma.
     * 
     * @return 
     * @throws EmptySentenceListException 
     */
    public List<SentenceNode> countingTokens()
            throws EmptySentenceListException
    {
        // Check we have sentences
        if( sents.isEmpty() )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:countingTokens()");
        
        /*
         * Sort sentences by tokens' count (long to short hence s2 - s1)
         * Allows sentence matching to work efficiently/properly
         */
        List<SentenceNode> nodeList = sents.stream()
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
            CountQR cqr; 
            
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
                Map<String,CountQR> wCountQR = new HashMap<>();
                
                // For each token in sentenceQ (Note: Q token count >= R token count)
                for( Token tQ: wordsQ )
                {
                    String wordQ = tQ.getToken();       // *MATCH* this token

                    if( wCountQR.containsKey(wordQ) )   // Is this token common between Q&R?
                    { // Yes
                        cqr = wCountQR.get(wordQ);      // Get current count for this token
                        int count = cqr.getQ();         // Count in Q
                        cqr.setQ(++count);              // Increment
                        wCountQR.replace(wordQ, cqr);   // Update
                        
                        // Already counted all of R (where token key is created below), so skip R
                        continue;
                    }

                    // Match Q token against each token in target sentenceR
                    for( Token tR: wordsR )
                    {
                        String wordR = tR.getToken();   // *MATCH* this token
                        
                        if( wordR.equals(wordQ) )                                   // Words match between sentences?
                        { //Yes
                            // First match for token in both Q&R?
                            if( !wCountQR.containsKey(wordQ) )                      // Create Map entry
                            {//Yes
                                wCountQR.put(wordQ, new CountQR(nameQ,1,nameR,1));  // Init count of both sentences
                            }
                            else
                            {//No
                                // Ok, get the counts for this token
                                cqr = wCountQR.get(wordR);
                                int count = cqr.getR();         // Count for R sentence
                                cqr.setR(++count);              // Increment R count
                                wCountQR.replace(wordR, cqr);   // Update
                            }
                        }
                    }
                }
                
            // Ok, store the common words for the sentence pairs
            commonWords.put(new AbstractMap.SimpleEntry(nodeQ,nodeR), wCountQR);
            }
        }

        /*
         * Number sentences in which token appears (from commonWords map)
         */
        commonWords.forEach((k,v) -> {
            String pSrc = k.getKey().getName();             // tuple 1: src
            String pTgt = k.getValue().getName();           // tuple 2: tgt
            
            // For common words build key:subkey (Sentence:Word)
            v.forEach((s,c) -> {                            // Map words-count
                // Word/token appeared before?
                if( !tokSentCount.containsKey(s) )
                {// No
                    Map<String,Nul> nulMap = new HashMap<>();
                    nulMap.put(pSrc,nul);
                    nulMap.put(pTgt, nul);
                    tokSentCount.put(s, nulMap);
                }
                else
                {// Yes
                    if( !tokSentCount.get(s).containsKey(pSrc) )
                    {
                        tokSentCount.get(s).put(pSrc, nul);
                    }
                    if( !tokSentCount.get(s).containsKey(pTgt) )
                    {
                        tokSentCount.get(s).put(pTgt, nul);
                    }
                }
            });
        });
        
        return sents;
    }
    
    /**
     * Lemma matching/counting per sentence pair.  Note Token class contains both
     * token and lemma.
     * 
     * @return 
     * @throws EmptySentenceListException 
     */
    public List<SentenceNode> countingLemmas()
            throws EmptySentenceListException
    {
        // Check we have sentences
        if( sents.isEmpty() )
            throw new EmptySentenceListException("Need sentences to process! CorpusProcess:countingLemmas()");
        
        /*
         * Sort sentences by tokens' count (long to short hence s2 - s1)
         * Allows sentence matching to work efficiently/properly
         */
        List<SentenceNode> nodeList = sents.stream()
                                .sorted((s1,s2) -> s2.getTokens().size()-s1.getTokens().size())
                                .collect(Collectors.toList());

        /*
         * Count common words per sentence pair (Q,R).
         * [Num sentence pairs = sents.size*(sents.size-1)/2.]
         */
        // ---> For each sentence (Q) 'Source' sentence
        for( SentenceNode nodeQ: nodeList )
        {
            // Count of common words in each sentence of pair
            CountQR cqr; 
            
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
                Map<String,CountQR> wCountQR = new HashMap<>();
                
                // For each lemma in sentenceQ (Note: Q lemma count >= R lemma count)
                for( Token tQ: wordsQ )
                {
                    String wordQ = tQ.getLemma();       // *MATCH* this lemma

                    if( wCountQR.containsKey(wordQ) )   // Is this lemma common between Q&R?
                    { // Yes
                        cqr = wCountQR.get(wordQ);      // Get current count for this token
                        int count = cqr.getQ();         // Count in Q
                        cqr.setQ(++count);              // Increment
                        wCountQR.replace(wordQ, cqr);   // Update
                        
                        // Already counted all of R (where token key is created below), so skip R
                        continue;
                    }

                    // Match Q token against each lemma in target sentenceR
                    for( Token tR: wordsR )
                    {
                        String wordR = tR.getLemma();   // *MATCH* this lemma
                        
                        if( wordR.equals(wordQ) )                                   // Words match between sentences?
                        { //Yes
                            // First match for token in both Q&R?
                            if( !wCountQR.containsKey(wordQ) )                      // Create Map entry
                            {//Yes
                                wCountQR.put(wordQ, new CountQR(nameQ,1,nameR,1));  // Init count of both sentences
                            }
                            else
                            {//No
                                // Ok, get the counts for this token
                                cqr = wCountQR.get(wordR);
                                int count = cqr.getR();         // Count for R sentence
                                cqr.setR(++count);              // Increment R count
                                wCountQR.replace(wordR, cqr);   // Update
                            }
                        }
                    }
                }
                
            // Ok, store the common words for the sentence pairs
            commonWords.put(new AbstractMap.SimpleEntry(nodeQ,nodeR), wCountQR);
            }
        }

        /*
         * Number sentences in which lemma appears (from commonWords map)
         */
        commonWords.forEach((k,v) -> {
            String pSrc = k.getKey().getName();             // tuple 1: src
            String pTgt = k.getValue().getName();           // tuple 2: tgt
            
            // For common words build key:subkey (Sentence:Word)
            v.forEach((s,c) -> {                            // Map words-count
                // Word/lemma appeared before?
                if( !tokSentCount.containsKey(s) )
                {// No
                    Map<String,Nul> nulMap = new HashMap<>();
                    nulMap.put(pSrc,nul);
                    nulMap.put(pTgt, nul);
                    tokSentCount.put(s, nulMap);
                }
                else
                {// Yes
                    if( !tokSentCount.get(s).containsKey(pSrc) )
                    {
                        tokSentCount.get(s).put(pSrc, nul);
                    }
                    if( !tokSentCount.get(s).containsKey(pTgt) )
                    {
                        tokSentCount.get(s).put(pTgt, nul);
                    }
                }
            });
        });
        
        return sents;
    }
    
    /**
     * Similarity scores.Determines topically similar sentences (pairs) and is
     * used as the weight of connecting edge between pairs in graph.IDF-weighted word overlap.
     * For algorithm, see Allan et al.
     * "Retrieval and Novelty Detection at the Sentence Level" and
     * Metzler et al, "Similarity Measures for Tracking Information Flow"
     * 
     * @throws eu.discoveri.predikt.exceptions.TokensCountInSentencesIsZeroException
     */
    public void similarity()
            throws TokensCountInSentencesIsZeroException
    {
        // This method requires a token count
        if( tokSentCount.isEmpty() )
            throw new TokensCountInSentencesIsZeroException("Need tokens to process! CorpusProcess:similarity()");
                    
        // Get scores into qrScore.
        commonWords.forEach((k,v) -> {
            // Init score
            score = 0.d;
            
            // (Word/token + counts)
            v.forEach((w,c) -> {
                // Number of times token/word appears across all sentences
                double sft = tokSentCount.get(w).values().stream().count();
                // Similarity score for this pair (k: <SentenceNode,SentenceNode>
                score += Math.log(c.getQ()+1.d)*Math.log(c.getR()+1.d)*Math.log((getSentences().size()+1.d)/(sft+0.5d));
            });
            
            // Store similarity score for each sentence pair
            qrScore.put(k, new QRscore(k.getKey(),k.getValue(),score));
        });
    }
    
    /**
     * Shutdown the HZC mapping
     */
    public void closeCorpusProcess()
    {
        Hazelcast.shutdownAll();
    }
    
    /**
     * TextRank.
     * See papers by Mihelacea et al on TextRank for algorithm.  Plus book:
     * "Graph based Natural Language Processing and Information Retrieval".
     * 
     */
    // Cypher idea, incoming, outgoing
//    String inout = "MATCH (s:Sentence {name:'S1'}) WITH size((s)-[:HAS]->()) as out, size((s)<-[:HAS]-()) as in, s  RETURN s, i, out (out-in) as diff ORDER BY diff DESC";
//
//    public static void textRank()
//    {
//        // Init nodes
//        g.vertexSet().forEach((n) -> {
//            System.out.format(n.getName()+": %6f ",n.getScore());
//        });
//        System.out.println("");
//        
//        // Score
//        for( int ii= 0; ii<Constants.NUMITERS; ii++ )
//        {
//            for( TestNode n: g.vertexSet() )                                    // For all vertices V(i)  [TestNode === SentenceNodeREDUNDANT/Sentence]
//            {
//                double rankVjTot = 0.d;
//
//                // Incoming edges of Vi
//                Set<DefaultWeightedEdge> edges = g.incomingEdgesOf(n);
//
//                // Sum over Vjs of Inc edges of Vi
//                for( DefaultWeightedEdge dwe: edges )
//                {
//                    //System.out.println("  > Remote vertex: " +g.getEdgeSource(dwe)+ ", Weight: " +g.getEdgeWeight(dwe));
//                    TestNode nJ = g.getEdgeSource(dwe);                         // vJ
//
//                    double edgeWeightJI = g.getEdgeWeight(dwe);                 // Wji
//                    Set<DefaultWeightedEdge> eJ = g.outgoingEdgesOf(nJ);
//
//                    // Sum of weights of Outgoing edges of Vj
//                    double sumOfVjOutEdgeWeightsJK = eJ.stream().mapToDouble((dweJ) -> g.getEdgeWeight(dweJ)).sum();
//
//                    // WS(Vj): nJ.getScore(); Wji: edgeWeightJI; Sum Out Wjk: sumOfVjOutEdgeWeightsJK
////                    System.out.println(""+n.getName()+": Wji="+edgeWeightJI+", Vj="+nJ.getName()+", WS(Vj)="+nJ.getScore()+", sumWjk="+sumOfVjOutEdgeWeightsJK);
//                    rankVjTot += edgeWeightJI * nJ.getScore() / sumOfVjOutEdgeWeightsJK;
//                }
//                
//                // Update previous score with current score
//                n.setPrevScore(n.getScore());
//                // WS(Vi) update
//                n.setScore(rankVjTot*Constants.TEXTRANK_DAMPING_FACTOR +(1 - Constants.TEXTRANK_DAMPING_FACTOR)/ltns.size());
//            }
//            
//            // Show converging
//            totConverge = 0.d;
//            g.vertexSet().forEach((n) -> {
//                System.out.format(n.getName()+": %6f ",n.getScore());
//                totConverge += n.getPrevScore()-n.getScore();
//            });
//            System.out.println("");
//            
//            // Converged
//            if( totConverge*g.vertexSet().size() <= Constants.SCORECONVERGE )
//                break;
//        }
//    }

    
    /**
     * Get list of sentences (nodes).
     * 
     * @return 
     */
    public List<SentenceNode> getSentences()
    {
        return sents;
    }
    
    /**
     * Return QR sentence pair scores.
     * 
     * @return 
     */
    public Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,QRscore> getQRscores()
    {
        return qrScore;
    }
    
    /**
     * Dump common words map
     */
    public static void dumpCommonWords()
    {
        System.out.println("Tot. common words: " +commonWords.size());
        // <<Sentence Pair>,<Word,Count>>
        commonWords.forEach((k,v) -> {
            // Sentence pair
            String pSrc = k.getKey().getName();                 // Source sentence
            String pTgt = k.getValue().getName();               // Target sentence

            System.out.println("Pair: "+pSrc+"/"+pTgt);
            // Common word/token count
            v.forEach((s,c) -> {
                System.out.println("   word: " +s+"> "+c.getqName()+"(Q count): " +c.getQ()+" / "+c.getrName()+"(R count): " +c.getR());
                });
        });
    }
    
    /**
     * Dump the sentences
     */
    public void dumpSentences()
    {
        sents.forEach(s -> System.out.println("Key: " +s.getNamespace()+":"+s.getName()+ ", Sentence: " +s.getSentence() ));
    }
    
    /**
     * Dump sentence names and count by token/word
     */
    public void dumpTokSentCount()
    {
        // Number sentences in which token appears
        tokSentCount.forEach((k,v) -> {
            System.out.println("...> Word: " +k+ ", Count sents: " +v.values().stream().count());
            System.out.print("     Sentences: ");
            v.keySet().stream().sorted().forEach(k1 -> System.out.print(k1+" "));
            System.out.println("");
        });
    }
    
    /**
     * Show similarity score between sentences (QR).
     * @param full true for full dump
     */
    public void dumpQRscore( boolean full )
    {
        if( !full )
        {
            qrScore.forEach((k,v) -> {
                System.out.println("Pair: " +k.getKey().getName()+"/"+k.getValue().getName() +", Score: " +v);
            });
        }
        else
            qrScore.forEach((k,v) -> {
                System.out.format("Pair> Q:(" +k.getKey().getName()+")/R:("+k.getValue().getName() +"), SimScore: %9.5f%n",v);
                System.out.println("   Sentence Q: " +k.getKey().getSentence());
                System.out.println("   Sentence R: " +k.getValue().getSentence());
                System.out.println("");
            });
    }
}
