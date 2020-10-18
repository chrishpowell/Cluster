/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphUtils;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceEdge;
import eu.discoveri.predikt.graph.service.SentenceEdgeService;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import eu.discoveri.predikt.sentences.LangCode;
import eu.discoveri.predikt.sentences.Token;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LCTest
{
    private static final List<Token> lt = List.of(new Token("The"), new Token("quick"), new Token("brown"), new Token("fox"), new Token("jumps"), new Token("over"), new Token("the"), new Token("lazy"), new Token("dog"));
    private static final List<SentenceNode> sents = Arrays.asList(
       new SentenceNode("S1","American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.   "),
       new SentenceNode("S2","American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
       new SentenceNode("S3","With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall"),
       new SentenceNode("S4","The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television"),
       new SentenceNode("S5","If Utahans have their way, Philo T. Farnsworth will become a household name"),
       new SentenceNode("S6","The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue."),
       new SentenceNode("T0","Confederate statues litter the squares of Southern states."),
       new SentenceNode("T1","The quick brown fox jumps over the lazy dog"),
       new SentenceNode("T2","The quick brown hound jumps over the lazy fox."),
       new SentenceNode("C0","Tropical climate is one of the five major climate groups in the Köppen climate classification of heat."),
       new SentenceNode("C1","Tropical climates are broadly located within 20 to 25 degrees of the equator and characterized by monthly average temperatures of 18 ℃ (64.4 ℉), or higher year-round, often following a seasonal rhythm and where annual precipitation is generally abundant and sunlight is intense."),
       new SentenceNode("C2","Whew,       it's hot!"),
       new SentenceNode("TT0","The quick brown fox jumps over the lazy dog", LangCode.en, Locale.ENGLISH, lt, 0.0d)
    );
                
    public static void main(String[] args)
            throws IOException, SentenceIsEmptyException
    {
        // Language/locale setup
        System.out.println("... Setup [English]");
        EnSetup enSetup = new EnSetup();
        
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer)
        System.out.println("... Setup NLP");
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );
        
        // Session
        System.out.println("... Setup graph database");
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();

        // Clear database nodes and edges
        System.out.println("... Purge db");
        sess.purgeDatabase();
        
        // Drop and create indexes
        System.out.println("    > Drop and create indexes");
        GraphUtils.dropIndexes(sess);
        // SentenceNode indexes
        // QRscore index
        GraphUtils.indexSN( sess );

        // Service for nodes
        SentenceNodeService sns = new SentenceNodeService();
        
        // Store a node without tokens
        SentenceNode sn0 = sents.get(0);
        sn0.persist(sns);
        
        // Store a node with tokens
//        SentenceNode sn1 = sents.get(12);
//        sn1.persist(sns);
        
        // Update sn0
        Iterable<SentenceNode> isn = sess.query(SentenceNode.class,"match (s:SentenceNode) where s.name=$name return s", Map.of("name","S1"));
        for( SentenceNode sn: sents )
        {
//            sn.setTokens(lt);
            sn.rawTokenizeThisSentence();
//            List<Token> lst = sn.getTokens();
//            lst.add(new Token("fred"));
            sn.persist(sns);
        }
        
        System.out.println("\r\n... Closing");
        discSess.close();
    }
}
