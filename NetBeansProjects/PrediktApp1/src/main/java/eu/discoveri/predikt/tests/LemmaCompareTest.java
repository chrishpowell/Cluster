/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author chrispowell
 */
public class LemmaCompareTest
{
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
    

    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        List<String> sents = List.of("Complete all entries in this form marked with an asterisk","argle fox bargle said the qumquat fox");
        List<FakeToken> ltQ = new ArrayList<>();
        List<FakeToken> ltR = new ArrayList<>();
        
        List<String> ls = Arrays.asList(sents.get(0).split("\\s*[ ]\\s*"));
        ls.forEach(t -> {
            ltQ.add(new FakeToken(t,t));
        });
        ls = Arrays.asList(sents.get(1).split("\\s*[ ]\\s*"));
        ls.forEach(t -> {
            ltR.add(new FakeToken(t,t));
        });
        
        FakeCWcount cqr = null;
        Map<String,FakeCWcount> mf = lemmaCompare(ltQ,ltR,cqr);
        if( mf.values().size() > 0 )
        {
            dumpLemmaCountMap(mf);
        }
        else
            System.out.println("No matching tokens");
    }
}

//------------------------------------------------------------------------------
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