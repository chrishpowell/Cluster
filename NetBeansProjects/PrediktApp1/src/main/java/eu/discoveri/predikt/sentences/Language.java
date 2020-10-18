/*
 */
package eu.discoveri.predikt.sentences;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;
import eu.discoveri.predikt.resources.LangResources;
import java.io.IOException;
import java.util.Iterator;

import java.util.List;
import java.util.Properties;
import eu.discoveri.predikt.config.LangSetup;


/**
 * Languages handled.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public abstract class Language
{
    // Language to model file
    private final static EnumMap<LangCode,Map<String,String>> langMap = new EnumMap<>(LangCode.class);
    // Populate the map
    static
    {
        // English
        Map<String,String> enModel = new HashMap<>();
        enModel.put(Constants.SENTMODEL, Constants.ENSENTFILE);
        enModel.put(Constants.TOKENMODEL, Constants.ENTOKENFILE);
        enModel.put(Constants.SEQMODEL, Constants.ENPOSMEFILE);
        enModel.put(Constants.LEMMATIZE, Constants.ENLEMMAFILE);
        langMap.put(LangCode.en,enModel);
        
        // Spanish
        Map<String,String> esModel = new HashMap<>();
        esModel.put(Constants.SENTMODEL, Constants.ESSENTFILE);
        esModel.put(Constants.TOKENMODEL, Constants.ESTOKENFILE);
        esModel.put(Constants.SEQMODEL, Constants.ESPOSMEFILE);
        esModel.put(Constants.LEMMATIZE, Constants.ESLEMMAFILE);
        langMap.put(LangCode.es,esModel);
        
        // Portuguese
        
        // French
        
        // German
        
        // Russian
        
        // Hindi
        
        // Mandarin
    }
    
    /**
     * Get language models.
     * Map<LngCode,Map<String,String>>, eg: <LangCode.en,<"Sentence","en-sent.bin">>
     * @return 
     */
    public static EnumMap<LangCode,Map<String,String>> getLangModels() { return langMap; }
    
    /**
     * Ditch apostrophes.
     * 
     * @param props
     * @param tokens
     * @return 
     * @throws eu.discoveri.predikt.exceptions.TokensListIsEmptyException 
     */
    public abstract List<String> unApostrophe( Properties props, String[] tokens )
            throws TokensListIsEmptyException;
    
    /**
     * Ditch apostrophes.
     * 
     * @param props
     * @param tokens
     * @return 
     * @throws eu.discoveri.predikt.exceptions.TokensListIsEmptyException 
     */
    public abstract List<Token> unApostrophe( Properties props, List<Token> tokens )
            throws TokensListIsEmptyException;
    
    /**
     * Ditch Out Of Band characters (such as " for degree secs in English).
     * 
     * @param doc
     * @return 
     */
    public abstract String remOOBChars( String doc );
    
    /**
     * Remove stop words from token list
     * @param setup
     * @param tokens
     * @return 
     * @throws java.io.IOException 
     */
    public List<Token> remStopWords( LangSetup setup, List<Token> tokens )
            throws IOException
    {
        List<String> stops = setup.loadStopWords();
        for(Iterator<Token> iter = tokens.iterator(); iter.hasNext();)
        {
            Token t = iter.next();
            if(stops.contains(t.getToken())) { iter.remove(); }
        }
        
        return tokens;
    }
}
