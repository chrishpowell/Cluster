/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.searchapi.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
//import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;


/**
 *
 * @author chrispowell
 */
public class SearchDocs
{
    private static String       subscriptionKey = "3dc9b0801a9f4856b8e5fc86387e4727";
    private static String       host = "https://api.cognitive.microsoft.com/";
    private static String       path = "bing/v7.0/search";
    private static ObjectMapper om = new ObjectMapper();
    
    /**
     * Do a search.
     * 
     * @param searchQuery
     * @return
     * @throws Exception 
     */
    public static SearchResults SearchWeb( String searchQuery )
            throws Exception
    {
        URL url = new URL(host+path+"?q=" +URLEncoder.encode(searchQuery, "UTF-8"));
        Map<String,String>  rHdrs = new HashMap<>();
        
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        conn.setRequestProperty("responseFilter", "webpages,news,-images,-videos");
        conn.setRequestProperty("X-MSEdge-ClientID", "16FC7CF69BDA6F0638C973709A1C6E28");
        
        SearchResults srs;
        try( InputStream ins = conn.getInputStream() )
        {
            // Scan for alphanumeric?
            String resp = new Scanner(ins).useDelimiter("\\A").next();
            
            // Get the input stream headers
            Map<String,List<String>> hdrs = conn.getHeaderFields();
            
            // For specific (search return) headers
            hdrs.keySet().stream()
                    .filter(h -> ( h != null )).filter(h -> ( h.startsWith("BingAPIs-") || h.startsWith("X-MSEdge-") ))
                    .forEachOrdered(h -> {
                        rHdrs.put(h, hdrs.get(h).get(0));
                    });
            
            // Store the search results
            srs = new SearchResults(rHdrs,resp);
        }
        
        return srs;
    }
    
    /**
     * Search response.
     * 
     * @param jsonSearch
     * @return 
     * @throws com.fasterxml.jackson.core.JsonProcessingException 
     */
    public static SResponse searchResponse( String jsonSearch )
            throws IOException
    {   
        return om.readValue( jsonSearch, SResponse.class );
    }
    
    /**
     * Error response.
     * 
     * @param jsonError 
     * @return  
     * @throws java.io.IOException 
     */
    public static EResponse errorResponse( String jsonError )
            throws IOException
    {
        return om.readValue( jsonError, EResponse.class );
    }

    /**
     * Check response for error.
     * 
     * @param resp
     * @return
     * @throws JsonProcessingException 
     */
    public static boolean errorReturned( String resp )
            throws JsonProcessingException
    {
        // Check for error...
        JsonNode rootNode = om.readTree(resp);
        JsonNode retType = rootNode.path("_type");
        
        return retType.asText().equals("ErrorResponse");
    }
    
    /**
     * Dump prettified JSON.
     * 
     * @param json 
     * @throws com.fasterxml.jackson.core.JsonProcessingException 
     */
    public static void dumpJson( String json )
            throws JsonProcessingException
    {
        System.out.println("\r\n--------------------------------------------JSON:");
        System.out.println(om.writeValueAsString(json));
        System.out.println("--------------------------------------------\r\n");
    }
    
    /**
     * Dump search headers.
     * 
     * @param srs 
     */
    public static void dumpSearchHdrs( SearchResults srs )
    {
        System.out.println("Relevant headers:");
        srs.getRelevantHeaders().forEach((k,v) -> {
            System.out.println("  Header: " +k+ ": " +v);
        });
    }
    
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void mainOLD(String[] args)
    {
        String searchQuery = "astrology horoscope";
        
        // Confirm subscription key
        if( subscriptionKey.length() != 32 )
        {
            System.out.println("Invalid key");
            return;
        }
        
        System.out.println("Searching for: " +searchQuery);
        try
        {
            SearchResults srs = SearchWeb(searchQuery);
//            dumpSearchHdrs(srs);
            
            String resp = srs.getJsonResponse();
            if( !errorReturned(resp) )
            {
                System.out.println("JSON list:");
                searchResponse( resp );
                
                // Write to database
                //...TBD
            }
            else
                errorResponse( resp );

//        dumpJson(resp);
        }
        catch( Exception e )
            { e.printStackTrace(); }
    }
    
