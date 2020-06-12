/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.webscrape;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class JSoupTest2
{
    static List<String> incl = Arrays.asList("astrology","horoscope","prediction","aries","taurus","gemini","cancer","leo","virgo","libra","scorpio","sagittarius","aquarius","pisces","capricorn","zodiac");
    static List<String> excl = Arrays.asList("tarot","shop","game","divination","psychic","crystal","email","contact","fashion","beauty","culture");
    
    static Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    
    /**
     * Match input string against incl/excl list.
     * 
     * @param site Home site
     * @param link Link on each page
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public static boolean passLink( URL site, URL lURL )
            throws MalformedURLException
    {
        // Just the hostname of URLs, not any additional string that may contain host names
        String homeBase = site.getHost();
        String link = lURL.toString();
        
        return lURL.getHost().contains(homeBase) && incl.stream().anyMatch(m -> link.contains(m)) && excl.stream().noneMatch(n -> link.contains(n));
    }
    
    
    public static void main(String[] args)
            throws Exception
    {   
        org.jsoup.nodes.Document doc = null;
        doc = Jsoup.connect("https://www.msn.com/en-us/lifestyle/horoscope/whic-zodiac-signs-make-long-distance-work/ar-BB153bM7").get();
        System.out.println("==================================================");
        System.out.println(""+Jsoup.clean(doc.body().text(),Whitelist.none()).replaceAll("&amp;", "&"));
        System.out.println("==================================================");
    }
}
