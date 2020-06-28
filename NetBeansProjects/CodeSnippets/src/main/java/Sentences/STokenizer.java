/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package Sentences;

import eu.discoveri.codesnippets.graphs.SentenceNode;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class STokenizer
{
    
    public static void main(String[] args)
    {
        List<String> tokens = new ArrayList<>();
        List<SentenceNode> lsn = Arrays.asList(
            new SentenceNode("S1","American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
            new SentenceNode("S2","American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
            new SentenceNode("S3","With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall"),
            new SentenceNode("S4","The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television"),
            new SentenceNode("S5","If Utahans have their way, Philo T. Farnsworth will become a household name"),
            new SentenceNode("S6","The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue."),
            new SentenceNode("test1","Don't fear the reaper-man doesn't or, shouldn't've been, the city's 1989 recording!")
        );

        try
        {
            for( SentenceNode sn: lsn )
            {
                StringBuilder tok = new StringBuilder();
                StringReader sr = new StringReader(sn.getSentence());

                while(true)
                {
                    int ch = sr.read();
                    if( ch == -1 )
                    {
                        sn.getTokens().add(tok.toString());
                        break;
                    }

                    // If whitespace, new token else append 
                    if( Character.isWhitespace(ch) )
                    {
                        sn.getTokens().add(tok.toString());
                        tok = new StringBuilder();
                    }
                    else
                        tok.append((char)ch);
                }
                
                sr.close();
            }
        } catch( Exception ex ) { ex.printStackTrace(); }
        
        
        lsn.forEach( s -> {
            System.out.println("Sentence: " +s.getName());
            s.getTokens().forEach(t -> System.out.print(" "+t));
            System.out.println("");
        });
    }
}
