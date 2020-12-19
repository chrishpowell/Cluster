/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.cluster;

import eu.discoveri.predikt3.utils.Utils;
import java.util.List;
import java.util.Map;


/**
 * Determines document similarity and subsequent clustering.
 * @author chrispowell
 */
public class DocSimilarity
{
    /**
     * Print cosine similarity between document pairs. Being nDocs*(nDocs-1)/2
     * doc1:doc2, doc1:doc3, ... doc1:docN, doc2:doc3, doc2:doc4, ... docN-1:docN.
     * 
     * @param maxDocId For db access, max docId (note there may be gaps in Ids)
     * @param tfidf Map docId to list of LemmaScores
     */
    public static void dumpCosineSimilarity( int maxDocId, Map<Integer,List<LemmaScore>> tfidf )
    {
    System.out.println("\r\n(Cosine) Similarity, -1.0=None, 1.0=Same [NaN owing to removed doc]");
    for( int ii=1; ii<=maxDocId; ii++ )
        for( int jj=ii+1; jj<=maxDocId; jj++ )
        {
            // Beware of gaps in dId (removed docs)
            if( tfidf.containsKey(ii) && tfidf.containsKey(jj) )
            {
                System.out.println("Cos.sim.: [" +ii+ "]:[" +jj+ "] "
                                    +Utils.cosineSimilarity(LemmaScore.toScoreArray(tfidf.get(ii)), LemmaScore.toScoreArray(tfidf.get(jj))));
            }
        }
    }
}
