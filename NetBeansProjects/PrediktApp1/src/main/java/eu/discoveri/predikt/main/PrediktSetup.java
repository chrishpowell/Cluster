/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.main;

import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.graph.Corpi;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceEdge;
import eu.discoveri.predikt.graph.SentenceEdgeService;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.SentenceNodeService;
import eu.discoveri.predikt.sentences.CorpusProcess;
//import eu.discoveri.lemmas.db.LemmaDbBuild;

import java.sql.Connection;
import java.util.List;

import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class PrediktSetup
{
    public static void main(String[] args)
            throws Exception
    {
        // Language/locale setup
        EnSetup enSetup = new EnSetup();
        
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer) 
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );

        // Connect to lemma db
        //Connection conn = LemmaDbBuild.lemmaDb();
        
        /*
         * Process sentences
         * -----------------
         */
        // Get sentences from raw text
        List<SentenceNode> lsents = Corpi.getVertices();
        
        // Set up sentence analysis on above sentences/language/locale
        CorpusProcess cp = new CorpusProcess( lsents, enSetup );
        
        // Tokenize sentences
        cp.rawTokenizeSentenceCorpus(popl.getPme());

        // Clean up tokens (punctuation,numbers,unwanted POStags etc.)
        cp.cleanTokensSentenceCorpus();
        
        // Get lemmas
        //cp.lemmatizeSentenceCorpusViaDb(conn);
        
        // Now calculate common word count between sentences and
        // do the token/lemma counting per sentence pair (QRscore) [Updates each sentence]
        cp.countingLemmas();
        
        // Calculate the similarity score between sentences (QRscore) [Updates each sentence]
        cp.similarity();
        
        /*
         * Populate db (from corpus Maps)
         * ------------------------------
         */
        // Session
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getSession();
        // Db service
        SentenceNodeService sns = new SentenceNodeService();
        SentenceEdgeService ses = new SentenceEdgeService();

        // Clear database
        sess.purgeDatabase();
        
        // Nodes
        lsents.forEach(s -> s.persist(sns));

        // Edges
        cp.getQRscores().forEach((k,v) -> {
            SentenceEdge se = new SentenceEdge(k.getKey(),k.getValue(),v);
            se.persist(ses);
        });
        
        // Close
        System.out.println("");
        discSess.close();
    }
}
