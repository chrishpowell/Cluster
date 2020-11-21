/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.webscrape;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 *
 * @author chrispowell
 */
public class WebPageCounter
{
    // Log output
    private static final Logger logger = Logger.getLogger("eu.discoveri.webscrape.webpagecounter");
    // Web page pattern
    private static final Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    
    /**
     * Match input string against incl/excl list.
     * 
     * @param homeBase "Root" of pages
     * @param lURL Link on each page
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public boolean passLink( String homeBase, URL lURL )
            throws MalformedURLException
    {
        // Link to check
        String link = lURL.toString();
        
        // Include/exclude accordingly
        return lURL.getHost().contains(homeBase) && Constants.ASTROINCL.stream().anyMatch(m -> link.contains(m)) 
                                                 && Constants.ASTROEXCL.stream().noneMatch(n -> link.contains(n)) 
                                                 && Constants.NOTPAGE.stream().noneMatch(n -> link.contains(n));
    }
    
    public boolean passLink1( String homeBase, URL lURL )
            throws MalformedURLException
    {
        // Link to check
        String link = lURL.toString();
        
        // Include/exclude accordingly
        return lURL.getHost().contains(homeBase) && Constants.NOTPAGE.stream().noneMatch(n -> link.contains(n));
    }
    
    /**
     * Recurse down the links.
     * @param doc Current page
     * @param homeBase The "root" site, eg: https://www.horoscope.com
     * @param linkURLs Duplicate links
     * @return  
     * @throws java.net.MalformedURLException 
     */
    public int recurseLinks( org.jsoup.nodes.Document doc, String homeBase, Map<String,Set<URL>> linkURLs )
            throws MalformedURLException, IOException
    {
        int totPageCnt = 0;
        
        //... Second, get links to crawl sites
        Elements links = doc.select("a[href]");
        String parentPage = doc.location();
        logger.log(Level.INFO, " Num. links on: {0}: {1}", new Object[]{parentPage, links.size()});
        
        // Check if parent in link map, otherwise make new entry
        if( !linkURLs.containsKey(doc.location()) )
            linkURLs.put( parentPage, new HashSet<>() );

        // Get the 'owner' of these links
        Set<URL> su = linkURLs.get(parentPage);
        for( Element e: links )
        {
            // Form complete link (if relative)
            String link = e.attr("abs:href");
            logger.log(Level.INFO, "   > Link {0}", link);

            // Check link is good (at least no blanks etc.)
            if( p.matcher(link).matches() )
            {
                // Connect to link and process
                try
                {
                    // Form the URL (URL not string, 'cos parts need checking)
                    URL lURL = new URL(link);

                    // Avoid duplicate links
                    if( !su.contains(lURL) )
                    {
                        // Check we like the look of this link (no 'shop' etc.)
                        if(  passLink1(homeBase,lURL) )
                        {
                            // Add the link to Set
                            su.add(lURL);
                            logger.log(Level.INFO, "     -> {0}", lURL);

                            // Total = Total + this page + any descendant count
                            totPageCnt += 1 + recurseLinks(Jsoup.connect(link).get(),homeBase,linkURLs);
                        }
                    }
                }
                catch( UnsupportedMimeTypeException ux ){} // Only (hokey) method for continuing process for mime types such as PDFs
                catch( SocketTimeoutException sex )
                {
//                    String st = Arrays.asList(ex.getStackTrace()).stream().map(s -> s.toString()).collect(Collectors.joining("\r\n"));
//                    logger.log(Level.SEVERE, "!! WebPageCounter: Timed out or HTTP status exception on {0}\r\n {1}", new Object[]{link,st});
                    logger.log(Level.SEVERE, "!! WebPageCounter: Timed out or HTTP status exception on: {0}, {1}", new Object[]{link,sex.getMessage()});
                }
                catch( HttpStatusException hex )
                {
                    logger.log(Level.SEVERE, "!! WebPageCounter: HTTP status exception on: {0}, {1} ({3})", new Object[]{link,hex.getMessage(),hex.getStatusCode()});
                }
                catch( javax.net.ssl.SSLHandshakeException nex )
                {
                    logger.log(Level.SEVERE, "!! Possible remote LDAP configuration error: {0}, {1}", new Object[]{link, nex.getMessage()});
                }
                catch( Exception ex )
                {
                    String st = Arrays.asList(ex.getStackTrace()).stream().map(s -> s.toString()).collect(Collectors.joining("\r\n"));
                    logger.log(Level.SEVERE, "!! WebPageCounter: {0}\r\n{1}", new Object[]{link,st});
                }
            }
        }

        return totPageCnt;
    }
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static void main(String[] args)
            throws MalformedURLException, IOException
    {
        List<URL> locns1 = Arrays.asList(    new URL("https://www.horoscope.com"),
                                            new URL("https://www.astrology.com/horoscope/daily.html"),
                                            new URL("https://www.elle.com/horoscopes"),
                                            new URL("https://www.msn.com/en-us/lifestyle/horoscope"),
                                            new URL("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-today.aspx?sign=4"),
                                            new URL("https://nypost.com/horoscope"),
                                            new URL("https://www.yourtango.com/horoscope"),
                                            new URL("https://www.astroyogi.com/horoscopes"),
                                            new URL("https://www.eugenialast.com/astro-weekly"),
                                            new URL("https://www.astrolis.com"),
                                            new URL("https://horoscopes.proastro.com"),
                                            new URL("https://www.ganeshaspeaks.com/horoscopes"),
                                            new URL("https://www.prokerala.com/astrology/horoscope"),
                                            new URL("https://www.californiapsychics.com/horoscope"),
                                            new URL("https://astrologyanswers.com/horoscopes"),
                                            new URL("http://horoscope-daily-free.net"),
                                            new URL("http://www.findyourlucky.com/free-daily-horoscopes.html"),
                                            new URL("https://www.jessicaadams.com/horoscopes"),
                                            new URL("https://www.patrickarundell.com/horoscopes"),
                                            new URL("https://www.sunsigns.com/horoscopes/daily"),
                                            new URL("https://www.washingtonpost.com/entertainment/horoscopes"),
                                            new URL("https://www.weeklyhoroscope.com"),
                                            new URL("https://askastrology.com/horoscopes"),
                                            new URL("https://www.horoscopedates.com/daily-horoscope"),
                                            new URL("https://www.sfgate.com/horoscope"),
                                            new URL("https://www.lifereader.com/free-daily-horoscope"),
                                            new URL("https://www.tarot.com/daily-horoscope"),
                                            new URL("https://www.yahoo.com/lifestyle/horoscope"),
                                            new URL("https://www.huffpost.com/horoscopes"),
                                            new URL("https://dailyhoroscopes.net"),
                                            new URL("https://www.free-horoscope.com"),
                                            new URL("https://www.horoscope.com/us/index.aspx"),
                                            new URL("https://www.cainer.com"),
                                            new URL("http://www.gotohoroscope.com"),
                                            new URL("https://www.asknow.com/horoscope"),
                                            new URL("https://georgianicols.com/daily"),
                                            new URL("https://www.astrologyzone.com/horoscopes"),
                                            new URL("https://astrostyle.com/horoscopes/daily"),
                                            new URL("https://jeffprince.com/horoscopes"),
                                            new URL("https://www.msn.com/en-ca/lifestyle/horoscope"),
                                            new URL("https://www.indastro.com"),
                                            new URL("https://www.dailyhoroscope.com"),
                                            new URL("https://cafeastrology.com/sundailyhoroscopes.html"),
                                            new URL("https://www.powerfortunes.com/daily_horoscope_detail.php?sign=Capricorn"),
                                            new URL("https://www.chicagotribune.com/horoscopes"),
                                            new URL("https://www.lcblack.com/cancer_horoscope"),
                                            new URL("https://www.elle.com/horoscopes/daily"),
                                            new URL("https://www.0800-horoscope.com"),
                                            new URL("https://astrologyking.com/weekly-horoscope"),
                                            new URL("https://www.horoscope.com"),
                                            new URL("https://www.astrology.com/horoscope/daily.html"),
                                            new URL("https://www.elle.com/horoscopes"),
                                            new URL("https://www.msn.com/en-us/lifestyle/horoscope"),
                                            new URL("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-today.aspx?sign=4"),
                                            new URL("https://nypost.com/horoscope"),
                                            new URL("https://www.yourtango.com/horoscope"),
                                            new URL("https://www.astroyogi.com/horoscopes")   );
//        List<URL> locns = Arrays.asList(    new URL("https://www.astrology.com/horoscope/daily.html")  );
        List<URL> locns = Arrays.asList(    new URL("https://felixbaumgartner.com"),
                                            new URL("https://grantbaumgartner.com")  );
        
        // Logging level
        Logger.getLogger("eu.discoveri.webscrape.webpagecounter").setLevel(Level.SEVERE);
        
        // Check for duplicates
        Map<String,Set<URL>> pPages = new HashMap<>();
        Set<URL> linkURLs = new HashSet<>();
        
        // Init
        WebPageCounter wpc = new WebPageCounter();
        
        // Connect to sites in turn
        org.jsoup.nodes.Document doc = null;
        for( URL site: locns )
        {
            Instant std = Instant.now();
            
            System.out.println("Processing site: "+site+" ["+std+"], please wait...");
            //... Ok, connect to site
            try
            {
                // Connect to site
                doc = Jsoup.connect(site.toString()).get();
                // Duplicator check
                if( p.matcher(site.toString()).matches() )
                    linkURLs.add(site);
            }
            catch( SocketTimeoutException stx )
            {
                logger.log(Level.SEVERE, "!! Timed out on {0}, {1}", new Object[]{site, stx.getMessage()});
                continue;
            }
            catch( javax.net.ssl.SSLHandshakeException nex )
            {
                logger.log(Level.SEVERE, "!! Possible remote LDAP configuration error: {0}, {1}", new Object[]{site, nex.getMessage()});
                continue;
            }
            catch( HttpStatusException hex )
            {
                logger.log(Level.SEVERE, "!! WebPageCounter: HTTP status exception on: {0}, {1} ({3})", new Object[]{site,hex.getMessage(),hex.getStatusCode()});
            }
            catch( Exception ex )
            {
                String st = Arrays.asList(ex.getStackTrace()).stream().map(s -> s.toString()).collect(Collectors.joining("\r\n"));
                logger.log(Level.SEVERE, "!! WebPageCounter: {0}", st);
                continue;
            }
            
            // Ok, update the 'parent' link map
            pPages.put(site.toString(), linkURLs);

            // Recurse and count the pages
            int totPages = wpc.recurseLinks( doc, site.getHost(), pPages );
            
            System.out.println("\r\n---------------------------------------------------------------------------");
            System.out.println("Site: " +site+ ", docs/pages: " +totPages+ ", duration (mins): " +Duration.between(std,Instant.now()).toMinutes() );
            System.out.println("---------------------------------------------------------------------------");
            
            pPages.forEach((k,v) -> {
                System.out.println("Page: " +k+ ", num.links: " +v.size());
                System.out.println("   " +v);
            });
            System.out.println("");
        }
    }
}
