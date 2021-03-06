/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.graph;

import eu.discoveri.elements.Sentence;
import eu.discoveri.exceptions.EmptySentenceListException;
import eu.discoveri.exceptions.ListLengthsDifferException;
import eu.discoveri.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.exceptions.SentenceIsEmptyException;
import eu.discoveri.exceptions.TokensCountInSentencesIsZeroException;
import eu.discoveri.exceptions.TokensListIsEmptyException;
import eu.discoveri.testing.Corpus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;


/**
 * Sentence based graph.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GraphSentences
{       
    /**
     * Persist a sentence.
     * 
     * @param sn A sentence(node) - a wrapper for a sentence.
     * @returns the result
     * @return 
     */
    private static void persistSentenceNode( Sentence sn )
            throws TokensCountInSentencesIsZeroException
    {
        try( Session sess = Graphing.getDriver().session(Graphing.getSessionCfg()) )
        {
            Set<Query> qs = sn.buildSentenceQuerySet();
            qs.forEach(q -> sess.run(q,Graphing.getTxCfg()));
        }
    }

    /**
     * Persist an undirected weighted edge between two sentences.  Note if the
     * weight drops below a threshold, a weighted edge will not be added.
     * 
     * @param source
     * @param target
     * @param weight
     * @return 
     */
    private static Result persistUWEdge( Sentence source, Sentence target, double weight )
    {
        try( Session sess = Graphing.getDriver().session(Graphing.getSessionCfg()) )
        {
            return sess.run( source.addWeightedEdge(target,weight) );
        }
    }
    
    /**
     * Start the graph load with sentences (Sentence[node]).
     * 
     * @throws TokensCountInSentencesIsZeroException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SentenceIsEmptyException
     * @throws EmptySentenceListException
     * @throws TokensListIsEmptyException
     * @throws ListLengthsDifferException
     * @throws POSTagsListIsEmptyException
     */
    public static void startGraph()
            throws  TokensCountInSentencesIsZeroException,
                    FileNotFoundException,
                    IOException,
                    SentenceIsEmptyException,
                    EmptySentenceListException,
                    TokensCountInSentencesIsZeroException,
                    TokensListIsEmptyException,
                    ListLengthsDifferException,
                    POSTagsListIsEmptyException
    {
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer) 
        Populate popl = new Populate( LangCode.en );
        
        // Get sentences from raw text
        List<Sentence> ls = Corpus.getSentList(popl);
        
        // Set up sentence analysis
        GraphAlgos gas = new GraphAlgos( ls );
        
        // Tokenize sentences
        gas.tokenizeSentenceCorpus(popl.getTme());
        
        // Now determine POS tag (NN,NNS etc.) of each token
        gas.posTagSentenceCorpus(popl.getPme());
        
        // Lemmatize sentence corpus using default Lemmatizer
        // *** Nota Bene: Must be done AFTER tokenization and POS tagging.
        gas.lemmatizeSentenceCorpus(popl, true);
        
        // Remove tokens that don't match reqd. POS tags.
        // *** Nota Bene: Must be done BEFORE counting tokens or lemmas. 
        gas.filterPOStags();
        
        // Now calculate common word count between sentences and
        // do the token/lemma counting per sentence pair (QRscore) [Updates each sentence]
        gas.countingLemmas();
        
        // Calculate the similarity score between sentences (QRscore) [Updates each sentence]
        gas.similarity();
        
        /*
         * Now populate db
         */
        // Nodes
        for( Sentence s: ls )
            persistSentenceNode(s);

        // Edges
        gas.getQRscores().forEach((k,v) -> {
            persistUWEdge(k.getKey(),k.getValue(), v);
        });
    }
}