    /**
     * M A I N (test)
     * ==============
     * 
     * @param args
     * @throws JsonProcessingException 
     */
    public static void main(String[] args)
            throws JsonProcessingException
    {
        String srch = "{\"_type\": \"SearchResponse\", \"queryContext\": {\"originalQuery\": \"astrology horoscope\"}, \"webPages\": {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=astrology+horoscope\", \"totalEstimatedMatches\": 5720000, \"value\": [{\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.0\", \"name\": \"Today's Free Daily Horoscope - Astrology.com\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily.html\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily.html\", \"snippet\": \"Daily Horoscope for all signs. Astrology.com provides over 30 combinations of free daily, weekly, monthly and yearly horoscopes in a variety of interests including love for singles and couples, gay or straight, finance, travel, career, moms, teens, cats and dogs.\", \"deepLinks\": [{\"name\": \"Daily Chinese\", \"url\": \"https:\\/\\/www.astrology.com\\/us\\/horoscope\\/index-chinese.aspx\", \"snippet\": \"Astrology.com provides free chinese horoscopes, online tarot readings, psychic readings, Chinese astrology, Vedic Astrology, Mayan Astrology, Numerology, Feng Shui, zodiac 101, sun sign compatibility and video horoscopes.\"}, {\"name\": \"Cancer\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily\\/cancer.html\", \"snippet\": \"yesterday today: 11.15.20 . Weekly; Monthly; 2020; tomorrow. Nov 15, 2020: Fight any urge to go it alone today. At first glance, it might seem like it would be easier and faster to get things done by yourself, but you have to start reaching out for guidance and insight from other people if you want to get the most possible value out of today's experiences.\"}, {\"name\": \"Sagittarius\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily\\/sagittarius.html\", \"snippet\": \"yesterday today: 11.15.20 . Weekly; Monthly; 2020; tomorrow. Nov 15, 2020: You may have been given some responsibilities that require you to be the voice of authority. This is a change if you're usually be the one making all the wisecracks. While initially challenging, this role is going to finally start feeling more comfortable to you.\"}, {\"name\": \"Aries\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily\\/aries.html\", \"snippet\": \"Read today's Aries Horoscope on Astrology.com. Use this daily horoscope to see what's in store for the sometimes impulsive, but always assertive, Aries sun sign.\"}, {\"name\": \"Capricorn\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily\\/capricorn.html\", \"snippet\": \"Capricorns, don't let life's challenges deter your hard work & determination. Read today's Capricorn Horoscope on Astrology.com & gain useful astrological insight.\"}, {\"name\": \"Leo\", \"url\": \"https:\\/\\/www.astrology.com\\/horoscope\\/daily\\/leo.html\", \"snippet\": \"Read today's Leo Horoscope on Astrology.com. Learn about the challenges & trials that will arise in the life of the ambitious, regal lion of the zodiac.\"}], \"dateLastCrawled\": \"2020-11-15T09:18:00.0000000Z\", \"language\": \"en\", \"isNavigational\": true}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.1\", \"name\": \"Free Horoscopes, Zodiac Signs, Numerology & More ...\", \"url\": \"https:\\/\\/www.horoscope.com\\/us\\/index.aspx\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.horoscope.com\", \"snippet\": \"Find free daily, weekly, monthly and 2021 horoscopes at Horoscope.com, your one stop shop for all things astrological. Find out what the stars have aligned for you today!\", \"dateLastCrawled\": \"2020-11-14T08:06:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.2\", \"name\": \"Horoscopes, Tarot & Love Compatibility | Astrology.com\", \"url\": \"https:\\/\\/www.astrology.com\\/\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.astrology.com\", \"snippet\": \"Astrology.com provides free daily horoscopes, online tarot readings, psychic readings, Chinese astrology, Vedic Astrology, Mayan Astrology, Numerology, Feng Shui, zodiac 101, sun sign compatibility and video horoscopes.\", \"dateLastCrawled\": \"2020-11-15T09:10:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.3\", \"name\": \"Daily Horoscopes 2020 - Today's Astrological Predictions ...\", \"url\": \"https:\\/\\/www.elle.com\\/horoscopes\\/daily\\/\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.elle.com\\/horoscopes\\/daily\", \"snippet\": \"The Astro Twins forecast every zodiac sign's horoscope for today. Find out if the moon's position presents any new opportunities, if todays' the day to take a chance on love, or if you should be ...\", \"dateLastCrawled\": \"2020-11-16T00:36:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.4\", \"name\": \"Horoscope - Free Daily, Weekly & Monthly Foresight\", \"url\": \"https:\\/\\/www.astrology-zodiac-signs.com\\/horoscope\\/\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.astrology-zodiac-signs.com\\/horoscope\", \"snippet\": \"Horoscope Explained. Even though Astrology is much more than your Sun sign’s daily Horoscope, the most common use of Astrology is in the field of predictions for Sun signs based on observation of positions of planets in relation to that sign for a specific day, week, month or a year.In most cases, it is silly to assume that one interpretation can be connected to millions of people around the ...\", \"dateLastCrawled\": \"2020-11-15T11:03:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.5\", \"name\": \"Horoscope - Today's Horoscope, Free Daily Horoscope ...\", \"url\": \"https:\\/\\/www.astroyogi.com\\/horoscopes\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.astroyogi.com\\/horoscopes\", \"snippet\": \"A horoscope is an astrology chart that is well prepared in order to examine the future span of events for a native's life based on the position of the Sunshine, the Moon and other celestial bodies during his or her time of birth. This chart is utilized to analyze how a personal personality will condition up due to astrological affects.\", \"dateLastCrawled\": \"2020-11-15T09:14:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.6\", \"name\": \"Free Birth Chart Calculator, Natal Astrology Horoscope\", \"url\": \"https:\\/\\/horoscopes.astro-seek.com\\/birth-chart-horoscope-online\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/horoscopes.astro-seek.com\\/birth-chart-horoscope-online\", \"snippet\": \"Free Birth Chart Calculator, Natal Astrology Horoscope, Free Astrology Interpretations & Horoscopes, Best Birth Chart Calculator Online, Free Astrology Interpretations, natal chart online calculator - Seek and meet people born on the same date as you. AstroSeek, Free Horoscopes and charts 2020 Astro-Seek.com\", \"dateLastCrawled\": \"2020-11-14T08:17:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.7\", \"name\": \"Horoscopes - Daily wisdom - MSN Lifestyle\", \"url\": \"https:\\/\\/www.msn.com\\/en-us\\/lifestyle\\/horoscope\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.msn.com\\/en-us\\/lifestyle\\/horoscope\", \"snippet\": \"Daily Horoscope : 11\\/15\\/2020 Aries. Mar 21-Apr 19. Taurus. Apr 20-May 20. Gemini. May 21-June 20. Cancer. June 21-July 22. Leo. July 23-Aug 22. Virgo. Aug 23-Sept 22\", \"dateLastCrawled\": \"2020-11-15T09:18:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.8\", \"name\": \"12 Astrology Zodiac Signs Dates, Meanings and Compatibility\", \"url\": \"https:\\/\\/www.astrology-zodiac-signs.com\\/\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.astrology-zodiac-signs.com\", \"snippet\": \"According to a 1999 study, the word horoscope and astrology are the two most searched topics on the Internet. Astrology is considered to be both an art and a science. Astrology is art because interpretation is needed to bring the different aspects together and formulate an idea of the individual's character traits.\", \"dateLastCrawled\": \"2020-11-15T09:15:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}, {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.9\", \"name\": \"Astrology & Horoscope for Windows PC - Free Download\", \"url\": \"https:\\/\\/www.browsercam.com\\/astrology-horoscope-pc\\/\", \"isFamilyFriendly\": true, \"displayUrl\": \"https:\\/\\/www.browsercam.com\\/astrology-horoscope-pc\", \"snippet\": \"Free Download Astrology & Horoscope for PC using this guide at BrowserCam. Even if Astrology & Horoscope undefined is created for Google Android and iOS by webjyotishi.com. you can actually install Astrology & Horoscope on PC for MAC computer. Ever thought how you can download Astrology & Horoscope PC? No worries, we are going to break it down for everyone into straightforward steps.<\\/p>\", \"dateLastCrawled\": \"2020-11-15T10:02:00.0000000Z\", \"language\": \"en\", \"isNavigational\": false}]}, \"entities\": {\"value\": [{\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#Entities.0\", \"contractualRules\": [{\"_type\": \"ContractualRules\\/LicenseAttribution\", \"targetPropertyName\": \"description\", \"mustBeCloseToContent\": true, \"license\": {\"name\": \"CC-BY-SA\", \"url\": \"http:\\/\\/creativecommons.org\\/licenses\\/by-sa\\/3.0\\/\"}, \"licenseNotice\": \"Teksts nodrošināts saskaņā ar CC-BY-SA licenci.\"}, {\"_type\": \"ContractualRules\\/LinkAttribution\", \"targetPropertyName\": \"description\", \"mustBeCloseToContent\": true, \"text\": \"Wikipedia\", \"url\": \"http:\\/\\/en.wikipedia.org\\/wiki\\/Astrology\"}, {\"_type\": \"ContractualRules\\/MediaAttribution\", \"targetPropertyName\": \"image\", \"mustBeCloseToContent\": true, \"url\": \"http:\\/\\/en.wikipedia.org\\/wiki\\/Astrology\"}], \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/entityexplore?q=Astrology&filters=sid:%224f133e90-81c0-2614-dbda-5651028a715f%22&elv=AXXfrEiqqD9r3GuelwApulqVGgAaeL4fgSwxzmr4eGv1OlTEhclda9Y0m!tFUhC*BMAukQ!BotHjjRRVM7am7gs0duLXUz!fO2EL39AFys*O\", \"name\": \"Astrology\", \"image\": {\"name\": \"Astrology\", \"thumbnailUrl\": \"https:\\/\\/www.bing.com\\/th?id=AMMS_5de66408244b269e8d3548900c15f578&w=110&h=110&c=7&rs=1&qlt=80&cdv=1&pid=16.1\", \"provider\": [{\"_type\": \"Organization\", \"url\": \"http:\\/\\/en.wikipedia.org\\/wiki\\/Astrology\"}], \"hostPageUrl\": \"http:\\/\\/upload.wikimedia.org\\/wikipedia\\/commons\\/1\\/12\\/Venice_ast_sm.jpg\", \"width\": 110, \"height\": 110, \"sourceWidth\": 350, \"sourceHeight\": 350}, \"description\": \"Astrology is a pseudoscience that claims to divine information about human affairs and terrestrial events by studying the movements and relative positions of celestial objects. Astrology has been dated to at least the 2nd millennium BCE, and has its roots in calendrical systems used to predict seasonal shifts and to interpret celestial cycles as signs of divine communications. Many cultures have attached importance to astronomical events, and some—such as the Hindus, Chinese, and the Maya—developed elaborate systems for predicting terrestrial events from celestial observations. Western astrology, one of the oldest astrological systems still in use, can trace its roots to 19th–17th century BCE Mesopotamia, from where it spread to Ancient Greece, Rome, the Arab world and eventually Central and Western Europe. Contemporary Western astrology is often associated with systems of horoscopes that purport to explain aspects of a person's personality and predict significant events in their lives based on the positions of celestial objects; the majority of professional astrologers rely on such systems.\", \"entityPresentationInfo\": {\"entityScenario\": \"DominantEntity\", \"entityTypeHints\": [\"Generic\"]}, \"bingId\": \"4f133e90-81c0-2614-dbda-5651028a715f\"}]}, \"relatedSearches\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#RelatedSearches\", \"value\": [{\"text\": \"msn horoscopes\", \"displayText\": \"msn horoscopes\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=msn+horoscopes\"}, {\"text\": \"daily horoscope free horoscope\", \"displayText\": \"daily horoscope free horoscope\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=daily+horoscope+free+horoscope\"}, {\"text\": \"susan miller's astrology zone monthly\", \"displayText\": \"susan miller's astrology zone monthly\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=susan+miller%27s+astrology+zone+monthly\"}, {\"text\": \"best free horoscope readings\", \"displayText\": \"best free horoscope readings\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=best+free+horoscope+readings\"}, {\"text\": \"jessica adams horoscopes\", \"displayText\": \"jessica adams horoscopes\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=jessica+adams+horoscopes\"}, {\"text\": \"zodiac astrology horoscope\", \"displayText\": \"zodiac astrology horoscope\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=zodiac+astrology+horoscope\"}, {\"text\": \"free horoscope reading for today\", \"displayText\": \"free horoscope reading for today\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=free+horoscope+reading+for+today\"}, {\"text\": \"free daily astrology numerology horoscope\", \"displayText\": \"free daily astrology numerology horoscope\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/search?q=free+daily+astrology+numerology+horoscope\"}]}, \"videos\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#Videos\", \"readLink\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/videos\\/search?q=astrology+horoscope\", \"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology+horoscope\", \"isFamilyFriendly\": true, \"value\": [{\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=22DC1592C8557291B29122DC1592C8557291B291\", \"name\": \"Aries Weekly Astrology Horoscope 24th February 2020\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OVP.gIygY7UldeTcKMRuNWU7wwHgFo&pid=Api\", \"datePublished\": \"2020-02-22T08:00:00.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=SONJ0RtOVX0\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=SONJ0RtOVX0\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=SONJ0RtOVX0\", \"width\": 1280, \"height\": 720, \"duration\": \"PT4M40S\", \"motionThumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OM.kbKRclXIkhXcIg_1603055235&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/SONJ0RtOVX0?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 7637, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=3F22F7F999270C9262563F22F7F999270C926256\", \"name\": \"Gemini Monthly Astrology Horoscope March 2020\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse4.mm.bing.net\\/th?id=OVP.jMLnQ_cqbqSd1apMvW4YfgHgFo&pid=Api\", \"datePublished\": \"2020-03-01T08:00:00.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=rKGQQwT_Quw\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=rKGQQwT_Quw\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=rKGQQwT_Quw\", \"width\": 1280, \"height\": 720, \"duration\": \"PT9M15S\", \"motionThumbnailUrl\": \"https:\\/\\/tse4.mm.bing.net\\/th?id=OM.VmKSDCeZ-fciPw_1600822186&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/rKGQQwT_Quw?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 6356, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=F0F87CA98FE88B9FF14DF0F87CA98FE88B9FF14D\", \"name\": \"Scorpio Weekly Astrology Horoscope 24th February 2020\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse2.mm.bing.net\\/th?id=OVP.xo3lpjlY9BC5_15ofjH5aAHgFo&pid=Api\", \"datePublished\": \"2020-02-22T21:22:36.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=JVEXmB-BeF8\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=JVEXmB-BeF8\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=JVEXmB-BeF8\", \"width\": 1280, \"height\": 720, \"duration\": \"PT4M18S\", \"motionThumbnailUrl\": \"https:\\/\\/tse2.mm.bing.net\\/th?id=OM.TfGfi-iPqXz48A_1601681206&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/JVEXmB-BeF8?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 6591, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=ED77584CF6B8F6494876ED77584CF6B8F6494876\", \"name\": \"Libra Astrology Horoscope 11th March 2019\", \"description\": \"SIGN UP! https:\\/\\/horoscope.co.uk\\/welcome For your free astrology reports until June 2019) Weekly Horoscopes with Michele Knight. Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology ...\", \"thumbnailUrl\": \"https:\\/\\/tse4.mm.bing.net\\/th?id=OVP.gtkbPyN_YLCJY3u9TCIDSgEsDh&pid=Api\", \"datePublished\": \"2019-03-10T16:18:55.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=AVRTGAW7XP8\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=AVRTGAW7XP8\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=AVRTGAW7XP8\", \"width\": 1280, \"height\": 720, \"duration\": \"PT4M31S\", \"motionThumbnailUrl\": \"https:\\/\\/tse4.mm.bing.net\\/th?id=OM2.dkhJ9rj2TFh37Q_1590947753&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/AVRTGAW7XP8?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 6891, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=2B7B5236D2D9B0432FDD2B7B5236D2D9B0432FDD\", \"name\": \"Scorpio Weekly Astrology Horoscope 18th November 2019\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OVP.9Y6ldqV1QieCpja2SP90LAHgFo&pid=Api\", \"datePublished\": \"2019-11-15T08:00:00.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=u-G0f83RB78\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=u-G0f83RB78\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=u-G0f83RB78\", \"width\": 1280, \"height\": 720, \"duration\": \"PT5M19S\", \"motionThumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OM2.3S9DsNnSNlJ7Kw_1604450988&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/u-G0f83RB78?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 10922, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=CA569BA7BFD9A769632DCA569BA7BFD9A769632D\", \"name\": \"Taurus Monthly Astrology Horoscope December 2019\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OVP.wcWqieRPJt22ji_cKXYUBwHgFo&pid=Api\", \"datePublished\": \"2019-11-27T21:56:28.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=ktY6h94B-q8\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=ktY6h94B-q8\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=ktY6h94B-q8\", \"width\": 1280, \"height\": 720, \"duration\": \"PT7M47S\", \"motionThumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OM1.LWNpp9m_p5tWyg_1605044242&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/ktY6h94B-q8?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 9950, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=3A464D6CCDD8B19A1B333A464D6CCDD8B19A1B33\", \"name\": \"Sagittarius Weekly Astrology Horoscope November 25th 2019\", \"description\": \"Weekly Horoscopes with Michele Knight. https:\\/\\/www.facebook.com\\/micheleknightastrologer Come visit me on instagram for daily updates https:\\/\\/www.instagram.com\\/micheleknight\\/ http:\\/\\/www.micheleknight.com http:\\/\\/www.horoscope.co.uk https:\\/\\/www.facebook.com\\/micheleknightastrologer @micheleknight #astrology #horoscope\", \"thumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OVP.M8SDGdTAQnTxEAW6KhS4ZwHgFo&pid=Api\", \"datePublished\": \"2019-11-23T08:00:00.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=MqZxMQe7gfM\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=MqZxMQe7gfM\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=MqZxMQe7gfM\", \"width\": 1280, \"height\": 720, \"duration\": \"PT4M56S\", \"motionThumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OM1.MxuasdjNbE1GOg_1603237408&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/MqZxMQe7gfM?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 11037, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=7BD821CD3155EFE2823D7BD821CD3155EFE2823D\", \"name\": \"Aries December 2019 Horoscope #Aries #Astrology #Horoscope\", \"description\": \"#AriesDecember2019 #AriesDecemberAstrology2019 #AriesDecemberHoroscope2019 Get your NEED-TO-KNOW’s for ARIES and General DECEMBER 2019 Transits in this video. Watch this if ARIES is your Sun, Sign, Rising Sign, or Moon Sign Receive SHINE! ~ My 28-day Virtual Coaching Program for Free and detailed monthly astrology reports one month early ...\", \"thumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OVP.yXBhOsfY3psh1eI_MQscPgEsDh&pid=Api\", \"datePublished\": \"2019-10-26T09:09:27.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=6AveplYJC7Q\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=6AveplYJC7Q\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=6AveplYJC7Q\", \"width\": 1280, \"height\": 720, \"duration\": \"PT27M17S\", \"motionThumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OM.PYLi71UxzSHYew_1599205834&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/6AveplYJC7Q?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 8246, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=C87C58BA29D67AB9E3EBC87C58BA29D67AB9E3EB\", \"name\": \"Aries August Horoscope 2020\", \"description\": \"30% Off 12 Month Personal Horoscope Forecast & Character Analysis Combination Please CLICK HERE... https:\\/\\/store.patrickarundell.com\\/product\\/12-month-personal-horoscope-forecast-and-character-analysis-special-offer\\/ SPEAK TO ME 1 to 1 Live Please CLICK HERE Book 1 to 1 Consultation with Patrick… If you would like to make a Contribution to ...\", \"thumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OVP.rjSL3RgVXvDuDmFJwJCNewEsDh&pid=Api\", \"datePublished\": \"2020-07-07T14:37:30.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=CnCpUVPmrto\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=CnCpUVPmrto\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=CnCpUVPmrto\", \"width\": 1280, \"height\": 720, \"duration\": \"PT10M31S\", \"motionThumbnailUrl\": \"https:\\/\\/tse1.mm.bing.net\\/th?id=OM.6-O5etYpulh8yA_1605466651&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/CnCpUVPmrto?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 7095, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}, {\"webSearchUrl\": \"https:\\/\\/www.bing.com\\/videos\\/search?q=astrology%20horoscope&view=detail&mid=58EFAF2C0620F58BD91858EFAF2C0620F58BD918\", \"name\": \"Capricorn December 2019 Horoscope #CAPRICORN #Astrology #Horoscope\", \"description\": \"#CapricornDecember2019 #CapricornDecemberAstrology2019 #CapricornDecemberHoroscope2019 Get your NEED-TO-KNOWb\", \"thumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OVP.mrViGe5f-dogyN6x26qK_gEsDh&pid=Api\", \"datePublished\": \"2019-10-25T08:00:00.0000000\", \"publisher\": [{\"name\": \"YouTube\"}], \"isAccessibleForFree\": true, \"contentUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=OSNsIGZBkkk\", \"hostPageUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=OSNsIGZBkkk\", \"encodingFormat\": \"mp4\", \"hostPageDisplayUrl\": \"https:\\/\\/www.youtube.com\\/watch?v=OSNsIGZBkkk\", \"width\": 1280, \"height\": 720, \"duration\": \"PT22M32S\", \"motionThumbnailUrl\": \"https:\\/\\/tse3.mm.bing.net\\/th?id=OM.GNmL9SAGLK_vWA_1604114450&pid=Api\", \"embedHtml\": \"<iframe width=\\\"1280\\\" height=\\\"720\\\" src=\\\"http:\\/\\/www.youtube.com\\/embed\\/OSNsIGZBkkk?autoplay=1\\\" frameborder=\\\"0\\\" allowfullscreen><\\/iframe>\", \"allowHttpsEmbed\": true, \"viewCount\": 8355, \"thumbnail\": {\"width\": 160, \"height\": 120}, \"allowMobileEmbed\": true, \"isSuperfresh\": false}], \"scenario\": \"List\"}, \"rankingResponse\": {\"mainline\": {\"items\": [{\"answerType\": \"WebPages\", \"resultIndex\": 0, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.0\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 1, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.1\"}}, {\"answerType\": \"Videos\", \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#Videos\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 2, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.2\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 3, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.3\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 4, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.4\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 5, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.5\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 6, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.6\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 7, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.7\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 8, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.8\"}}, {\"answerType\": \"WebPages\", \"resultIndex\": 9, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#WebPages.9\"}}, {\"answerType\": \"RelatedSearches\", \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#RelatedSearches\"}}]}, \"sidebar\": {\"items\": [{\"answerType\": \"Entities\", \"resultIndex\": 0, \"value\": {\"id\": \"https:\\/\\/api.cognitive.microsoft.com\\/api\\/v7\\/#Entities.0\"}}]}}}";

        SResponse sr = om.readValue( srch, SResponse.class );
        System.out.println("--> " +sr.getRespType());
    }
}

