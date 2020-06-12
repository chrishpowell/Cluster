/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.sentences.Token;

import java.util.Arrays;
import java.util.List;


/**
 * Test sentences.  NOTA BENE!! Need at least two sentences as otherwise similarity
 * method will fail.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Corpi
{
    // Test data
    private static final List<Token> lts = List.of(new Token("quick"),new Token("brown"),new Token("fox"),new Token("jump"),new Token("lazy"),new Token("dog"));
    private static final List<Token> lts2 = List.of(new Token("every"),new Token("good"),new Token("boy"),new Token("deserve"),new Token("fruit"),new Token("vegetable"),new Token("console"),new Token("time"));

    
    private static final String NS = "eu.discoveri.predikt";
    
    private static String tDoc = "Advertisement not logged in Login Shopping Cart de dk en es fr it nl pt py 中文 日本 Deutsch Dansk English Español Français Italiano Nederlands Português Русский 中文 日本语 HomeHome Free HoroscopesHoroscopes Free Horoscopes Free Horoscopes - all at a glance New: The Seven Sins, by Liz Greene Daily Horoscope Personal Daily Horoscope Love Horoscope Celestial Events Short Horoscopes Personal Portrait Short Report Partner Love, Flirtation & Sex Short Report Forecast Money and Success Horoscope Children & Young People Stories from the Stars Color Horoscope Color Oracle Horoscopes by Liz Greene New: The Seven Sins Psychological Horoscope Career & Vocation Relationship Horoscope The Child`s Horoscope Yearly Horoscope Analysis Astrology for Lovers Interactive Horoscopes AstroClick Portrait AstroClick Travel AstroClick Local Space AstroClick Love AstroClick Partner Sun Sign Horoscopes Weekly Horoscope Astrology for Lovers Drawings & Calculations Natal Chart, Ascendant Extended Chart Selection Create an Ephemeris Astrology Atlas Query ADB Search Tool Current Planets Your Astro-Twins NEW IN THE ASTRO SHOP: PERSONAL ECLIPSE REPORT Astro ShopShop Astro Shop Astro Shop - Overview AstroIntelligence Reports New Horoscope: The Seven Sins Psychological Horoscope Analysis Career and Vocation Stories from the Stars The Child's Horoscope Relationship Horoscope Horoscope for Two New Horoscope: Personal Eclipse Report Yearly Horoscope Analysis Long-Term Perspectives Transits of the Year Personal Horoscope Calendar Money and Success Horoscope AstroText Horoscopes Personal Portrait Youth Horoscope Partner Horoscope Forecast Horoscope Love Horoscope Color Horoscope Online Services Extended Daily Horoscope PDF Chart Drawings Extended Data Storage Additional Information Report Samples Book or Screen? A Pleasure for your Eyes What Customers say The ideal present ... Special Offer Fundraising Campaign Kantha Bopha Terms of trade Charts Drawings PDF Chart Drawings Printed chart drawings Software Swiss Ephemeris Shopping Cart View Shopping Cart NEW: PERSONAL ECLIPSE REPORT All about AstrologyAstrology All about Astrology All about Astrology - Overview Introductions A Brief Introduction to Astrology First Steps in Astrology Understanding Astrology The Astrological Journal The Mountain Astrologer Advanced Astrology Articles Articles by Liz Greene Articles by Robert Hand Articles by Dana Gerhardt Community Forum Astro Wiki Astro-Databank ADB Search Tool Astrodienst Newsletter Education Study Astrology CPA London The Sophia Centre Astrologos Faculty of Astrological Studies Mercury Internet School MISPA Ephemeris Ephemeris 2020 Ephemeris 2021 9000 Years Ephemeris Swiss Ephemeris Atlas Direct Atlas Query ContactContact Contact Contact Phone, Email, Address About Astrodienst People at Astrodienst Authors at Astrodienst Freelancers About Astrodienst Jobs at Astrodienst Tools, Links, Infos Webmaster Tools Astrodienst Links FAQ Terms and Conditions Terms of Trade Privacy Policy Terms of Use Current Planets 7-Jun-2020, 13:00 UT/GMT Sun 17 13'59\" 22n49 Moon 9 56'36\" 24s03 Mercury 10 31'59\" 24n06 Venus 11 14'14\"r 21n43 Mars 17 0'33\" 7s28 Jupiter 26 21' 4\"r 21s06 Saturn 1 21'58\"r 20s01 Uranus 8 52'56\" 14n02 Neptune 20 53'37\" 4s35 Pluto 24 34'56\"r 22s07 TrueNode 29 5'18\" 23n26 Chiron 8 56'13\" 6n03 Explanations of the symbols Chart of the moment | My Astro | Forum | FAQ Accept Astrodienst Privacy Policy: This website uses cookies to make it easier for you to use our services. For those visitors who do not have paid advertisement-free access, cookies may also be set by advertisers. Please see our privacy policy for more information on this and how you can refuse the use of cookies by advertisers. About the use of your data: The data you enter on this website will be used exclusively for the function of the website and will not be passed on to third parties. You can delete all data in \"My Astro\" at any time. By using our services you agree to the privacy policy.. Select your language: DeutschDanskEnglishEspañolFrançaisItalianoNederlandsPortuguêsРусский中文日本语 Advertisement share tweet e-mail The World's Best Horoscopes Personal Daily Horoscope Your individual topics for each day, with the Love Horoscope and the Celestial Events. All Free Horoscopes at a Glance A large selection of free horoscopes on the topics personality, forecasting, relationships, love and more. The Weekly Horoscope The weekly horoscope for each sun sign, new for you every week. Best Horoscopes - Astro Shop The Astrodienst horoscope interpretations are considered the best (computer-generated) horoscopes world-wide. Astrology at its highest level. Understanding Astrology Here you can find articles for beginners, students and professional astrologer, about the basics, the psychological background or philosphical perceptions. Quality of Time Personal Daily Horoscope Your individual topics for each day, by Robert Hand (with the Love Horoscope and the Celestial Events) Yearly Horoscope Analysis The challenging annual preview by Liz Greene, free Try-Out Edition Short Report Forecast Selected topics of the next six months, in the transit horoscope of Robert Hand The Weekly Horoscope The Astrodienst Sun-sign horoscope: Love, profession and everyday topics, every week anew! Love and Relationship Relationship Horoscope The outstanding Relationship Horoscope by Liz Greene, in a free Try-Out Edition. Partner Horoscope How do you match? A short horoscope by Robert Hand Astro Click Partner Check the potential of your relationship with the interactive partner horoscope! Love, Flirtation, and Sex The refreshing love horoscope in the style of the 70s, by John Townley Personality Psychological Horoscope A fascinating analysis of the personality by Liz Greene, free Try-Out Edition. Stories from the Stars Your personal relationship to the starry heavens! The new Fixed Star Report, in a free Try-Out Edition. Money and Success The horoscope about your potential for success, and how you can use it to create personal happiness - Free try-out edition. Personal Portrait Selected aspects of your birth horoscope, by Robert Pelletier. Astro.com at a Glance Free Daily Horoscope: Created especially for you Quality: We offer horoscope interpretations of prime quality All our free Horoscopes: Enjoy your expedition! Knowledge: Dive into the depths of \"Understanding Astrology\" Astro.com Forum: Visit the great Astrodienst Discussion Forum Most read Personal Daily Horoscope Free! Your individual topics for every day! Psychological Horoscope Free Try-Out Edition The Psychological Horoscope Analysis by Liz Greene combines astrological insight with modern psychology. Check it out! The Weekly Horoscope From the Astro Shop NEW! Balancing Light and Darkness Personal Eclipse Report By Bernadette Brady More Information The NEW Horoscope by Liz Greene The Seven Sins The Journey from Shadow into Light Read more... Free Try-Out Edition Learn more about your true personality! Psychological Horoscope Analysis by Liz Greene Free Try-Out Edition More Information Listen to your Child! The Child's Horoscope by Liz Greene Free Try-Out Edition More Information What will the next year bring? Transits of the Year by Robert Hand More Information Advertisement THE ASTROLOGICAL ASSOCIATION'S 52ND ANNUAL CONFERENCE 25.05.2020 Serving the Future A full three-day conference, online 26 - 28 June 2020. Book before 31 May for a heavily discounted rate of just £239. A rich and diverse programme, with 45 great international speakers! Access to all conference webinars for 21 days. Read more on www.astrologicalassociation.com The ASTROLOGICAL JOURNAL 02.06.2020 Frank C. Clifford: A Practical Guide to Writers of Astrology In this article I'll be discussing the various ways in which astrologers can get their work published and offering advice about approaching magazines and publishers. I'll also provide some tips on how to start the writing process and to discover your niche as a writer. There are plenty of websites and books dedicated to writing and getting published. But I'd like to focus on what I've learned as a writer and publisher in the area of astrology. The CAREER ASTROLOGER 19.05.2020 Chet Zdrowski: Dane Rudhyar - Astrology for the Modern Psyche Dane Rudhyar is a towering figure in modern astrology. Long after his passing in 1985 he continues to have a pervasive influence on much of modern astrology, especially evolutionary and psychological forms. Many of his later works are relatively unknown and there are many treasures for coming generations of astrologers to discover in them. Here we'll be taking a look at his first astrology book, one of the craft's most ground-breaking and enduring classics, The Astrology of Personality. IVC CONSTELLATION NEWS 06.05.2020 Grazia Mirti: Lilith - Black Moon: An Ally in the Astrological Interpretation In this article I shall discuss the important role played by Lilith-Dark Moon in our astrological analyses. I have researched the effects of Lilith-Dark Moon for many years; I now regularly include the study of Lilith-Dark Moon in my daily astrological practice. In my view, it is important to consider Lilith-Dark Moon’s role in a wider sense: not only as a symbol of feminine empowerment and transformation, but also as a useful indicator of our unfulfilled wishes, lacks and areas of our lives which need to be more carefully addressed. The MOUNTAIN ASTROLOGER 20.04.2020 Ray Grasse: Saturn as the Key 'Karmic' Challenge of the Horoscope More years ago than I care to remember, I heard a visiting astrologer make the offhand comment that Saturn's placement in the horoscope indicates where one's \"key karmic challenge in this lifetime is.\" Having thought about that astrologer's comment quite a bit since first hearing it, I've come to believe there is indeed something unique about Saturn as representing an especially focused symbol of one's current life-challenges. Understanding Astrology 04.03.2020 Ray Grasse: StarGates: Planetary Portals and Windows in Time Besides making two groundbreaking scientific discoveries, the scientist Joseph Priestley published major papers on electricity, invented new apparatuses for the creation of electrical charge, isolated and named ten distinct gases, and wrote more than fifty books and pamphlets on politics, education, and faith. What explains why certain historical periods are times when new ideas burst onto the scene in profusion? What is Astrology? Liz Greene: First steps in Astrology A Brief Introduction to Astrology All Reports at a Glance Personality Relationship Quality of Time Love Life Profession and Career Children and Young People Chart of the moment Liz Greene's Astrology for Lovers I am Aries Taurus Gemini Cancer Leo Virgo Libra Scorpio Sagittarius Capricorn Aquarius Pisces female male He is / She is ??? Aries Taurus Gemini Cancer Leo Virgo Libra Scorpio Sagittarius Capricorn Aquarius Pisces female male Recent Articles A Practical Guide for Writers of Astrology, by Frank C. Clifford Chet Zdrowski: Dane Rudhyar - Astrology for the Modern Psyche Lilith - Black Moon: An Ally in Astrological Interpreation, by Grazia Mirti Current Planets 7-Jun-2020, 13:00 UT/GMT Sun 17 13'59\" 22n49 Moon 9 56'36\" 24s03 Mercury 10 31'59\" 24n06 Venus 11 14'14\"r 21n43 Mars 17 0'33\" 7s28 Jupiter 26 21' 4\"r 21s06 Saturn 1 21'58\"r 20s01 Uranus 8 52'56\" 14n02 Neptune 20 53'37\" 4s35 Pluto 24 34'56\"r 22s07 TrueNode 29 5'18\" 23n26 Chiron 8 56'13\" 6n03 Explanations of the symbols Chart of the moment Astrodienst Newsletter Extended Chart Selection Astro-Databank Astro Wiki Understanding Astrology 9000 Years Ephemeris Swiss Ephemeris Astro-Databank Birthdays & News Birthday on June 7th Bakley, Bonny Lee Pamuk, Orhan Farquhar, Shawn Prince (musician) Kournikova, Anna Joachim, Prince of Denmark Gauguin, Paul Martin, Dean Koch, Bill Entremont, Philippe more&gt; New in Astrodatabank JUL (French rapper) Ascari, Janice Ferreira, Salette Woods, Lindsay McCann, Madeleine Garcin, Gilbert Feltrin, Rogério Rabin, Fábio Calabresa, Dani Dejesus, Pathy more&gt; Advertisement Copyright © 2020 Astrodienst AG - Privacy Policy - report a problem Leave mobile view As one of the largest astrology portals WWW.ASTRO.COM offers a lot of free features on the subject. With high-quality horoscope interpretations by the world's leading astrologers Liz Greene, Robert Hand and other authors, many free horoscopes and extensive information on astrology for beginners and professionals, www.astro.com is the first address for astrology on the web. Homepage - Free Horoscopes - Astro Shop - Astrology Knowledge - Ephemeris - Authors and Staff - My Astro - Direct Atlas query - FAQ - Forum - Contact - Privacy Policy - Terms of use - Sitemap";
    
    
    // Test sentences
    private static final List<SentenceNode> sents9 = Arrays.asList(
        new SentenceNode("S1","American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
        new SentenceNode("S2","American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
        new SentenceNode("S3","With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall"),
        new SentenceNode("S4","The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television"),
        new SentenceNode("S5","If Utahans have their way, Philo T. Farnsworth will become a household name"),
        new SentenceNode("S6","The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue."),
        new SentenceNode("T0","Confederate statues litter the squares of Southern states."),
        new SentenceNode("T1","The quick brown fox jumps over the lazy dog"),
        new SentenceNode("T2","The quick brown hound jumps over the lazy fox."),
        new SentenceNode("C0","Tropical climate is one of the five major climate groups in the Köppen climate classification of heat."),
        new SentenceNode("C1","Tropical climates are broadly located within 20 to 25 degrees of the equator and characterized by monthly average temperatures of 18 ℃ (64.4 ℉), or higher year-round, often following a seasonal rhythm and where annual precipitation is generally abundant and sunlight is intense."),
        new SentenceNode("C2","Whew, it's hot!")
    );
    
    private static final List<SentenceNode> sents = Arrays.asList(
        new SentenceNode("s1","A Pleasure for your Eyes What Customers say The ideal present ... Special Offer Fundraising Campaign Kantha Bopha Terms of trade Charts Drawings PDF Chart Drawings Printed chart drawings Software Swiss Ephemeris Shopping Cart View Shopping Cart NEW: PERSONAL ECLIPSE REPORT All about AstrologyAstrology All about Astrology All about Astrology - Overview Introductions A Brief Introduction to Astrology First Steps in Astrology Understanding Astrology The Astrological Journal The Mountain Astrologer Advanced Astrology Articles Articles by Liz Greene Articles by Robert Hand Articles by Dana Gerhardt Community Forum Astro Wiki Astro-Databank ADB Search Tool Astrodienst Newsletter Education Study Astrology CPA London The Sophia Centre Astrologos Faculty of Astrological Studies Mercury Internet School MISPA Ephemeris Ephemeris 2020 Ephemeris 2021 9000 Years Ephemeris Swiss Ephemeris Atlas Direct Atlas Query ContactContact Contact Contact Phone, Email, Address About Astrodienst People at Astrodienst Authors at Astrodienst Freelancers About Astrodienst Jobs at Astrodienst Tools, Links, Infos Webmaster Tools Astrodienst Links FAQ Terms and Conditions Terms of Trade Privacy Policy Terms of Use Current Planets 7-Jun-2020, 13:00 UT/GMT Sun 17 13'59\" 22n49 Moon 9 56'36\" 24s03 Mercury 10 31'59\" 24n06 Venus 11 14'14\"r 21n43 Mars 17 0'33\" 7s28 Jupiter 26 21' 4\"r 21s06 Saturn 1 21'58\"r 20s01 Uranus 8 52'56\" 14n02 Neptune 20 53'37\" 4s35 Pluto 24 34'56\"r 22s07 TrueNode 29 5'18\" 23n26 Chiron 8 56'13\" 6n03 Explanations of the symbols Chart of the moment | My Astro | Forum | FAQ Accept Astrodienst Privacy Policy: This website uses cookies to make it easier for you to use our services."),
        new SentenceNode("s2","Two lovely black eyes, oh! what a surprise...")
    );
//    private static final List<SentenceNode> sents1 = Arrays.asList(
//            new SentenceNode("S1",NS,"One two three one three five five four one argle five.",lts,3.14159d),
//            new SentenceNode("S2",NS,"Eight nine one atheist.",lts,3.14159d),
//            new SentenceNode("S3",NS,"Nine nine three bee sea deaf elephant five three three argle bargle atheist three.",lts,3.14159d),
//            new SentenceNode("S4",NS,"Eight nine one the contrarian.",lts,3.14159d),
//            new SentenceNode("S5",NS,"One two three four five six sevsn eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty thity forty fifty sixty seventy eighty ninety hundred thousand million billion trillion numbers in this sentence.",lts,3.14159d),
//            new SentenceNode("S6",NS,"One two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty thity forty fifty sixty seventy eighty ninety hundred thousand million billion trillion numbers in this sentence.",lts,3.14159d),
//            new SentenceNode("S7",NS,"Push towards the cliff edge.",lts,3.14159d),
//            new SentenceNode("S8",NS,"Push towards the cliff edge.",lts,3.14159d)
//    );
//    
    /**
     * Get corpus (sentences).
     * @return 
     */
//    public static Map<String,SentenceNode> getVerticesForEdges()
//    {
//        return disc2Map;
//    }
//    
//    public static Map<AbstractMap.SimpleEntry<SentenceNode,SentenceNode>,SentenceEdge> getEdges()
//    {
//        return disc2Edges;
//    }
    
    public static List<SentenceNode> getVertices()
    {
        return sents;
    }
    
    public static String getTestDoc()
    {
        return tDoc;
    }
    
    /**
     * Get a list of sentences.
     * 
     * @param popl
     * @param doc
     * @return 
     */
    public static List<SentenceNode> getSentList( Populate popl, String doc )
    {
        return popl.extractSentences( doc, 0 );
    }
}
