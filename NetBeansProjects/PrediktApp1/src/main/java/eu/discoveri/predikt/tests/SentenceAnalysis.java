/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.exceptions.EmptySentenceListException;
import eu.discoveri.predikt.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.sentences.CorpusProcessDb;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SentenceAnalysis
{
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
           new SentenceNode("C2","Whew,       it's hot!")
       );
 
    public static void main(String[] args)
            throws IOException, EmptySentenceListException, SentenceIsEmptyException, TokensListIsEmptyException
    {
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );
        EnSetup enSetup = new EnSetup();
        CorpusProcessDb cp = new CorpusProcessDb( enSetup, null, null );
        //cp.setSentenceCount(sents.size());  // Re-insert in cp for test purposes
        cp.rawTokenizeSentenceCorpus(popl.getPme(),sents);
        SentenceNode c2 = sents.get(11);
        c2.getTokens().forEach(t -> System.out.print(" [" +t+ "]"));
        System.out.println("");
    }
}
