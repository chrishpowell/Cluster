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
import org.jsoup.select.Elements;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class JSoupTest1
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
    
    
    public static void main1(String[] args)
            throws Exception
    {
        List<URL> locns = Arrays.asList(    new URL("https://www.horoscope.com"),
                                            new URL("https://www.astrology.com/horoscope/daily.html"),
                                            new URL("https://www.elle.com/horoscopes")    );
        
        org.jsoup.nodes.Document doc = null;
        Set<URL> linkURLs = new HashSet<>();                                    // To eliminate duplicate links
        for( URL site: locns )
        {
            System.out.println("Processing: " +site);
            doc = Jsoup.connect(site.toString()).get();
            Elements links = doc.select("a[href]");
            
            // Get links to crawl sites
            for( Element e: links)
            {
                String link = e.attr("abs:href");
                if( p.matcher(link).matches() )
                {
                    System.out.println("   Link: " +link);

                    URL lURL = new URL(link); 
                    if(  passLink(site,lURL) )
                        linkURLs.add(lURL);
                }
            }
            
            System.out.println("\r\n==============================================");
            linkURLs.forEach(l -> System.out.println("  " +l.toString()));
            System.out.println("==============================================");
        }
    }
    
    /**
     * Base test.
     * @param args
     * @throws MalformedURLException 
     */
    public static void main(String[] args) throws MalformedURLException
    {
        List<String> links = Arrays.asList( "https://www.astrology.com/us/games/game-book-of-love.aspx",
                                            "https://www.astrology.com/us/psychics/KEEN-psychic-reading.aspx?cat=195",
                                            "https://www.elle.com/horoscopes/weekly/a65/cancer-weekly-horoscope/",
                                            "https://www.elle.com/horoscopes/#",
                                            "http://astronargon.us/The%20Whole%20Astrology%20Workbook.pdf",
                                            "https://www.astrology.com/horoscope/daily-couples.html"    );
        
        for( String l: links )
        {
            if( p.matcher(l).matches() )
            {
                System.out.println(" --> " +l);
                URL lURL = new URL(l);
                if(  passLink(new URL(lURL.getProtocol()+"://"+lURL.getHost()),lURL) )
                    System.out.println("    Passed->> " +lURL.getHost());
            }
        }
    }
}
