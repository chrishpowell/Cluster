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
 dId INT unsigned NOT NULL AUTO_INCREMENT,
 langcode CHAR(2) DEFAULT "en",
 title VARCHAR(255) NOT NULL,
 url VARCHAR(255) NOT NULL,
 content MEDIUMTEXT NOT NULL,
 PRIMARY KEY (id),
 KEY (title)
) engine = innodb default charset=utf8 collate=utf8_bin;

MEDIUMTEXT allows up to 16Mb size document (HTML)

create table DocCluster
(
 cId INT signed,
 dId INT unsigned,
 PRIMARY KEY (cId,dId),
 CONSTRAINT docc
  FOREIGN KEY (dId) REFERENCES Document(dId)
 CONSTRAINT cdoc
  FORIEGN KEY (cId) REFERENCES Cluster(cId)
) engine = innodb default charset=utf8 collate=utf8_bin;

create table Cluster
(
 cId INT signed DEFAULT -1,
 cTags VARCHAR(255),
 PRIMARY KEY (cId)
)  engine = innodb default charset=utf8 collate=utf8_bin;

*/
//</editor-fold>

import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.config.LangSetup;
import eu.discoveri.predikt.db.DocumentDb;
import eu.discoveri.predikt.tests.AstroDocCluster;

import org.apache.commons.dbutils.ResultSetIterator;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.Document;             // Note: This is a functional interface
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.language.LanguageComponents;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
public class ScrapeDocuments
{
    /**
     * Get a list of web pages/docs.
     * @return
     * @throws MalformedURLException 
     */
    private static Map<Integer,List<URL>> search4Docs()
            throws MalformedURLException
    {
        Map<Integer,List<URL>> mapLUrl = new HashMap<>();
        List<URL> locns3 = Arrays.asList(   new URL("https://www.horoscope.com"),
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
                                            new URL("http://www.findyourlucky.com/free-daily-horoscopes.html")   );
        List<URL> locns2 = Arrays.asList(   new URL("https://www.jessicaadams.com/horoscopes"),
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
                                            new URL("https://www.asknow.com/horoscope")  );
        List<URL> locns1 = Arrays.asList(   new URL("https://georgianicols.com/daily"),
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
                                            new URL("https://astrologyking.com/weekly-horoscope")   );
        List<URL> locns4 = Arrays.asList(   new URL("https://www.horoscope.com"),
                                            new URL("https://www.astrology.com/horoscope/daily.html"),
                                            new URL("https://www.elle.com/horoscopes"),
                                            new URL("https://www.msn.com/en-us/lifestyle/horoscope"),
                                            new URL("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-today.aspx?sign=4"),
                                            new URL("https://nypost.com/horoscope")   );
        List<URL> locns0 = Arrays.asList(   new URL("https://www.yourtango.com/horoscope"),
                                            new URL("https://www.astroyogi.com/horoscopes")   );
        mapLUrl.put(0, locns0);
        mapLUrl.put(1, locns1);
        mapLUrl.put(2, locns2);
        mapLUrl.put(3, locns3);
        
        return mapLUrl;
    }
    
