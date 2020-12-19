/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import eu.discoveri.predikt.sentences.Token;
import eu.discoveri.predikt.graph.SentenceNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class SimTest
{
        /**
     * Lemma counting.
     * @param wordsQ
     * @param wordsR
     * @param cqr
     * @return 
     */
    public static Map<String,FCWcount> lemmaCompare( List<Token> wordsQ, List<Token> wordsR, FCWcount cqr )
    {
        // Map lemma to count
        Map<String,FCWcount> wCountQR = new HashMap<>();
        
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
                        wCountQR.put(wordQ, new FCWcount(wordQ,1,1));    // Init count of both sentences
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
     * Dump lemma count map
     * @param wCountQR 
     */
    public static void dumpLemmaCountMap(Map<String,FCWcount> wCountQR)
    {
        wCountQR.forEach((k,v) -> {
            System.out.println("Lemma: " +k);
            System.out.println("   Matched word: " +v.getMatchedWord()+ ", countQ/countR: " +v.getCountQ()+"/"+v.getCountR());
        });
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    static int sid = -1;
    public static void main(String[] args)
    {
        List<SentenceNode> sents = new ArrayList<>();
//        List<String> texs = List.of(
//            "American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.",
//            "American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.",
//            "With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall",
//            "The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television",
//            "If Utahans have their way, Philo T. Farnsworth will become a household name",
//            "The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue.",
//            "Confederate statues litter the squares of Southern states.", 
//            "The rest of the day passes with no major aspects, until the moon/Mercury square of the early evening."
//            );
        List<String> texs = List.of( "At this moment, we may be trying to hold our ongoing wounds in check with Libra’s objectivity and patience.",
                                     "Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space." );

        texs.forEach( s ->
        {
            List<Token> lt = new ArrayList<>();
            List<String> ls = Arrays.asList(s.split("\\s*[ ]\\s*"));
            ls.forEach(t -> {
                lt.add(new Token(t,t));
            });
            SentenceNode sen = new SentenceNode("S"+(++sid),s,lt);
            sents.add(sen);
        });
        
        sents.forEach(s -> {
            List<Token> lt = new ArrayList<>();
            try
                { lt = s.rawTokenizeThisSentence(); }
            catch( Exception e )
                { e.printStackTrace(); }
            System.out.println("\r\n"+s.getName()+"> ");
            lt.forEach(t -> {
                System.out.print(" "+t.getToken());
            });
        });

        //--------------- Lemmatize, add count of lemmas, calc. score

        FCWcount cqr = null;
        sents.forEach( s0 -> {
            sents.forEach( s1 -> {
                if( !s0.getName().equals(s1.getName()) )
                {
                    Map<String,FCWcount> mf = lemmaCompare(s0.getTokens(),s1.getTokens(),cqr);
                    if( mf.values().size() > 0 )
                    {
                        dumpLemmaCountMap(mf);
                    }
                }
            });
        });
//        
//        
//        Map<String,FakeCWcount> mf = lemmaCompare(ltQ,ltR,cqr);
//
//        else
//            System.out.println("No matching tokens");
//
//            while( res.next() )
//            {
//                String mw = res.getString("matchWord");
//                score += Math.log(res.getInt("countQ")+1.d)*Math.log(res.getInt("countR")+1.d)*Math.log((sentCount+1.d)/(lemmaSentCount.get(mw)+0.5d));
//            }
//            
//            // Update the score (if significant) and index the relevant Sentences
//            if( score < Constants.EDGEWEIGHTMIN )
//            {
//                score = Constants.EDGEWEIGHTNRZERO;
//            }
//            
//            
    }
}

//------------------------------------------------------------------------------
/*
 * CWcount
 * -------
 */
class FCWcount
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
    public FCWcount( String matchedWord, int countQ, int countR )
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
