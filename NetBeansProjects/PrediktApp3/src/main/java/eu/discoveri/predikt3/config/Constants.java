/*
 * To be merged into generic Constants
 */
package eu.discoveri.predikt3.config;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Constants
{
    // Resource root
    public static final String      RESROOT = "/home/chrispowell/NetBeansProjects/";
    public static final String      PROJECT = "PrediktApp3/";
    public static final String      PROJSUBPATH = "src/main/java/";
    public static final String      ROOT = RESROOT+PROJECT+PROJSUBPATH;
    
    // Search clusters
    public static final String      STORECLUSTERSPATH = RESROOT+"WebScrape/src/main/java/resources/model/";
    public static final String      STORECLUSTERSEXT = "cluster";
    
    // Num. clusters to process
    public static final int         TOPNUMCLUSTERS = 5;
    
    // Default server
    public static final String      DEFSRV = "eu.discoveri";
    // Default namespace
    public static final String      DEFNS = "eu.discoveri.predikt3";
    
    // Resource path and files
    public static final String      RESMODELS = ROOT+"eu/discoveri/predikt3/resources/";
    
    // English sentences lengths
    public static final int         ENGLISHGOODMAXLEN = 20;
    public static final int         ENGLISHDIFFICULTLEN = 29;
    public static final int         ENGLISHBADMAXLEN = 35;                        // Sentences words longer than this get dropped.
    
    // OpenNLP model files (built via Language class)
    public static final String      SENTMODEL = "Sentence";
    public static final String      TOKENMODEL = "Token";
    public static final String      SEQMODEL = "Sequence";                        // POSTaggerME sequence/outcome
    public static final String      CHUNKMODEL = "Chunk";
    public static final String      PARSEMODEL = "Parse";
    public static final String      LEMMATIZE = "Lemmatize";
    
    public static final String      ENSENTFILE = "en-sent.bin";
    public static final String      ESSENTFILE = "es-sent.bin";
    public static final String      ENTOKENFILE = "en-token.bin";
    public static final String      ESTOKENFILE = "es-token.bin";
    public static final String      ENPOSMEFILE = "en-pos-maxent.bin";
    public static final String      ESPOSMEFILE = "es-pos-maxent.bin";
    public static final String      ENLEMMAFILE = "en-lemmatizer.dict";
    public static final String      ESLEMMAFILE = "es-lemmatizer.dict";
    
    // Neo4j constants
    public static int               WORD = 1, POSID = 2, LANGID = 3, LEMMID = 4, PAGEID = 5;
    public static String            INPATH = RESROOT+"Lemmas/src/main/java/es/discoveri/lemmas/txt/";
    
    // Prepared statements
    public static String            WORDPS = "insert into lemma.Word values(default,?,?,?,?,?)  on duplicate key update word=word,POSId=POSId";
    public static String            LEMMAPS = "insert into lemma.Lemma values(default,?) on duplicate key update lemma=lemma";
    public static String            LANGCODEPS = "insert into lemma.LangCode values(?,?)";
    public static String            PENNPOSCODEPS = "insert into lemma.PennPOSCode values(?,?)";
    public static String            PAGEPS = "";
    public static String            LEMMA4WORDPS = "select id from lemma.Lemma where lemma = ?";
    public static String            LEMMANULLPS = "insert into lemma.Lemma values(default,?)";
    public static String            PAGEZEROPS = "insert into lemma.Page values(default,?,?)";
    
    // Node score default (Milhelcea et al)
    public static final double      NODESCOREDEF = 0.25;
    // Node UUID
    public static final String      GRAPHNAMESPACE = "eu.discoveri.languagegraph";
    
    // Text Ranking parameters (?? No longer used?)
//    public static final double      TEXTRANK_DAMPING_FACTOR = 0.85D;
//    public static final double      SCORECONVERGE = 0.005D;
//    public static final int         NUMITERS = 100;

    // Transaction max. duration
    public static final int         TXDURATION = 30;                            // Secs
    // If sentence edge weight too small do not persist edge
    public static final double      EDGEWEIGHTMIN = 0.75;
    // Do NOT make edge weight zero.  Currently causes clustering problems
    public static final double      EDGEWEIGHTNRZERO = 0.000001;

    // Pagination
    public static final int         PAGSIZE = 7;
    public static final int         MAXSENTLEN = 511;                           // "Sentence" this long is probably nonsense
    
    // MySQL settings
    public static final int         MAXCONNECTIONS = 200;                       // mysql>set global Max_connections=#
    public static final int         TIMEOUTMS = 30000;                          // mS.  Be aware a timeout can mean a write failure.
    
    // Db threads, retry consts for locking clash
    public static final int         THREADLISTSIZE = 1;
    public static final int         THREADPAUSEMSECS = 250;
    public static final int         THREADRETRIES = 5;
}
