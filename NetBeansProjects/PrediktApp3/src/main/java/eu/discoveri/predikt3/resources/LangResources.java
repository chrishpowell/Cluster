/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.resources;

import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.sentences.Language;

import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LangResources
{
    private final LangCode                              langCode;
    private final Language                              lang;
    private final Locale                                locl;
    private final Properties                            apostrophes;
    private final Map<AbstractMap.SimpleEntry,String>   tagsDict;
    private final List<String>                          stopWords;
    
    /**
     * Constructor.
     * 
     * @param langCode
     * @param lang
     * @param locl
     * @param apostrophes
     * @param stopWords
     * @param tagsDict 
     */
    public LangResources(   LangCode langCode,
                            Language lang,
                            Locale locl,
                            Properties apostrophes,
                            List<String> stopWords,
                            Map<AbstractMap.SimpleEntry,String> tagsDict  )
    {
        this.langCode = langCode;
        this.lang = lang;
        this.locl = locl;
        this.apostrophes = apostrophes;
        this.stopWords = stopWords;
        this.tagsDict = tagsDict;
    }

    public LangCode getLangCode() { return langCode; }
    
    public Language getLanguage() { return lang; }
    
    public Locale getLocale() { return locl; }
    
    public Properties getApostrophes() { return apostrophes; }

    public List<String> getStopWords() { return stopWords; }

    public Map<AbstractMap.SimpleEntry,String> getTagsDict() { return tagsDict; }
}