//------------------------------------------------------------------------------
class EResponse
{
    @JsonProperty("_type")
    private String              errType;
    private List<SError>        errors;

    public String getErrType() { return errType; }
    public List<SError> getErrors() { return errors; }
}

class SError
{
    private String              error;
    private String              message;
    private String              moreDetails;
    private String              parameter;
    private String              subCode;
    private String              value;

    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getMoreDetails() { return moreDetails; }
    public String getParameter() { return parameter; }
    public String getSubCode() { return subCode; }
    public String getValue() { return value; }
}


class SResponse
{
    @JsonProperty("_type")
    private String              respType;
    private QueryContext        queryContext;
    private WebPages            webPages;
    private Entities            entities;
    private RelatedSearches     relatedSearches;
    private Videos              videos;
    private RankingResponse     rankingResponse;

    public String getRespType() { return respType; }
    public QueryContext getQueryContext() { return queryContext; }
    public WebPages getWebPages() { return webPages; }
    public Entities getEntities() { return entities; }
    public RelatedSearches getRelatedSearches() { return relatedSearches; }
    public Videos getVideos() { return videos; }
    public RankingResponse getRankingResponse() { return rankingResponse; }
}

class QueryContext
{
    private String              originalQuery;

    public String getOriginalQuery() { return originalQuery; }
}

