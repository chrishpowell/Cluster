/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;

import eu.discoveri.predikt3.cluster.DocumentCategory;
import eu.discoveri.predikt3.cluster.RawDocument;
import eu.discoveri.predikt3.sentences.Token;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
        new SentenceNode("S1","American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.", new DocumentCategory(1,"television inventor")),
        new SentenceNode("S2","American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.", new DocumentCategory(1,"television inventor")),
        new SentenceNode("S3","With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall", new DocumentCategory(1,"television inventor")),
        new SentenceNode("S4","The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television", new DocumentCategory(1,"television inventor")),
        new SentenceNode("S5","If Utahans have their way, Philo T. Farnsworth will become a household name", new DocumentCategory(1,"television inventor")),
        new SentenceNode("S6","The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue.", new DocumentCategory(2,"Rocky")),
        new SentenceNode("T0","Confederate statues litter the squares of Southern states.", new DocumentCategory(3,"Confederates")),
        new SentenceNode("T1","The quick brown fox jumps over the lazy dog", new DocumentCategory(4,"fox lazy dog")),
        new SentenceNode("T2","The quick brown hound jumps over the lazy fox.", new DocumentCategory(4,"fox lazy dog")),
        new SentenceNode("C0","Tropical climate is one of the five major climate groups in the Köppen climate classification of heat.", new DocumentCategory(5,"climate")),
        new SentenceNode("C1","Tropical climates are broadly located within 20 to 25 degrees of the equator and characterized by monthly average temperatures of 18 ℃ (64.4 ℉), or higher year-round, often following a seasonal rhythm and where annual precipitation is generally abundant and sunlight is intense.", new DocumentCategory(5,"climate")),
        new SentenceNode("C2","Whew, it's hot!", new DocumentCategory(9,"singleton")),
        new SentenceNode("A1","Dear readers, as the sun shifts into fire sign Leo, the Tarot offers a collective message for all signs to ponder: Step up to the work presented by 5 of Swords, and invite the gifts of Knight of Cups.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A2","The 5 of Swords card depicts two figures walking away in defeat, their swords lay on the ground, as the third figure watches in satisfaction carrying three swords in their hands.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A3","In spirituality, there seems to be an overarching message that all we need is love and light, that rising above tribulations and becoming enlightened will end suffering.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A4","That message is not what this card represents.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A5","The energy we are being asked to experience is one of victory and defeat. We cannot bypass struggle and discomfort and all the “dark” aspects of life, we must move through them and allow them to fuel us.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A6","Saturday’s first planetary aspect is an opposition between the moon in balanced Libra and Chiron in impulsive Aries.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A7","At this moment, we may be trying to hold our ongoing wounds in check with Libra’s objectivity and patience.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A8","Chiron in Aries is the wounding of the self and the ways that we have been hurt by simply being who we are.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("A9","The moon in Libra is in polarity to this, as Libra tends to put others first for the sake of peace, a practice that can carry wounds of its own.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("Aa","The tension that this opposition story presents is an opportunity to bring our sense of self and personal authority back into some balance; by being objective enough to see that we are worthy as we are, yet being individualistic enough to stand in our authenticity.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("Ab","The rest of the day passes with no major aspects, until the moon/Mercury square of the early evening.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("Ac","The moon in Libra and Mercury in Cancer are both working strong, initiatory, cardinal powers, yet they do so at cross purposes.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("Ad","This evening, it’s difficult to bring the somewhat aloof emotional tone of the moment into harmony with the raw, vulnerable sentiments that have prevailed for the last two months.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("Ae","This edgy mood is further exacerbated by the moon opposing Mars in battle-ready Aries, an aspect that may bring emotional matters to a head tonight.", new DocumentCategory(6,"astrology horoscope")),
        new SentenceNode("P0","With eight successful Mars landings, NASA is upping the ante with its newest rover.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P1","The spacecraft Perseverance—set for liftoff this week—is NASA's biggest and brainiest Martian rover yet.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P2","It sports the latest landing tech, plus the most cameras and microphones ever assembled to capture the sights and sounds of Mars.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P3","Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P4","A helicopter is even tagging along for an otherworldly test flight.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P5","This summer's third and final mission to Mars—after the United Arab Emirates' Hope orbiter and China's Quest for Heavenly Truth orbiter-rover combo—begins with a launch scheduled for Thursday morning from Cape Canaveral.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P6","Like the other spacecraft, Perseverance should reach the red planet next February following a journey spanning seven months and more than 300 million miles (480 million kilometers).", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P7","The six-wheeled, car-sized Perseverance is a copycat of NASA's Curiosity rover, prowling Mars since 2012, but with more upgrades and bulk.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P8","Its 7-foot (2-meter) robotic arm has a stronger grip and bigger drill for collecting rock samples, and it's packed with 23 cameras, most of them in color, plus two more on Ingenuity, the hitchhiking helicopter.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("P9","The cameras will provide the first glimpse of a parachute billowing open at Mars, with two microphones letting Earthlings eavesdrop for the first time.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("Pa","Once home to a river delta and lake, Jezero Crater is NASA's riskiest Martian landing site yet because of boulders and cliffs, hopefully avoided by the spacecraft's self-navigating systems.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("Pb","Perseverance has more self-driving capability, too, so it can cover more ground than Curiosity.", new DocumentCategory(7,"NASA Mars")),
        new SentenceNode("Pc","The enhancements make for a higher mission price tag: nearly $3 billion.", new DocumentCategory(7,"NASA Mars"))
    );
    
    // Test sentences
    private static final List<SentenceNode> sents8 = Arrays.asList(
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
    public static List<RawDocument> testDocuments1()
        throws URISyntaxException
    {
        List<RawDocument> docs = new ArrayList<>();
        docs.add( new RawDocument(new DocumentCategory(1,"test"),
                "At this moment, we may be trying to hold our ongoing wounds in check with Libra’s objectivity and patience."
                + " Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space."));
        
        return docs;
    }
    
    public static List<RawDocument> testDocuments2()
        throws URISyntaxException
    {
        List<RawDocument> docs = new ArrayList<>();
        docs.add( new RawDocument(new DocumentCategory(1,"test"),
                "American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."
                + " American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."
                + " With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall."
                + " The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television."
                + " Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space."
                + " Its 7-foot (2-meter) robotic arm has a stronger grip and bigger drill for collecting rock samples, and it's packed with 23 cameras, most of them in color, plus two more on Ingenuity, the hitchhiking helicopter."
        ));
        
        return docs;
    }
    
    public static List<RawDocument> testDocuments()
            throws URISyntaxException
    {
        List<RawDocument> docs = new ArrayList<>();
        
        docs.add(new RawDocument(new DocumentCategory(1,"television inventor"),
                "American inventor Philo T. Farnsworth, a pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."
                + " American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."
                + " With his 81-year-old widow, Elma Farnsworth, looking on, the inventor was extolled as the father of television and his statue was placed in the pantheon of famous Americans of the Capitol’s National Statuary Hall."
                + " The clear favorite was one Philo T. Farnsworth, an inventor who is considered the father of television."
                + " If Utahans have their way, Philo T. Farnsworth will become a household name."));
        docs.add(new RawDocument(new DocumentCategory(2,"Rocky"),
                "The crew worked for more than two hours to separate the 8.5-foot bronze likeness of the city’s fictitious boxer from the steps of the Philadelphia Museum of Art, which has repeatedly insisted it doesn’t want the statue."));
        docs.add(new RawDocument(new DocumentCategory(3,"Confederates"),
                "Confederate statues litter the squares of Southern states."));
        docs.add(new RawDocument(new DocumentCategory(4,"fox lazy dog"),
                " The quick brown fox jumps over the lazy dog."
                + " The quick brown hound jumps over the lazy fox."));
        docs.add(new RawDocument(new DocumentCategory(5,"climate"),
                " Tropical climate is one of the five major climate groups in the Köppen climate classification of heat."
                + " Tropical climates are broadly located within 20 to 25 degrees of the equator and characterized by monthly average temperatures of 18 ℃ (64.4 ℉), or higher year-round, often following a seasonal rhythm and where annual precipitation is generally abundant and sunlight is intense."));
        docs.add(new RawDocument(new DocumentCategory(6,"astrology horoscope"),
                " Dear readers, as the sun shifts into fire sign Leo, the Tarot offers a collective message for all signs to ponder: Step up to the work presented by 5 of Swords, and invite the gifts of Knight of Cups."
                + " The 5 of Swords card depicts two figures walking away in defeat, their swords lay on the ground, as the third figure watches in satisfaction carrying three swords in their hands."
                + " In spirituality, there seems to be an overarching message that all we need is love and light, that rising above tribulations and becoming enlightened will end suffering."
                + " That message is not what this card represents."
                + " The energy we are being asked to experience is one of victory and defeat. We cannot bypass struggle and discomfort and all the “dark” aspects of life, we must move through them and allow them to fuel us."
                + " Saturday’s first planetary aspect is an opposition between the moon in balanced Libra and Chiron in impulsive Aries."
                + " At this moment, we may be trying to hold our ongoing wounds in check with Libra’s objectivity and patience."
                + " Chiron in Aries is the wounding of the self and the ways that we have been hurt by simply being who we are."
                + " The moon in Libra is in polarity to this, as Libra tends to put others first for the sake of peace, a practice that can carry wounds of its own."
                + " The tension that this opposition story presents is an opportunity to bring our sense of self and personal authority back into some balance; by being objective enough to see that we are worthy as we are, yet being individualistic enough to stand in our authenticity."
                + " The rest of the day passes with no major aspects, until the moon/Mercury square of the early evening."
                + " The moon in Libra and Mercury in Cancer are both working strong, initiatory, cardinal powers, yet they do so at cross purposes."
                + " This evening, it’s difficult to bring the somewhat aloof emotional tone of the moment into harmony with the raw, vulnerable sentiments that have prevailed for the last two months."
                + " This edgy mood is further exacerbated by the moon opposing Mars in battle-ready Aries, an aspect that may bring emotional matters to a head tonight."));
        docs.add(new RawDocument(new DocumentCategory(7,"NASA Mars"),
                " With eight successful Mars landings, NASA is upping the ante with its newest rover."
                + " It sports the latest landing tech, plus the most cameras and microphones ever assembled to capture the sights and sounds of Mars."
                + " Its super-sanitized sample return tubes—for rocks that could hold evidence of past Martian life—are the cleanest items ever bound for space."
                + " A helicopter is even tagging along for an otherworldly test flight."
                + " This summer's third and final mission to Mars—after the United Arab Emirates' Hope orbiter and China's Quest for Heavenly Truth orbiter-rover combo—begins with a launch scheduled for Thursday morning from Cape Canaveral."
                + " Like the other spacecraft, Perseverance should reach the red planet next February following a journey spanning seven months and more than 300 million miles (480 million kilometers)."
                + " The six-wheeled, car-sized Perseverance is a copycat of NASA's Curiosity rover, prowling Mars since 2012, but with more upgrades and bulk."
                + " Its 7-foot (2-meter) robotic arm has a stronger grip and bigger drill for collecting rock samples, and it's packed with 23 cameras, most of them in color, plus two more on Ingenuity, the hitchhiking helicopter."
                + " The cameras will provide the first glimpse of a parachute billowing open at Mars, with two microphones letting Earthlings eavesdrop for the first time."
                + " Once home to a river delta and lake, Jezero Crater is NASA's riskiest Martian landing site yet because of boulders and cliffs, hopefully avoided by the spacecraft's self-navigating systems."
                + " Perseverance has more self-driving capability, too, so it can cover more ground than Curiosity."
                + " The enhancements make for a higher mission price tag: nearly $3 billion."));
        docs.add(new RawDocument(new DocumentCategory(9,"singleton"),
                " Whew, it's hot!"));
        docs.add(new RawDocument(new DocumentCategory(10,"prediction pisces"),
                " Pisces 2021 Love Predictions."
                + " If you are able to use communication well this year, then the Pisces 2021 horoscope predicts you should have a fair amount of luck in love."
                + " You will be luckiest in love from February 25 to March 20, when Venus is in Pisces."
                + " You will be most compatible with people who are creative and spend much of their time expanding their skills."
                + " Pisces Career Prospects For 2021."
                + " 2021 yearly predictions foretell that Jupiter will have positive effects on your work life this year."
                + " You are likely to gain more satisfaction from everyday work tasks that may have bored you in the past."
                + " This could be because you will work with new people or you may learn something new that can help to make your job more rewarding."
                + " Either way, your career as a whole is likely to be more fulfilling this year."
                + " If you are looking for more work this year, consider working online."
                + " For Pisces people especially, 2021 is a great year to get money from creative projects done online." 
                + " If you have creative talent, consider creative writing or making art on commission.Pisces Finance 2021 Forecasts."
                + " This year, for one reason or another, you may be required to share your money and other resources with others."
                + " When this happens, make sure to keep your cool and to compromise peacefully."
                + " If you do not learn how to compromise with others in this way, then you are likely to find yourself in disagreements that could have been easily avoided."
                + " If you manage to have your finances separate from everyone else’s, which is unlikely but can happen, then this is a great year to make investments."
                + " However, before you do this, make sure to ask for advice as to what investments to make."
                + " Pisces Family Predictions 2021."
                + " Neptune in Pisces (for the entire year) will have a large effect on your personal life, including your family life."
                + " This year, you may struggle to stay true to yourself."
                + " In some cases, your family may be the one causing your internal conflicts." 
                + " If you can, try to show your true self to your family."
                + " Do not plan to have a baby out of pressure."
                + " Health Horoscope For The FISHES."
                + " In 2021, it’s best that you put most of the focus on health on your physical health."
                + " You are likely to have to do things that will take a toll on your physical body."
                + " For this reason, it’s best to exercise often and remember to eat right." 
                + " This is also a great year to try a detox to cleanse the body."
                + " Pisces Social Life Changes."
                + " Venus finds itself in Capricorn at the beginning of the year (for most of January) and again in November throughout the rest of 2021."
                + " When Venus is in this sign, you will feel more social than usual."
                + " These months are the best time for hanging out with large groups of people and reconnecting with old friends."
                + " 2021 Forecasts for Pisces Birthdays."
                + " Jupiter will be in Scorpio for most of 2021, which will have a positive influence on your year for the better."
                + " This planet will encourage feelings of self-love and boost your creativity."
                + " This can help in so many areas of your life, including your romantic life, hobbies, and family life."
                + " This year, you are also likely to feel more determined to try and learn new things."
                + " Even if you are inexperienced with something, don’t be afraid to ask for help and try something new."
                + " Learning new things and working on your hobbies is of the utmost importance this year."
                + " Doing this will expand your happiness considerably during the 2021 Mercury retrograde."
                + " Pisces 2021 Monthly Horoscopes."
                + " Let’s take a closer look at the Pisces 2021 horoscope by breaking the year down into months."
                + " Below are short summaries for each individual month."
                + " January 2021 starts the year off by giving you a sense of nearly boundless creativity and productivity."
                + " Use this time to work on projects, both at work and in your hobbies."
                + " February 2021 encourages you to use this month for introspection."
                + " What do you want out of the year?"
                + " This also continues to be a great time to work on projects."
                + " March 2021 puts the focus on your financial state."
                + " The Sun and Venus will be in your sign for most of this month; this will boost your communication skills."
                + " April 2021 splits the focus between your platonic relationships and your finances."
                + " This is a great time to seek advice on your finances, as this combines those two aspects of your life."
                + " May 2021 puts the focus on your family life!"
                + " For most of this month, both Venus and the Sun will be in Taurus."
                + " This will help to add stability in your relationships."
                + " June 2021 encourages you to work on your romantic pursuits."
                + " With Neptune in Pisces all this year, this is a great time to get into a new relationship or take the next step in your current relationship."
                + " July 2021 won’t have much of a focus at all."
                + " Your daily routine will take over."
                + " Try to make time to spend time with your significant other and to work on your hobbies."
                + " August 2021 will bless you with a boost of creativity and confidence."
                + " This makes August a great time to try to make new friends and to work on your hobbies."
                + " September 2021 is a mixed bag."
                + " Until the 10th, Venus will be in Libra, which can help to boost your romantic life."
                + " After this period, you will be most likely to shift the focus on your family."
                + " October 2021 will have your emotions at a high point, which can make you seem dreamy at times."
                + " Try to stay focused, as your family is likely to need you more than usual this month."
                + " This may affect your relationships with your friends."
                + " November 2021 graces you with high levels of confidence."
                + " You are also likely to find some luck at work and in your finances."
                + " December 2021 gives you time to prepare for the year ahead."
                + " You will be able to see potential changes in the distance."
                + " Use this month to plan your goals for 2022."
                + " Conclusion."
                + " As mentioned earlier, the yearly Pisces 2021 horoscope predicts a relaxed year ahead."
                + " This is the perfect year for getting back to your hobbies and focusing on your family life."
                + " Use this year to better yourself both intellectually and socially."
                + " If you can do this, then 2021 should go well for you."));
        docs.add(new RawDocument(new DocumentCategory(11,"prediction aries"),
                " Libra people can achieve what they aspire for by making minor alterations in daily activities and avoiding significant changes."
                + " Planetary aspects are favorable for improving your health and vitality during this year."
                + " Jupiter and Saturn will help you to achieve your goals without much problem during the year 2021."
                + " Venus and Jupiter will help you in forming love relationships."
                + " There will be plenty of love and romance."
                + " Social life will be enjoyable with the help of good astrological influences."
                + " You will take measures to maintain your physical and emotional health."
                + " You should be enthusiastic about doing whatever you want."
                + " Libra 2021 Horoscope – A Look At The Year Ahead"
                + " The Libra 2021 horoscope brings good news of hope and positive change!"
                + " This year, you will need to rely on your relationships, both romantic and platonic, to help get you through the year."
                + " It’s also a great year to revisit old friendships and hobbies."
                + " This is a great year for both learning from the past and looking toward the future."
                + " Libra 2021 Love Predictions"
                + " In January, Venus will be in Capricorn, which may make you feel more intensely about your partner in a loving way, but generally less passionate."
                + " Depending on what sign you are in a relationship with, this could help to improve your relationship or it may hurt it."
                + " Libra horoscope 2021 suggests you follow your partner’s lead to know what the right thing to do is."
                + " Libra Career Prospects For 2021"
                + " Jupiter presides over your career this year, which can make work-related decisions tricky."
                + " You are likely to be extremely busy this year, especially from March to July, when Jupiter will be in a Scorpio retrograde."
                + " Even during this time, try to remain as professional as possible. This can open up new opportunities, like travel and meeting new people."
                + " Libra Finance 2021 Forecasts"
                + " Throughout 2021, Saturn will be in Capricorn."
                + " This can make it difficult to manage your finances if you let yourself give in to material temptations."
                + " You will find it harder to save money than usual."
                + " This is not because of a lesser cash flow, but an increase in your spending."
                + " Try to reduce your spending on nonessential items to keep your finances healthy."
                + " Libra Family Predictions 2021"
                + " Uranus spends most of 2021 in Aries, which will cause some dramatic changes in your family life."
                + " These changes will most likely have to do with your parents or older relatives."
                + " Expect a shift in your family’s power dynamic."
                + " You may have to take care of your parents or older relatives for a few months, if not longer, beginning this year."
                + " As far as your other family members are concerned, Venus in Capricorn (from November through the end of the year) helps you feel close to your family members and close community members."
                + " It is likely that you will want to make improvements to your relationships, your physical home, have a baby during these two months."
                + " Health Horoscope For The SCALES"
                + " During 2021, Mars will have several transits through Aquarius, which is likely to feel more ambitious when it comes to your health-related goals."
                + " However, it is also likely that you will try to improve your health, not for the sake of your well-being, but more for the sake of your looks."
                + " Try not to push yourself."
                + " Avoid crazy trendy diets."
                + " If you can meet your fitness goals in a healthy way, that’s great!"
                + " If not, you will end up being all the less healthy for it especially during the 2021 Mercury retograde periods."
                + " Libra Social Life Changes"
                + " Yearly astrology prediction for 2021 foretells that meeting new people and making new friends is vital to your success in many areas of life this year."
                + " Whether you want to excel in your career or complete a project, your friends will be there to help you."
                + " At the same time, your friends are likely to require your help this year."
                + " Reach out to them whenever possible."
                + " You never know when they might need you."
                + " 2021 Forecasts for Libra Birthdays"
                + " Jupiter enters Sagittarius in November."
                + " This will encourage you to pick back up on old projects you may have left to the wayside or to begin new projects if you don’t have any currently in progress."
                + " This is also a great time to reconnect with old friends or make new friends."
                + " All in all, these last two months of the year will inspire positive change."
                + " Libra 2021 Monthly Horoscopes"
                + " The year seems promising as a whole."
                + " Below are the individual blessings each month of 2021 has to offer to the lucky Libra-born."
                + " January 2021 brings creativity into your personal life and happiness into your family life."
                + " This month will keep you feeling zen."
                + " Venus in Capricorn also helps to improve your communication."
                + " February 2021 is the perfect month to focus on your romantic relationships."
                + " Jupiter and Saturn both help to improve your communication skills."
                + " Mars in Sagittarius encourages you to be more creative and passionate."
                + " March 2021 focuses on your platonic social life, especially at work."
                + " Make friends with your coworkers and help your friends when you can."
                + " Your relationships with your children or young relatives will also improve in March."
                + " April 2021 again focused on romance, especially in regards to your sex life."
                + " Jupiter in Scorpio helps to jazz things up in the bedroom."
                + " Saturn in Capricorn helps to keep your communication stable."
                + " May 2021 has the Sun in Taurus encouraging deep introspection."
                + " Focus on yourself, your mental and physical health, your career, and your family."
                + " Try to keep all these things balanced."
                + " This can be stressful, but as a Libra, “Balance” may as well be your middle name."
                + " June 2021 can bring mood swings, which can put a dent in your relationships and slow down any progress you have been making on your hobby projects."
                + " Try to work through the month, knowing that the coming months will be better."
                + " July 2021 allows you to regain focus on your projects, both at work and in your hobbies."
                + " Your energy levels are also likely to change."
                + " This may impact your relationships with your family members, including your significant other."
                + " August 2021 brings luck to your social life."
                + " You will be more charming than usual this month, due to Venus being in Libra from mid-August to mid-September."
                + " This will work to improve your romantic relationships."
                + " September 2021 again focuses on romantic relationships."
                + " You will have an outpouring of attention to others."
                + " When Venus enters Scorpio in mid-September, you will be more confident and seductive than usual."
                + " October 2021 makes you regain focus on work and others."
                + " Try to spend less time focusing on yourself."
                + " If you do this, then your social life and career will see improvements."
                + " November 2021 may seem a bit confusing."
                + " Should you focus on your job or travel?"
                + " Focus on your goals or your friendships?"
                + " No matter what you choose, you will likely succeed."
                + " However, if you can’t make a choice, you will face the consequences of your indecision."
                + " December 2021 encourages you to focus on your goals and complete any projects you started during the year."
                + " You may need to distance yourself from your loved ones to do this."
                + " However, once you complete your project, you’ll have the rest of the year to focus on your social life."
                + " Conclusion"
                + " Libra horoscope 2021 shows a promising year."
                + " Whether you are hoping for blessings in your social life or your hobbies, you are sure to be in luck."
                + " Make sure to spread your love and attention evenly to ensure the best year possible."));
            docs.add(new RawDocument(new DocumentCategory(12,"tokenless"),
                " The the the."
                + "          ."
                + " 18."
                + " 12/12 SLIDES SHARE SHARE TWEET SHARE EMAIL 1/12 SLIDES Next Slide AdChoices 1 2 3 4 YOU MAY LIKE Ad Microsoft Savvy Americans do this to earn an extra $1,394 per month in retirement The Motley Fool New Car Gadget Magically Removes Scratches & Dents NanoMagic 23 Gadgets That Could Sell Out Before the Holidays Gadgets Post More from Astrofame The Reason Why Each Zodiac Sign Is Difficult To Love Your Weekly Horoscope: August 24 - 30 Who Is Your Zodiac Sign Sexually Incompatible With?"
                + " ..."));
            docs.add(new RawDocument(new DocumentCategory(13,"tokenless"),"")
        );
                        
        return docs;
    }
    
    public static List<SentenceNode> getVertices()
    {
        return sents9;
    }
    
    public static String getTestDoc()
    {
        return tDoc;
    }
    
    /**
     * Get a list of sentences.
     * 
     * @param popl
     * @param dcat
     * @return 
     */
    public static List<SentenceNode> getSentList( Populate popl, String doc, DocumentCategory dcat )
    {
        return popl.extractSentences( doc, dcat );
    }
}
