/*
 */
package eu.discoveri.lemmatizer;

import eu.discoveri.config.Constants;
import eu.discoveri.elements.PennPOSCode;
import eu.discoveri.graph.LangCode;
import eu.discoveri.graph.Language;
import eu.discoveri.utils.Utils;

import eu.discoveri.exceptions.ListLengthsDifferException;
import eu.discoveri.exceptions.ArrayLengthsDifferException;
import eu.discoveri.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.exceptions.TokensListIsEmptyException;
import eu.discoveri.graph.Populate;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Need to be able to read various dictionaries.
 * Note: Dictionaries are structured as follows:
 *  token\tab\POStag\tab\lemma
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SimpleLemmatizer implements Lemmatizer
{
    private static final String ENTRYNOTFOUND = "ERR: NoSuchWord";
    private static final String NOMATCHINGPOSTAG = "ERR: POStags do not match";
    // Dictionary entries: <<tok,tag>,lemma> (tok in LC, [POS]tag in UC, lemma in LC)
    private Map<AbstractMap.SimpleEntry,String> dictMap = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param dictionary 
     */
    public SimpleLemmatizer( InputStream dictionary )
    {
        BufferedReader breader = new BufferedReader(new InputStreamReader(dictionary));
        String line;
        
        try
        {
            while ((line = breader.readLine()) != null)
            {
                String[] elems = line.split("\t");
                // <<tok,tag>,lemma> (Token in LC, POStag in UC, lemma in LC)
                dictMap.put(new SimpleEntry(elems[0].toLowerCase(),elems[1].toUpperCase()),elems[2].toLowerCase());
            }
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Generates lemmas for the word and postag arrays returning the result in a
     * map of every possible lemma for each token and postag
     * (NB: OpenNLP would make better sense if tokens and tags were held in a
     * class associating tokens with tags one-2-one).
     * 
     * @param toks
     * @param tags
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return Token-lemma map
     * @throws eu.discoveri.exceptions.TokensListIsEmptyException
     * @throws eu.discoveri.exceptions.POSTagsListIsEmptyException
     * @throws ArrayLengthsDifferException
     * @throws ListLengthsDifferException (Should never happen)
     */
    @Override
    public Map<String,String> lemmatize(String[] toks, String[] tags, boolean match2NN)
            throws TokensListIsEmptyException, POSTagsListIsEmptyException, ArrayLengthsDifferException, ListLengthsDifferException
    {
        // Check array sizes match
        if( toks.length != tags.length )
            throw new ListLengthsDifferException("toks: "+toks.length+", tags: "+tags.length);

        return lemmatize(Arrays.asList(toks),Arrays.asList(tags),match2NN);
    }


    /**
     * Generates lemmas for the word and postag lists returning the result in a
     * map of every possible lemma for each token and postag
     * (NB: OpenNLP would make better sense if tokens and tags were held in a
     * class associating tokens with tags one-2-one rather than as two separate arrays)
     * 
     * @param toks Tokens of the sentence
     * @param tags POS tags of the tokens of the sentence
     * @param match2NN Match Pell NNP/NNPS to NN/NNS on dictionary
     * @throws eu.discoveri.exceptions.TokensListIsEmptyException
     * @throws eu.discoveri.exceptions.POSTagsListIsEmptyException
     * @throws ListLengthsDifferException
     * @return Token-lemma map
     */
    @Override
    public Map<String,String> lemmatize(List<String> toks, List<String> tags, boolean match2NN)
            throws TokensListIsEmptyException, POSTagsListIsEmptyException, ListLengthsDifferException
    {
        // No tokens?
        if( toks.isEmpty() )
            throw new TokensListIsEmptyException("No tokens, SimpleLemmatizer:lemmatize()");
        // No POS tags?
        if( tags.isEmpty() )
            throw new POSTagsListIsEmptyException("No POStags, SimpleLemmatizer:lemmatize()");
        // Check array sizes match
        if( toks.size() != tags.size() )
            throw new ListLengthsDifferException("toks: "+toks.size()+", tags: "+tags.size());
        
        // Remove duplicates (note: toks and tags are directly updated)
        Utils.removeDupsFromLists(toks, tags);
        
        // Read two input params (lists) into map (associates tok with tag)
        Map<String,String> tokTag = IntStream.range(0, toks.size())
                                    .boxed()
                                    .collect(Collectors.toMap(toks::get,tags::get));
        // Map for lemmas
        Map<String,String> lemmas = new HashMap<>();
        
        // For each token/tag, look up dictMap: Map<tuple,String>, being <<token,POStag>,lemma>
        tokTag.entrySet().forEach((entry) -> {
            String tok = entry.getKey().toLowerCase();                          // Compare like with like
            String tag = entry.getValue().toUpperCase();                        // Compare like with like
            // Match Pell NNP/NNPS to 2 char NN on dictionary?
            if( match2NN )
            {
                if(tag.equals(PennPOSCode.NNP.name()))
                    tag = PennPOSCode.NN.name();
                else
                    if(tag.equals(PennPOSCode.NNPS.name()))
                        tag = PennPOSCode.NNS.name();
            }
            
            // Is there such a word/token?
            String lemma = dictMap.get(new AbstractMap.SimpleEntry(tok,tag));
            if( lemma == null )
                lemmas.put(tok+":"+tag, ENTRYNOTFOUND);
            else
                lemmas.put(tok, lemma);
        });
        
        return lemmas;
    }

    /**
     * Read 'michmuch' dictionary lists and add POS tags.
     * Structured as follows: token\tab\POStag\tab\lemma
     * 
     * @param dict
     */
    public static void readMichAddTags(InputStream dict)
            throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Michmuch files not supported yet");
    }

    /**
     * Wikitionary interface for lemmas.
     * 
     * @param langCode
     * @return 
     */
    public static File getWiktionaryLemma(LangCode langCode)
            throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Wiktionary not supported yet");
    }
    
    /**
     * Get dictionary map.
     * 
     * @return 
     */
    public Map<AbstractMap.SimpleEntry,String> getDictMap() { return dictMap; }
    
    /**
     * Dump all entries of the dictionary for given POS tag (or "ALL").
     * @param posTag
     */
    public void dumpDictionary( String posTag )
    {
        // Dump dict map: Map<tuple,String>, being <<token,POStag>,lemma>
        this.dictMap.forEach((t,l)->{
            String tag = (String)t.getValue();
            if(posTag.equals("ALL"))
            {
                System.out.println("tok: " +t.getKey()+ ", tag: " +tag);
                System.out.println("  Lemma: " +l);
            }
            else
            if(tag.equals(posTag))
            {
                System.out.println("tok: " +t.getKey()+ ", tag: " +tag);
                System.out.println("  Lemma: " +l);
            }
        });
    }
    
    /**
     * Dump all the (unique) POStags of a dictionary 
     */
    public void dumpPOSTagsOfDict()
    {
        // Get the dictionary map
        Map<AbstractMap.SimpleEntry,String> dm = this.getDictMap();
        
        // Get all the keys of the dictionary map
        List<AbstractMap.SimpleEntry> dks = dm.keySet().stream().collect(Collectors.toList());
        
        // Loop over this key->value into a Set to get a unique set of tags
        Set<String> tags = new HashSet<>(); 
        dks.stream().forEach(key -> {
            tags.add((String)key.getValue());
        });
        
        // And dump
        tags.stream().sorted().forEach(t->System.out.println("tag> "+t));
    }

    /**
     * All keys of the dictionary.
     * @return 
     */
    private List<String> getAllDictKeys()
    {
        return new ArrayList(dictMap.keySet());
    }
    
    
    /**
     * M A I N (test)
     * =======
     * @param args 
     * @throws FileNotFoundException 
     * @throws ListLengthsDifferException 
     */
    static int ii = 0;
    public static void main(String[] args)
            throws FileNotFoundException, IOException, TokensListIsEmptyException, POSTagsListIsEmptyException, ListLengthsDifferException, ArrayLengthsDifferException
    {
        // Set up some NLP stuff
        Populate popl = new Populate(LangCode.en);
        
        // Dump the dictionary
        popl.getSl().dumpDictionary("NNP");
        
        // Dump the POStags
        //sl.posTagsOfDict();

        // Test lemmatizer
        String[] toks = {"Achinese","Jupiter","Moon","January","Capricorn","Science","is","science","scientists","doing","sciency","stuff","to","the","sciences","daily"};
        String[] tags = popl.getPme().tag(toks);
        Map<String,String> lems = popl.getSl().lemmatize(toks,tags,true);
        lems.forEach((k,v) -> {
            System.out.println("Key: " +k+ "("+tags[ii]+"), Val: "+v);
            ++ii;
        });
    }
}