class WebPages
{
    private String              webSearchUrl;
    private int                 totalEstimatedMatches;
    @JsonProperty("value")
    private List<WebValue>      values;

    public String getWebSearchUrl() { return webSearchUrl; }
    public int getTotalEstimatedMatches() { return totalEstimatedMatches; }
    public List<WebValue> getValues() { return values; }
}

class WebValue
{
    private String              id;
    private String              name;
    private String              url;
    private boolean             isFamilyFriendly;
    private String              displayUrl;
    private String              snippet;
    private List<DeepLink>      deepLinks;
    private String              dateLastCrawled;
    private String              language;
    private boolean             isNavigational;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public boolean isIsFamilyFriendly() { return isFamilyFriendly; }
    public String getDisplayUrl() { return displayUrl; }
    public String getSnippet() { return snippet; }
    public List<DeepLink> getDeepLinks() { return deepLinks; }
    public ZonedDateTime getDateLastCrawled() { return ZonedDateTime.parse(dateLastCrawled); }
    public String getLanguage() { return language; }
    public boolean isIsNavigational() { return isNavigational; }
}

class DeepLink
{
    private String              name;
    private String              url;
    private String              snippet;

    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getSnippet() { return snippet; }
}

class Entities
{
    @JsonProperty("value")
    private List<EntityValue>   values;

