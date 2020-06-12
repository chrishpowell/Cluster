/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.webscrape;

/*

//<editor-fold defaultstate="collapsed" desc="Document">
create table Document
(
id INT unsigned NOT NULL AUTO_INCREMENT,
langcode CHAR(2) DEFAULT "en",
title VARCHAR(255) NOT NULL,
url VARCHAR(255) NOT NULL,
content MEDIUMTEXT NOT NULL,
PRIMARY KEY (id),
KEY (title)
) engine = innodb default charset=utf8 collate=utf8_bin;

MEDIUMTEXT allows up to 16Mb size document (HTML)

*/
//</editor-fold>

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.HttpStatusException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class JSoupTest
{
    static List<String> incl = Arrays.asList("astrology","horoscope","prediction","aries","taurus","gemini","cancer","leo","virgo","libra","scorpio","sagittarius","aquarius","pisces","capricorn","zodiac");
    static List<String> excl = Arrays.asList("tarot","shop","game","divination","psychic","crystal","numerology","email","contact","subscribe","subscription","about","fashion","beauty","culture","feedback","sitemap");
    
    static Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");


    /**
     * Connection for documents db
     * @return
     * @throws Exception 
     */
    public static Connection docDb()
            throws Exception
    {
        String URL = "jdbc:mysql://localhost:3306/documents?useSSL=false&serverTimezone=CET";
        String USER = "chrispowell";
        String PWD = "karabiner";
        
        return DriverManager.getConnection(URL,USER,PWD);
    }
    
    /**
     * Empty Document table.
     * 
     * @param conn
     * @throws SQLException 
     */
    public void emptyDocuments( Connection conn )
            throws SQLException
    {
        // Empty the table
        PreparedStatement empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.Document");
        empty.executeUpdate();
        empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1");
        empty.executeUpdate();
    }
    
    /**
     * Store documents.
     * @param conn
     * @param linkURLs
     * @throws Exception 
     */
    public void tableDocuments( Connection conn, Map<URL,Content> linkURLs )
            throws Exception
    {
        // Populate the table
        PreparedStatement ps = conn.prepareStatement("insert into documents.Document values(default,?,?,?)");
        for( Map.Entry<URL,Content> url: linkURLs.entrySet() )
        {
            ps.setString(1, url.getValue().getTitle());
            ps.setString(2, url.getKey().toString());
            ps.setString(3, url.getValue().getContent());
            ps.executeUpdate();
        }
    }
    
    /**
     * Match input string against incl/excl list.
     * 
     * @param site Home site
     * @param lURL Link on each page
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public boolean passLink( URL site, URL lURL )
            throws MalformedURLException
    {
        // Just the hostname of URLs, not any additional string that may contain host names
        String homeBase = site.getHost();
        String link = lURL.toString();
        
        return lURL.getHost().contains(homeBase) && incl.stream().anyMatch(m -> link.contains(m)) && excl.stream().noneMatch(n -> link.contains(n));
    }
    
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
            throws Exception
    {
//        List<URL> locnsAll = Arrays.asList(    new URL("https://www.horoscope.com"),
//                                            new URL("https://www.astrology.com/horoscope/daily.html"),
//                                            new URL("https://www.elle.com/horoscopes"),
//                                            new URL("https://www.msn.com/en-us/lifestyle/horoscope"),
//                                            new URL("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-today.aspx?sign=4"),
//                                            new URL("https://nypost.com/horoscope"),
//                                            new URL("https://www.yourtango.com/horoscope"),
//                                            new URL("https://www.astroyogi.com/horoscopes"),
//                                            new URL("https://www.eugenialast.com/astro-weekly"),
//                                            new URL("https://www.astrolis.com"),
//                                            new URL("https://horoscopes.proastro.com"),
//                                            new URL("https://www.ganeshaspeaks.com/horoscopes"),
//                                            new URL("https://www.prokerala.com/astrology/horoscope"),
//                                            new URL("https://www.californiapsychics.com/horoscope"),
//                                            new URL("https://astrologyanswers.com/horoscopes"),
//                                            new URL("http://horoscope-daily-free.net"),
//                                            new URL("http://www.findyourlucky.com/free-daily-horoscopes.html"),
//                                            new URL("https://www.jessicaadams.com/horoscopes"),
//                                            new URL("https://www.patrickarundell.com/horoscopes"),
//                                            new URL("https://www.sunsigns.com/horoscopes/daily"),
//                                            new URL("https://www.washingtonpost.com/entertainment/horoscopes"),
//                                            new URL("https://www.weeklyhoroscope.com"),
//                                            new URL("https://askastrology.com/horoscopes"),
//                                            new URL("https://www.horoscopedates.com/daily-horoscope"),
//                                            new URL("https://www.sfgate.com/horoscope"),
//                                            new URL("https://www.lifereader.com/free-daily-horoscope"),
//                                            new URL("https://www.tarot.com/daily-horoscope"),
//                                            new URL("https://www.yahoo.com/lifestyle/horoscope"),
//                                            new URL("https://www.huffpost.com/horoscopes"),
//                                            new URL("https://dailyhoroscopes.net"),
//                                            new URL("https://www.free-horoscope.com"),
//                                            new URL("https://www.horoscope.com/us/index.aspx"),
//                                            new URL("https://www.cainer.com"),
//                                            new URL("http://www.gotohoroscope.com"),
//                                            new URL("https://www.asknow.com/horoscope"),
//                                            new URL("https://georgianicols.com/daily"),
//                                            new URL("https://www.astrologyzone.com/horoscopes"),
//                                            new URL("https://astrostyle.com/horoscopes/daily"),
//                                            new URL("https://jeffprince.com/horoscopes"),
//                                            new URL("https://www.msn.com/en-ca/lifestyle/horoscope"),
//                                            new URL("https://www.indastro.com"),
//                                            new URL("https://www.dailyhoroscope.com"),
//                                            new URL("https://cafeastrology.com/sundailyhoroscopes.html"),
//                                            new URL("https://www.powerfortunes.com/daily_horoscope_detail.php?sign=Capricorn"),
//                                            new URL("https://www.chicagotribune.com/horoscopes"),
//                                            new URL("https://www.lcblack.com/cancer_horoscope"),
//                                            new URL("https://www.elle.com/horoscopes/daily"),
//                                            new URL("https://www.0800-horoscope.com"),
//                                            new URL("https://astrologyking.com/weekly-horoscope")   );
        List<URL> locns = Arrays.asList(    new URL("https://www.horoscope.com"),
                                            new URL("https://www.astrology.com/horoscope/daily.html"),
                                            new URL("https://www.elle.com/horoscopes"),
                                            new URL("https://www.msn.com/en-us/lifestyle/horoscope"),
                                            new URL("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-today.aspx?sign=4"),
                                            new URL("https://nypost.com/horoscope"),
                                            new URL("https://www.yourtango.com/horoscope"),
                                            new URL("https://www.astroyogi.com/horoscopes")   );
        
        Map<URL,Content> linkURLs = new HashMap<>();
        
        // Start
        JSoupTest jst = new JSoupTest();
        
        try ( Connection conn = docDb() )
        {
            org.jsoup.nodes.Document doc = null;
            for( URL site: locns )
            {
                //... First, connect to site
                try
                {
                    doc = Jsoup.connect(site.toString()).get();
                    if( p.matcher(site.toString()).matches() )
                    {
                        linkURLs.put( site, new Content(doc.title(),
                                      Jsoup.clean(doc.body().text(),Whitelist.none()).replaceAll("&amp;", "&")) );
                    }
                }
                catch( SocketTimeoutException stx )
                {
                    System.out.println( "!! Timed out on " +site.toString()+ ", " +stx.getMessage() );
                    continue;
                }
                
                //... Second, get links to crawl sites
                Elements links = doc.select("a[href]");
                for( Element e: links)
                {
                    String link = e.attr("abs:href");
                    
                    // Check link is good (at least no blanks etc.)
                    if( p.matcher(link).matches() )
                    {
                        // Connect to link and process
                        try
                        {
                            // Form the URL
                            URL lURL = new URL(link);
                            
                            // Avoid duplicate links
                            if( !linkURLs.containsKey(lURL) )
                            {
                                // Check we like the look of this link (no 'shop' etc.)
                                if(  jst.passLink(site,lURL) )
                                {
                                    // Get the link
                                    doc = Jsoup.connect(link).get();
                                    linkURLs.put( lURL, new Content(doc.title(),
                                                  Jsoup.clean(doc.body().text(),Whitelist.none()).replaceAll("&amp;", "&")) );
                                }
                            }
                        }
                        catch( SocketTimeoutException | HttpStatusException ex )
                        {
                            System.out.println( "!! Timed out or HTTP status exception on " +link+ ", " +ex.getMessage() );
                        }
                    }
                }
            }
            
            System.out.println("\r\nDocuments, clean and store...");
            jst.emptyDocuments(conn);
            jst.tableDocuments(conn, linkURLs);
        }
        
        System.out.println("==============================================");
        linkURLs.entrySet().forEach((url) -> {
            System.out.println("  " +url.getKey().toString()+ "(" +url.getValue().getTitle()+")");
        });
        System.out.println("==============================================");
    }
}

/**
 * For db.
 * @author Chris Powell, Discoveri OU
 */
class Content
{
    private final String    title;
    private final String    content;
    
    public Content( String title, String content )
    {
        this. title = title;
        this.content = content;
    }

    public String getTitle() { return title; }

    public String getContent() { return content; }
}