/*
 */
package eu.discoveri.predikt3.lemmatizer;

import eu.discoveri.predikt3.exceptions.ArrayLengthsDifferException;
import eu.discoveri.predikt3.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt3.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt3.exceptions.TokensListIsEmptyException;

import eu.discoveri.predikt3.sentences.Token;

import java.util.List;
import java.util.Map;


/**
 * Copy of OpenNLP Lemmatizer interface but with Exceptions thrown.
 * 
 * @author Chris Powell, Discoveri OU
 */
public interface Lemmatizer
{
    /**
     * Generates lemmas for the word and POStag returning the result in a
     * map containing every possible lemma for each token and postag:
     * String-token:String-tag.
     * (NB: OpenNLP would make better sense if tokens and tags were held in a
     * class associating tokens with tags one-2-one)
     * 
     * @param toks Tokens of the sentence
     * @param tags POS tags of the tokens of the sentence
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return The token as key and the associated lemma
     * @throws TokensListIsEmptyException
     * @throws POSTagsListIsEmptyException
     * @throws ListLengthsDifferException 
     */
    public Map<String,String> lemmatize2String(List<String> toks, List<String> tags, boolean match2NN)
            throws TokensListIsEmptyException, POSTagsListIsEmptyException, ListLengthsDifferException;
    
    /**
     * Generates lemmas for the word and POStag returning the result in a
     * map containing every possible lemma for each token and postag:
     * Token-token:String-tag.
     * (NB: OpenNLP would make better sense if tokens and tags were held in a
     * class associating tokens with tags one-2-one)
     * 
     * @param toks
     * @param tags
     * @param match2NN
     * @return
     * @throws TokensListIsEmptyException
     * @throws POSTagsListIsEmptyException
     * @throws ListLengthsDifferException 
     */
    public Map<Token,String> lemmatize2Token(List<Token> toks, List<String> tags, boolean match2NN)
            throws TokensListIsEmptyException, POSTagsListIsEmptyException, ListLengthsDifferException;
    
    /**
     * Generates lemmas for the word and POStag returning the result in a
     * array containing every possible lemma for each token and postag.
     * (NB: OpenNLP would make better sense if tokens and tags were held in a
     * class associating tokens with tags one-2-one)
     * 
     * @param toks Tokens of the sentence
     * @param tags POS tags of the tokens of the sentence
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return The token as key and the associated lemma
     * @throws TokensListIsEmptyException
     * @throws POSTagsListIsEmptyException
     * @throws ArrayLengthsDifferException 
     * @throws ListLengthsDifferException (Should never happen)
     */
    public Map<String,String> lemmatize2String(String[] toks, String[] tags, boolean match2NN)
            throws TokensListIsEmptyException, POSTagsListIsEmptyException, ArrayLengthsDifferException, ListLengthsDifferException;
}