    public List<EntityValue> getValues() { return values; }
}

class EntityValue
{
    private String                  id;
    private List<ContractualRules>  contractualRules;
    private String                  webSearchUrl;
    private String                  name;
    private SImage                  image;
    private String                  description;
    private EntityPresentationInfo  entityPresentationInfo;
    private String                  bingId;

    public String getId() { return id; }
    public List<ContractualRules> getContractualRules() { return contractualRules; }
    public String getWebSearchUrl() { return webSearchUrl; }
    public String getName() { return name; }
    public SImage getImage() { return image; }
    public String getDescription() { return description; }
    public EntityPresentationInfo getEntityPresentationInfo() { return entityPresentationInfo; }
    public String getBingId() { return bingId; }
}

class ContractualRules
{
    @JsonProperty("_type")
    private String              type;
    private String              targetPropertyName;
    private boolean             mustBeCloseToContent;
    private LicenseAttribution  license;
    private String              licenseNotice;
    private String              text;
    private String              url;

    public String getType() { return type; }
    public String getTargetPropertyName() { return targetPropertyName; }
    public boolean isMustBeCloseToContent() { return mustBeCloseToContent; }
    public LicenseAttribution getLicense() { return license; }
    public String getLicenseNotice() { return licenseNotice; }
    public String getText() { return text; }
    public String getUrl() { return url; }
}

