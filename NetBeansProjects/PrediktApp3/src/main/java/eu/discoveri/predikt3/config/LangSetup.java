/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.config;

import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.sentences.Language;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 */
public interface LangSetup
{
    // Lemma setup
    public String getApostrophes();
    public List<String> getKeepNodes();
    public String getStops();
    public List<String> getUnWanted();
    public boolean getMatch2NN();
    public boolean getNoNumbers();
    
    public Properties loadApostrophesProperties()
        throws FileNotFoundException, IOException;
    public List<String> loadStopWords()
        throws IOException;
    
    // Language/Locale setup
    public LangCode getLangCode();
    public Language getLanguage();
    public Locale getLocale();
}
