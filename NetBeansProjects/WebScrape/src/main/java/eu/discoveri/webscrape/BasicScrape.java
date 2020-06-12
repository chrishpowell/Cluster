/*
 */
package eu.discoveri.webscrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class BasicScrape
{
    private static String basic()
        throws IOException
    {
        Document doc = Jsoup.connect("https://www.prokerala.com/astrology/horoscope").get();
        // & is converted to &amp; in some (not all cases)
        // See: https://stackoverflow.com/questions/31379040/jsoup-converting-to-amp-when-i-require-that-info-as-it-is
        //   and basic1() below (which does not convert).
        String cleanText = Jsoup.clean(doc.body().text(),Whitelist.none()).replaceAll("&amp;", "&");

        return cleanText;
    }
    
    private static String basic1()
    {
        String s = "<dl>\n" +
"  <dt>Coffee</dt>\n" +
"  <dd><a href=\"#\">Black & hot drink</a></dd>\n" +
"  <dt>Letter &oacute;</dt>\n" +
"  <dd>White cold drink</dd>\n" +
"</dl>";
        
        return Jsoup.parse(s).text();  // or use clean()
    }
    
    public static void boiler(){}
    
    private static void tika1()
            throws IOException, MalformedURLException
    {
        URL url = new URL("https://astro.com");
        BufferedReader read = new BufferedReader( new InputStreamReader(url.openStream()) );
        
        String s;
        while( (s = read.readLine()) != null )
            System.out.println(s);
    }
    
    private static String tika2()
            throws IOException, MalformedURLException, SAXException, TikaException
    {
        URL url = new URL("https://astro.com");
//        BoilerpipeContentHandler handler = new BoilerpipeContentHandler(new BodyContentHandler());
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        HtmlParser hp = new HtmlParser();
//        AutoDetectParser hp = new AutoDetectParser();
        
        try( InputStream stream = url.openStream() )
        {
            hp.parse(stream, handler, metadata, new ParseContext());
            return handler.toString();
        }
    }
    
    public static void main(String[] args)
            throws IOException, MalformedURLException, SAXException, TikaException
    {
        System.out.println("----> " +basic());
    }
}
