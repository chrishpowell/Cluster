/*
 * To be merged into generic Constants
 */

package eu.discoveri.webscrape;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Constants
{
    // Search clusters
    public static String    STORECLUSTERSPATH = "/home/chrispowell/NetBeansProjects/WebScrape/src/main/java/resources/model/";
    public static String    STORECLUSTERSEXT = "cluster";
    
    // Num. clusters to process
    public static int       TOPNUMCLUSTERS = 5;
    
    // Include/exclude site lists
    public static final List<String>  ASTROINCL = Arrays.asList("astrology","horoscope","prediction","aries","taurus","gemini","cancer","leo","virgo","libra","scorpio","sagittarius","aquarius","pisces","capricorn","zodiac");
    public static final List<String>  ASTROEXCL = Arrays.asList("tarot","shop","game","divination","psychic","crystal","numerology","email","contact","subscribe","subscription","about","fashion","beauty","culture","feedback","sitemap");
    public static final List<String>  NOTPAGE = Arrays.asList(".jpg",".jpeg",".png",".mov",".tiff",".svg",".pdf",".doc",".docx",".gif",".raw",".bmp",".ps",".psd",".webp",".tga",".dds","exr",".j2k",".pnm",".xwd",".mp4",".ai");
}
