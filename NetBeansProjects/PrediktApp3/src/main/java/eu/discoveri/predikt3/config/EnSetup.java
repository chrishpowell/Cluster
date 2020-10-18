/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.config;

import eu.discoveri.predikt3.sentences.English;
import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.sentences.Language;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class EnSetup implements LangSetup
{
    // Lemmas
    public static class Lemma
    {
        // Apostrophes
        public static final String      ENAPOST = Constants.RESMODELS+"en-apostrophes.properties";
        
        // Stopwords
        public static final String      ENSTOP = Constants.RESMODELS+"en-stopwords.txt";
        
        // Node (POS) types to keep
        public static List<String>      keepNodes = List.of("VB","NN");
        public static List<String>      unWanted = List.of("");
        // Lemma flags
        public static boolean           match2NN = false;
        public static boolean           noNumbers = true;
    }

    /**
     * Apostrophe file path.
     * @return 
     */
    @Override
    public String getApostrophes() { return Lemma.ENAPOST; }
    /**
     * Load apostrophes properties
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @Override
    public Properties loadApostrophesProperties()
            throws FileNotFoundException, IOException
    {
        Properties enProps = new Properties();
        enProps.load(new FileInputStream(getApostrophes()));
        
        return enProps;
    }

    /**
     * Stop words file path.
     * @return 
     */
    @Override
    public String getStops() { return Lemma.ENSTOP; }
    /**
     * Load stop words list.
     * @return
     * @throws IOException 
     */
    @Override
    public List<String> loadStopWords()
            throws IOException
    {
        return Files.readAllLines(Paths.get(getStops()));
    }
    
    @Override
    public List<String> getKeepNodes() { return Lemma.keepNodes; }
    @Override
    public List<String> getUnWanted() { return Lemma.keepNodes; }
    @Override
    public boolean getMatch2NN() { return Lemma.match2NN; }
    @Override
    public boolean getNoNumbers() { return Lemma.noNumbers; }
    
    
    // Language and Locale
    public static class LangLocale
    {
        private static final LangCode   langCode = LangCode.en;
        private static final Language   lang = new English();
        private static final Locale     locl = Locale.ENGLISH;
    }

    @Override
    public LangCode getLangCode() { return LangLocale.langCode; }
    @Override
    public Language getLanguage() { return LangLocale.lang; }
    @Override
    public Locale getLocale() { return LangLocale.locl; }
}