class LicenseAttribution
{
    private String              name;
    private String              url;

    public String getName() { return name; }
    public String getUrl() { return url; }
}

class SImage
{
    private String              name;
    private String              thumbnailUrl;
    private List<Provider>      provider;
    private String              hostPageUrl;
    private int                 width, height, sourceWidth, sourceHeight;

    public String getName() { return name; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public List<Provider> getProvider() { return provider; }
    public String getHostPageUrl() { return hostPageUrl; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSourceWidth() { return sourceWidth; }
    public int getSourceHeight() { return sourceHeight; }
}

class Provider
{
    @JsonProperty("_type")
    private String              providerType;
    private String              url;

    public String getProviderType() { return providerType; }
    public String getUrl() { return url; }
}

class EntityPresentationInfo
{
    private String              entityScenario;
    private String[]            entityTypeHints;

    public String getentityScenario() { return entityScenario; }
    public String[] getEntityTypeHints() { return entityTypeHints; }
}

class RelatedSearches
{
    private String          id;
    @JsonProperty("value")
    private List<RSValue>   values;

    public String getId() { return id; }
    public List<RSValue> getValues() { return values; }
}

class RSValue
{
    private String          text;
    private String          displayText;
    private String          webSearchUrl;

    public String getText() { return text; }
    public String getDisplayText() { return displayText; }
    public String getWebSearchUrl() { return webSearchUrl; }
}

class Videos
{
    private String          id;
    private String          readLink;
    private String          webSearchUrl;
    private boolean         isFamilyFriendly;
    @JsonProperty("value")
    private List<VValues>   values;
    private String          scenario;

    public String getId() { return id; }
    public String getReadLink() { return readLink; }
    public String getWebSearchUrl() { return webSearchUrl; }
    public boolean isIsFamilyFriendly() { return isFamilyFriendly; }
    public List<VValues> getValues() { return values; }
    public String getScenario() { return scenario; }
}

class VValues
{
    private String          webSearchUrl;
    private String          name;
    private String          description;
    private String          thumbnailUrl;
    private String          datePublished;
    private List<Publisher> publisher;
    private boolean         isAccessibleForFree;
    private String          contentUrl;
    private String          hostPageUrl;
    private String          encodingFormat;
    private String          hostPageDisplayUrl;
    private int             width, height;
    private String          duration;
    private String          motionThumbnailUrl;
    private String          embedHtml;
    private boolean         allowHttpsEmbed;
    private int             viewCount;
    private Thumbnail       thumbnail;
    private boolean         allowMobileEmbed;
    private boolean         isSuperfresh;

    public String getWebSearchUrl() { return webSearchUrl; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public ZonedDateTime getDatePublished() { return ZonedDateTime.parse(datePublished); }
    public List<Publisher> getPublisher() { return publisher; }
    public boolean isIsAccessibleForFree() { return isAccessibleForFree; }
    public String getContentUrl() { return contentUrl; }
    public String getHostPageUrl() { return hostPageUrl; }
    public String getEncodingFormat() { return encodingFormat; }
    public String getHostPageDisplayUrl() { return hostPageDisplayUrl; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getDuration() { return duration; }
    public String getMotionThumbnailUrl() { return motionThumbnailUrl; }
    public String getEmbedHtml() { return embedHtml; }
    public boolean isAllowHttpsEmbed() { return allowHttpsEmbed; }
    public int getViewCount() { return viewCount; }
    public Thumbnail getThumbnail() { return thumbnail; }
    public boolean isAllowMobileEmbed() { return allowMobileEmbed; }
    public boolean isIsSuperfresh() { return isSuperfresh; }
}

class Publisher
{
    private String          name;

    public String getName() { return name; }
}

class Thumbnail
{
    private int             width, height;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

class RankingResponse
{
    private Mainline        mainline;
    private Sidebar         sidebar;

    public Mainline getMainline() { return mainline; }
    public Sidebar getSidebar() { return sidebar; }
}

class Mainline
{
    private List<MLItem>    items;

    public List<MLItem> getItems() { return items; }
}

class MLItem
{
    private String          answerType;
    private int             resultIndex;
    private MLValue         value;

    public String getAnswerType() { return answerType; }
    public int getResultIndex() { return resultIndex; }
    public MLValue getValue() { return value; }
}

class MLValue
{
    private String          id;

    public String getId() { return id; }
}

class Sidebar
{
    private List<SBItem>    items;

    public List<SBItem> getItems() { return items; }
}

class SBItem
{
    private String          answerType;
    private int             resultIndex;
    private SBValue         value;

    public String getAnswerType() { return answerType; }
    public int getResultIndex() { return resultIndex; }
    public SBValue getValue() { return value; }
}

class SBValue
{
    private String          id;

    public String getId() { return id; }
}