    /**
     * Empty document db, clean up docs and write to doc db
     * @param langSetup
     * @param locns
     * @throws SQLException
     * @throws IOException 
     */
    private static void cleanDocsWrite2Db( LangSetup langSetup, Map<Integer,List<URL>> locns )
            throws SQLException, IOException
    {
        // Match for URI
        Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        // JSoup, discoveri and Carrot2 all have Document defined
        org.jsoup.nodes.Document doc = null;
        
        // Write to Document database.
        try ( Connection conn = DocumentDb.docDb() )
        {
            // Empty the doc db
            emptyDocuments(conn);
            
            // ***** @TODO: Loop over pages-> locns.get(0..n) *****
            for( URL site: locns.get(0) )
            {
                // Links for this site
                Map<URL,Content> linkURLs = new HashMap<>();
                
                //... First, connect to site
                try
                {
                    doc = Jsoup.connect(site.toString()).get();
                    if( p.matcher(site.toString()).matches() )
                    {
                        linkURLs.put( site, new Content(langSetup.getLangCode().name(),doc.title(),
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
                                if( passLink(site,lURL) )
                                {
                                    // Get the link
                                    doc = Jsoup.connect(link).get();
                                    linkURLs.put( lURL, new Content(langSetup.getLangCode().name(),doc.title(),
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
                
                // Store the documents
                tableDocuments(conn, linkURLs);
            }
        }
    }
    
   /**
     * Empty Document table.
     * 
     * @param conn
     * @throws SQLException 
     */
    private static void emptyDocuments( Connection conn )
            throws SQLException
    {
        // Empty the table
        PreparedStatement empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.Document");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.Cluster");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.DocCluster");
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
    private static void tableDocuments( Connection conn, Map<URL,Content> linkURLs )
            throws SQLException
    {
        // Populate the table
        PreparedStatement ps = conn.prepareStatement("insert into documents.Document values(default,?,?,?,?)");
        for( Map.Entry<URL,Content> url: linkURLs.entrySet() )
        {
            ps.setString(1, url.getValue().getLangCode());
            ps.setString(2, url.getValue().getTitle());
            ps.setString(3, url.getKey().toString());
            ps.setString(4, url.getValue().getContent());
            ps.executeUpdate();
        }
        
        System.out.println("==============================================");
        linkURLs.entrySet().forEach((url) -> {
            System.out.println("  " +url.getKey().toString()+ "(" +url.getValue().getTitle()+")");
        });
        System.out.println("==============================================");
    }
    
    /**
     * Match input string against incl/excl list.
     * 
     * @param site Home site
     * @param lURL Link on each page
     * @return 
     * @throws java.net.MalformedURLException 
     */
    private static boolean passLink( URL site, URL lURL )
            throws MalformedURLException
    {
        // Include/exclude keywords
        List<String> incl = Arrays.asList("astrology","horoscope","prediction","aries","taurus","gemini","cancer","leo","virgo","libra","scorpio","sagittarius","aquarius","pisces","capricorn","zodiac");
        List<String> excl = Arrays.asList("tarot","shop","game","divination","psychic","crystal","numerology","email","contact","subscribe","subscription","about","fashion","beauty","culture","feedback","sitemap");

        // Just the hostname of URLs, not any additional string that may contain host names
        String homeBase = site.getHost();
        String link = lURL.toString();
        
        return lURL.getHost().contains(homeBase) && incl.stream().anyMatch(m -> link.contains(m)) && excl.stream().noneMatch(n -> link.contains(n));
    }
    
    /**
     * Classify documents.
     * @throws SQLException 
     */
    static int docCount = 0, cId = 0;
    private static void classifyDocs()
            throws SQLException, IOException
    {
        // Get the docs into result set
        Stream<AstroDocCluster> sd;
        try( Connection conn = DocumentDb.docDb() )
        {
            // Get the docs into result set
            // @TODO: Paginate
            System.out.println("... Read all documents from Rdb");
            PreparedStatement docs = conn.prepareStatement("select * from Document");
            ResultSet rsDocs = docs.executeQuery();
            
            // Convert to stream [0]:dId, [1]:langCode, [2]:title, [3]:url, [4]:content
            Iterable<Object[]> rsi = ResultSetIterator.iterable(rsDocs);
            sd = StreamSupport.stream(rsi.spliterator(), false)
                    .map( rowf -> new AstroDocCluster((Long)rowf[0],(String)rowf[1],(String)rowf[2],(String)rowf[4]) );
        
            // Now, classify the docs
            // Our documents are in English so we load appropriate language resources.
            // This call can be heavy and an instance of LanguageComponents should be
            // created once and reused across different clustering calls.
            LanguageComponents languageComponents = LanguageComponents.loader().load().language("English");
            LingoClusteringAlgorithm lca = new LingoClusteringAlgorithm();

            // Suggest num. clusters
            lca.desiredClusterCount.set(20);

            // Perform clustering.
            // **** Note: Can only go to about 5000 documents.  Lingo4G for large collections.
            // Not ideal: Run this several times over paginated serach4Docs()
            List<Cluster<Document>> clusters = lca.cluster(sd, languageComponents);
            
            // Print cluster labels and a document count in each top-level cluster.
            System.out.println(padRightChars("Labels",' ',80)+"\tDoc. count");
            System.out.println(padRightChars("------",'-',80)+"\t----------");
            clusters.forEach(c -> {
                List<String> ls = c.getLabels();
                List<Document> ld = c.getDocuments();
                String labels = new HashSet(ls).toString();
                System.out.println(padRightChars(labels,' ',80) +"\t"+ ld.size());
                docCount += ld.size();
            });
            System.out.println("Total cluster/document count (a doc can be in multiple clusters): " +docCount);
            System.out.println("");

            System.out.println("... Insert into Cluster and link tables");
            PreparedStatement dci = conn.prepareStatement("insert into documents.DocCluster values(?,?)");
            PreparedStatement ci  = conn.prepareStatement("insert into documents.Cluster values(?,?)");
            clusters.forEach(c -> {
                // Cluster
                try
                {
                    ci.setLong(1, cId);
                    ci.setString(2, c.getLabels().toString().replaceAll(", ", " ").replaceAll("\\[|\\]", "").replaceAll("'", "\'"));
                    
                    ci.executeUpdate();
                }
                catch( SQLException ex )
                {
                    Logger.getLogger(ScrapeDocuments.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                // DocCluster (link table Document to Cluster)
                c.getDocuments().forEach(d -> {
                    try
                    {
                        dci.setLong(1, cId);
                        dci.setLong(2, ((AstroDocCluster)d).getId());

                        dci.executeUpdate();
                    }
                    catch( SQLException ex )
                    {
                        Logger.getLogger(ScrapeDocuments.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                    
                ++cId;
            });
        }
    }
    
    /**
     * Count of documents per cluster check
     */
    private static void clusterCounts()
            throws SQLException
    {
        long totDocCount = 0;
        
        try( Connection conn = DocumentDb.docDb() )
        {
            PreparedStatement ps = conn.prepareStatement("select cId,cTags,count(*) as countDocs from Document group by cId,cTags");
            ResultSet rsDocs = ps.executeQuery();
            
            System.out.println("ClusterId\t"+padRightChars("Labels",' ',80)+"\tDoc. count");
            System.out.println("---------\t"+padRightChars("------",'-',80)+"\t----------");
            while( rsDocs.next() )
            {
                System.out.print(rsDocs.getLong("cId"));
                System.out.print("\t\t"+padRightChars(rsDocs.getString("cTags"),' ',80));
                long docCount1 = rsDocs.getLong("countDocs");
                totDocCount += docCount1;
                System.out.println("\t"+docCount1);
            }
            System.out.println("Total docs: " +totDocCount);
        }
    }
    
    /**
     * Pad string right with spaces
     * @param inputString
     * @param ch
     * @param length
     * @return 
     */
    public static String padRightChars( String inputString, char ch, int length )
    {
        // Empty/null string
        if( inputString == null )
        {
            StringBuilder sb = new StringBuilder();
            while( sb.length() < length ) sb.append(ch);
            return sb.toString();
        }
        
        // No padding needed
        if( inputString.length() >= length ) { return inputString; }
        
        // Pad to length
        StringBuilder sb = new StringBuilder(inputString);
        while( sb.length() < length ) { sb.append(ch); }

        return sb.toString();
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
        // Timer
        long totSecs = 0l;
        Instant st = Instant.now();
        
        //... (Fake) Search
        Map<Integer,List<URL>> locns = search4Docs();
        
        // Timer
        Instant en = Instant.now();
        long secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> Search time (secs): " +secs+ "\r\n");
        st = Instant.now();
        
        // Language/locale setup
        LangSetup langSetup = new EnSetup();
        System.out.println("... Setup ["+langSetup.getLangCode().getName()+"]");
        
        //... Clean out the HTML and write content to RdB
        System.out.println("\r\nDocuments, clean and store...");
        cleanDocsWrite2Db( langSetup, locns );
        
        // Timer
        en = Instant.now();
        secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> Clean HTML and store docs (secs): " +secs+ "\r\n");
        st = Instant.now();
        
        //... Document classification
        // **** Note: Can only go to about 5000 documents.  Lingo4G for large collections.
        // Not ideal: Run this several times over paginated search4Docs()
        classifyDocs();
        
        // Timer
        en = Instant.now();
        secs = Duration.between(st,en).toSeconds();
        totSecs += secs;
        System.out.println("...> Document classification (secs): " +secs+ "\r\n");
        
        // Show cluster counts
        //clusterCounts();
        
        long s = totSecs % 60;
        long h = totSecs / 60;
        long m = h % 60;
        h /= 60;
        System.out.println("Total processing time: " +h +":"+ m +":"+ s);
    }
}
