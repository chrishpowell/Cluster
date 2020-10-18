/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.categories;

import java.util.Arrays;
import java.util.List;


/**
 * TfIdf is a numerical statistic that is intended to reflect how important a word
 * is to a document in a collection or corpus.
 * 
 * Formally, the TF-IDF score for a word t in the document d from the document
 * set D is calculated as follows:
 *      tfidf(t,d,D) = tf(t,d) * idf(t,D)
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class TfIdf
{
    /**
     * Term frequency.
     * @param doc  list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    public double tf( List<String> doc, String term )
    {
        double result = 0;
        result = doc.stream().filter(word -> (term.equalsIgnoreCase(word))).map(_item -> 1.0).reduce(result, (accumulator, _item) -> accumulator + 1);
        return result / doc.size();
    }

    /**
     * Inverse document frequency.
     * @param docs list of list of strings represents the dataset
     * @param term String represents a term
     * @return the inverse term frequency of term in documents
     */
    public double idf( List<List<String>> docs, String term )
    {
        double n = 0;
        for (List<String> doc : docs)
        {
            for (String word : doc)
            {
                if (term.equalsIgnoreCase(word))
                {
                    n++;
                    break;
                }
            }
        }
        
        return Math.log(docs.size() / n);
    }

    /**
     * @param doc  a text document
     * @param docs all documents
     * @param term term
     * @return the TF-IDF of term
     */
    public double tfIdf(List<String> doc, List<List<String>> docs, String term)
    {
        return tf(doc, term) * idf(docs, term);
    }

    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        List<String> doc1 = Arrays.asList("Lorem", "ipsum", "dolor", "ipsum", "sit", "ipsum");
        List<String> doc2 = Arrays.asList("Vituperata", "incorrupte", "at", "ipsum", "pro", "quo");
        List<String> doc3 = Arrays.asList("Has", "persius", "disputationi", "id", "simul");
        List<List<String>> documents = Arrays.asList(doc1, doc2, doc3);

        TfIdf calculator = new TfIdf();
        double tfidf = calculator.tfIdf(doc1, documents, "ipsum");
        System.out.println("TF-IDF (ipsum) = " + tfidf);
    }
}
