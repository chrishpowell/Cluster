/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import eu.discoveri.predikt3.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author chrispowell
 */
public class LSHTest
{
    /**
     * TIDF: Term Frequency/Inverse Document Frequency.
     * 
     * @param totDocs
     * @param documents
     * @param norm021 Normalise tfidf values to [0..1]
     * @return
     */
    static double maxTfidf = Double.MIN_VALUE;
    static double csimMax  = Double.MIN_VALUE;                      // Cos sim. max. (most similar)
    public static Map<Map<String,Integer>,Double> tfidf( int totDocs, List<List<String>> documents, boolean norm021 )
    {
        // TF: Count of lemmas per document <<lemma,docId>,count/docsize>
        Map<Map<String,Integer>,Double>     tf = new HashMap<>();
        // IDF: lemma,<Double> log(D/corpuscount)
        Map<String,Double>                  idf = new HashMap<>();
        // TFIDF <Double>, for each lemma/doc pair <String,Integer>
        Map<Map<String,Integer>,Double>     tfidf = new HashMap<>();


        // Init TF terms to zero for ALL docs
        documents.forEach(ls -> {
            ls.forEach(s -> {
                for( int dd=1; dd<=totDocs; dd++ )
                    tf.put(Map.of(s,dd), 0.d);
            });
        });

        // IDF: Count of docs in which term appears
        Set<String> done = new HashSet<>();
        documents.forEach(doc -> {
            doc.forEach(term -> {
                double c = 0.d;
                if (!done.contains(term)) {
                    done.add(term);
                    c = documents.stream()
                                 .filter(docin -> ( docin.contains(term) ))
                                 .map(_item -> 1.0)
                                 .reduce(c, (accumulator, _item) -> accumulator + 1);
                    idf.put(term, c);
                }
            });
        });
        
        int ii = 0;
        // TF: Ratio term to all terms in doc
        for( List<String> ls: documents )
        {
            // docId
            ii++;
            // Count of words in doc
            Map<String,Integer> hm = new HashMap<>();

            // Loop over words in doc
            ls.forEach(s -> {
                // Count of terms in THIS doc
                if( hm.containsKey(s) )
                {
                    int c = hm.get(s);
                    hm.put(s, ++c);
                }
                else
                    hm.put(s,1);
            });
            
            // TF: <<Lemma,docId>,count/docsize>
            for( Map.Entry<String,Integer> m: hm.entrySet() )
                tf.put( Map.of(m.getKey(),ii), (double)m.getValue()/(double)ls.size() );
        }
        
        // TF: Term Frequency
//        System.out.println("TF:");
//        tf.forEach((k,v) -> {
//            k.forEach((k1,v1) -> {
//                System.out.print(" " +k1+ "/" +v1);
//            });
//            System.out.println(": " +v);
//        });
        
        // IDF: Inverse Document Frequency.
        System.out.println("IDF build:");
        idf.forEach((k,v) -> {
            double idfv = Math.log10(totDocs/v);
            idf.put(k, idfv);
        });

        // TF-IDF
        idf.entrySet().stream().map(idf2 -> {
            return idf2;
        }).forEachOrdered(idf2 -> {
            tf.entrySet().forEach(tf1 -> {
                double tfidfVal = tf1.getValue()*idf2.getValue();
                tfidf.put(tf1.getKey(), tfidfVal);
                if( tfidfVal > maxTfidf )
                    maxTfidf = tfidfVal;
            });
        });
        
        // Normalise to [0..1]
        if( norm021 )
        {
            tfidf.forEach((k,v) -> {
                // Update value
                double csim = v/maxTfidf;
                tfidf.put(k, csim);
                
                // Check for max. value (most similar)
                if( csim > csimMax )
                    csimMax = csim;
            });
        }
        
        return tfidf;
    }
    
    public static double[][] vectorise( int nDocs, Map<Map<String,Integer>,Double> tfidf )
    {
        // Total terms
        int nTerms = tfidf.size()/nDocs;
        int[] idx = new int[nDocs];
        double[][] vecs = new double[nDocs][nTerms];

        
        System.out.println("TF-IDF build: (" +tfidf.size()+ "/" +nTerms+ ")");
        for( Map.Entry<Map<String,Integer>,Double> m: tfidf.entrySet() )
        {
            for( Map.Entry<String,Integer> m1: m.getKey().entrySet() )
            {
                vecs[m1.getValue()-1][idx[m1.getValue()-1]++] = m.getValue();
//                System.out.println("Term: " +m1.getKey()+ ", Doc num: " +m1.getValue()+ ", doc entry: " +(idx[m1.getValue()-1])+ ", value: " +m.getValue());
            }
        }
//        System.out.println(">> " +Arrays.deepToString(vecs));
        
        return vecs;
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        //... Documents
        // From [https://towardsdatascience.com/understanding-nlp-word-embeddings-text-vectorization-1a23744f7223]. Expect cosine sim: 0.822
//        List<String> d0 = Arrays.asList("Julie","loves","John","more","than","Linda","loves","John");
//        List<String> d1 = Arrays.asList("Jane","loves","John","more","than","Julie","loves","john");
//        List<List<String>> documents = Arrays.asList(d0,d1);
        
        // From [https://guendouz.wordpress.com/2015/02/17/implementation-of-tf-idf-in-java/], "ipsum" doc1, expect 0.2027325540540822 (logE)
//        List<String> doc0 = Arrays.asList("Lorem", "ipsum", "dolor", "ipsum", "sit", "ipsum");
//        List<String> doc1 = Arrays.asList("Vituperata", "incorrupte", "at", "ipsum", "pro", "quo");
//        List<String> doc2 = Arrays.asList("Has", "persius", "disputationi", "id", "simul");
//        List<List<String>> documents = Arrays.asList(doc0, doc1, doc2);

        // From [https://en.wikipedia.org/wiki/Tf–idf], tf("example",d0)=0 [A]; tf("example",d1)=0.429 [B]; idf("example",D)=0.301 [C]
        //   tfidf("example",d0,D)=[A]x[C]=0; tfidf("example",d1,D)=[A]x[B]=0.129
//        List<String> doc0 = Arrays.asList("this","is","a","a","sample");
//        List<String> doc1 = Arrays.asList("this","is","another","another","example","example","example");
//        List<List<String>> documents = Arrays.asList(doc0,doc1);

        // From [https://kavita-ganesan.com/tfidftransformer-tfidfvectorizer-usage-differences/]
        //   tfidf("had",d1,D)=0.493562; tfidf("house",d1,D)=0.398203; tfidf("cat",d1,D)=0.0
        List<String> doc0 = Arrays.asList("the", "house", "had", "tiny", "little", "mouse");
        List<String> doc1 = Arrays.asList("the", "cat", "saw", "the", "mouse");
        List<String> doc2 = Arrays.asList("the", "mouse", "ran", "away", "from", "the", "house");
        List<String> doc3 = Arrays.asList("the", "cat", "finally", "ate", "the", "mouse");
        List<String> doc4 = Arrays.asList("the", "end", "of", "the", "mouse", "story");
        List<String> doc5 = Arrays.asList("elephants","are","grey","with","long","trunks");
        List<String> doc6 = Arrays.asList("American", "inventor", "Philo T. Farnsworth", "pioneer", "of", "television", "was", "accorded", "what", "many", "believe", "was", "long", "overdue", "glory", "Wednesday", "when", "7-foot", "bronze", "likeness", "of", "the", "electronics", "genius", "was", "dedicated", "in", "the", "U.S.", "Capitol.");
        List<String> doc7 = Arrays.asList("American", "inventor", "Philo T. Farnsworth", "pioneer", "of", "television", "was", "honored", "when", "7-foot", "bronze", "likeness", "of", "the", "electronics", "genius", "was", "dedicated", "in", "the", "U.S. Capitol.");
        List<String> doc8 = Arrays.asList("With", "his", "81-year-old", "widow", "Elma", "Farnsworth", "looking", "on", "the", "inventor", "was", "extolled", "as", "the", "father", "of", "television", "and", "his", "statue", "was", "placed", "in", "the", "pantheon", "of", "famous", "Americans", "of", "the", "Capitol’s", "National", "Statuary", "Hall");
        List<String> doc9 = Arrays.asList("The", "clear", "favorite", "was", "one", "Philo T. Farnsworth", "inventor", "who", "is", "considered", "the", "father", "of", "television");
        List<String> doc10 = Arrays.asList("If", "Utahans", "have", "their", "way", "Philo T. Farnsworth", "will", "become", "household", "name");
        List<String> doca = Arrays.asList("If", "Utahans", "have", "their", "way", "Philo T. Farnsworth", "will", "become", "household", "name");
        List<String> doc11 = Arrays.asList("The", "crew", "worked", "for", "more", "than", "two", "hours", "to", "separate", "the", "8.5-foot", "bronze", "likeness", "of", "the", "city’s", "fictitious", "boxer", "from", "the", "steps", "of", "the", "Philadelphia", "Museum", "of", "Art", "which", "has", "repeatedly", "insisted", "it", "doesn’t", "want", "the", "statue.");
        List<String> doc12 = Arrays.asList("Confederate", "statues", "litter", "the", "squares", "of", "Southern", "states");
        List<String> doc13 = Arrays.asList("The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog");
        List<String> doc14 = Arrays.asList("The", "quick", "brown", "hound", "jumps", "over", "the", "lazy", "fox");
        List<String> doc15 = Arrays.asList("Tropical", "climate", "is", "one", "of", "the", "five", "major", "climate", "groups", "in", "the", "Köppen", "climate", "classification", "of", "heat");
        List<String> doc16 = Arrays.asList("Tropical", "climates", "are", "broadly", "located", "within", "to", "degrees", "of", "the", "equator", "and", "characterized", "by", "monthly", "average", "temperatures", "of", "or", "higher", "year", "round", "often", "following", "seasonal", "rhythm", "and", "where", "annual", "precipitation", "is", "generally", "abundant", "and", "sunlight", "is", "intense");
        List<String> doc17 = Arrays.asList("Whew", "it's", "hot!");
        List<String> doc18 = Arrays.asList("Dear", "readers", "as", "the", "sun", "shifts", "into", "fire", "sign", "Leo", "the", "Tarot", "offers", "a", "collective", "message", "for", "all", "signs", "to", "ponder", "Step", "up", "to", "the", "work", "presented", "by", "of", "Swords", "and", "invite", "the", "gifts", "of", "Knight", "of", "Cups");
        List<String> doc19 = Arrays.asList("The", "five", "of", "Swords", "card", "depicts", "two", "figures", "walking", "away", "in", "defeat", "their", "swords", "lay", "on", "the", "ground", "as", "the", "third", "figure", "watches", "in", "satisfaction", "carrying", "three", "swords", "in", "their", "hands");
        List<String> doc20 = Arrays.asList("In", "spirituality", "there", "seems", "to", "be", "an", "overarching", "message", "that", "all", "we", "need", "is", "love", "and", "light", "that", "rising", "above", "tribulations", "and", "becoming", "enlightened", "will", "end", "suffering");
        List<String> doc21 = Arrays.asList("That", "message", "is", "not", "what", "this", "card", "represents");
        List<String> doc22 = Arrays.asList("The", "energy", "we", "are", "being", "asked", "to", "experience", "is", "one", "of", "victory", "and", "defeat", "We", "cannot", "bypass", "struggle", "and", "discomfort", "and", "all", "the", "dark", "aspects", "of", "life", "we", "must", "move", "through", "them", "and", "allow", "them", "to", "fuel", "us");
        List<String> doc23 = Arrays.asList("Saturday's", "first", "planetary", "aspect", "is", "an", "opposition", "between", "the", "moon", "in", "balanced", "Libra", "and", "Chiron", "in", "impulsive", "Aries");
        List<String> doc24 = Arrays.asList("At", "this", "moment", "we", "may", "be", "trying", "to", "hold", "our", "ongoing", "wounds", "in", "check", "with", "Libra's", "objectivity", "and", "patience");
        List<String> doc25 = Arrays.asList("Chiron", "in", "Aries", "is", "the", "wounding", "of", "the", "self", "and", "the", "ways", "that", "we", "have", "been", "hurt", "by", "simply", "being", "who", "we", "are");
        List<String> doc26 = Arrays.asList("The", "moon", "in", "Libra", "is", "in", "polarity", "to", "this", "as", "Libra", "tends", "to", "put", "others", "first", "for", "the", "sake", "of", "peace", "a", "practice", "that", "can", "carry", "wounds", "of", "its", "own");
        List<String> doc27 = Arrays.asList("The", "tension", "that", "this", "opposition", "story", "presents", "is", "an", "opportunity", "to", "bring", "our", "sense", "of", "self", "and", "personal", "authority", "back", "into", "some", "balance", "by", "being", "objective", "enough", "to", "see", "that", "we", "are", "worthy", "as", "we", "are", "yet", "being", "individualistic", "enough", "to", "stand", "in", "our", "authenticity");
        List<String> doc28 = Arrays.asList("The", "rest", "of", "the", "day", "passes", "with", "no", "major", "aspects", "until", "the", "moon", "Mercury", "square", "of", "the", "early", "evening");
        List<String> doc29 = Arrays.asList("The", "moon", "in", "Libra", "and", "Mercury", "in", "Cancer", "are", "both", "working", "strong", "initiatory", "cardinal", "powers", "yet", "they", "do", "so", "at", "cross", "purposes");
        List<String> doc30 = Arrays.asList("This", "evening", "it's", "difficult", "to", "bring", "the", "somewhat", "aloof", "emotional", "tone", "of", "the", "moment", "into", "harmony", "with", "the", "raw", "vulnerable", "sentiments", "that", "have", "prevailed", "for", "the", "last", "two", "months");
        List<String> doc31 = Arrays.asList("This", "edgy", "mood", "is", "further", "exacerbated", "by", "the", "moon", "opposing", "Mars", "in", "battle", "ready", "Aries", "an", "aspect", "that", "may", "bring", "emotional", "matters", "to", "head", "tonight");
        List<String> doc32 = Arrays.asList("With", "eight", "successful", "Mars", "landings", "NASA", "is", "upping", "the", "ante", "with", "its", "newest", "rover");
        List<String> doc33 = Arrays.asList("The", "spacecraft", "Perseverance", "set", "for", "liftoff", "this", "week", "is", "NASA", "s", "biggest", "and", "brainiest", "Martian", "rover", "yet");
        List<String> doc34 = Arrays.asList("It", "sports", "the", "latest", "landing", "tech", "plus", "the", "most", "cameras", "and", "microphones", "ever", "assembled", "to", "capture", "the", "sights", "and", "sounds", "of", "Mars");
        List<String> doc35 = Arrays.asList("Its", "super", "sanitized", "sample", "return", "tubes", "for", "rocks", "that", "could", "hold", "evidence", "of", "past", "Martian", "life", "are", "the", "cleanest", "items", "ever", "bound", "for", "space");
        List<String> doc36 = Arrays.asList("A", "helicopter", "is", "even", "tagging", "along", "for", "an", "otherworldly", "test", "flight");
        List<String> doc37 = Arrays.asList("This", "summer's", "third", "and", "final", "mission", "to", "Mars", "after", "the", "United", "Arab", "Emirates", "Hope", "orbiter", "and", "China's", "Quest", "for", "Heavenly", "Truth", "orbiter", "rover", "combo", "begins", "with", "launch", "scheduled", "for", "Thursday", "morning", "from", "Cape", "Canaveral");
        List<String> doc38 = Arrays.asList("Like", "the", "other", "spacecraft", "Perseverance", "should", "reach", "the", "red", "planet", "next", "February", "following", "a", "journey", "spanning", "seven", "months", "and", "more", "than", "million", "miles", "million", "kilometers");
        List<String> doc39 = Arrays.asList("The", "six", "wheeled", "car", "sized", "Perseverance", "is", "copycat", "of", "NASA's", "Curiosity", "rover", "prowling", "Mars", "since", "but", "with", "more", "upgrades", "and", "bulk");
        List<String> doc40 = Arrays.asList("Its", "7-foot", "meter", "robotic", "arm", "has", "a", "stronger", "grip", "and", "bigger", "drill", "for", "collecting", "rock", "samples", "and", "it's", "packed", "with", "cameras", "most", "of", "them", "in", "color", "plus", "two", "more", "on", "Ingenuity", "the", "hitchhiking", "helicopter");
        List<String> doc41 = Arrays.asList("The", "cameras", "will", "provide", "the", "first", "glimpse", "of", "parachute", "billowing", "open", "at", "Mars", "with", "two", "microphones", "letting", "Earthlings", "eavesdrop", "for", "the", "first", "time");
        List<String> doc42 = Arrays.asList("Once", "home", "to", "river", "delta", "and", "lake", "Jezero", "Crater", "is", "NASA's", "riskiest", "Martian", "landing", "site", "yet", "because", "of", "boulders", "and", "cliffs", "hopefully", "avoided", "by", "the", "spacecraft's", "self", "navigating", "systems");
        List<String> doc43 = Arrays.asList("Perseverance", "has", "more", "self", "driving", "capability", "too", "so", "it", "can", "cover", "more", "ground", "than", "Curiosity");
        List<String> doc44 = Arrays.asList("The", "enhancements", "make", "for", "higher", "mission", "price", "tag", "nearly", "billion");
        List<List<String>> documents = Arrays.asList(doc0,doc1,doc2,doc3,doc4,doc5,doc6,doc7,doc8,doc9,doc10,doca,doc11,doc12,doc13,doc14,doc15,doc16,doc17,doc18,doc19,doc20,doc21,doc22,doc23,doc24,doc25,doc26,doc27,doc28,doc29,doc30,doc31,doc32,doc33,doc34,doc35,doc36,doc37,doc38,doc39,doc40,doc41,doc42,doc43,doc44);

        // Num. docs
        int totDocs = documents.size();
        
        // TFIDF (vectorization)
        boolean norm021 = true;
        Map<Map<String,Integer>,Double> mm = tfidf(totDocs,documents,norm021);
        
        // Show TF-IDF
//        System.out.println("TF-IDF:");
//        mm.forEach((k,v) -> {
//            // For each <lemma,doc>
//            k.entrySet().forEach(l -> {
//                System.out.print(" " +l.getKey()+ "/" +l.getValue());
//            });
//            // tfidf
//            System.out.println(" : " +v);
//        });

        // Cosine similarity
        double[][] vecs = vectorise(totDocs,mm);
        System.out.println("\r\n(Cosine) Similarity, 0.0=None, 1.0=Same");
        for( int ii=0; ii<totDocs; ii++ )
            for( int jj=ii+1; jj<totDocs; jj++ )
                System.out.println("Cosine similarity, v[" +ii+ "]/v[" +jj+ "]: " +Utils.cosineSimilarity(vecs[ii],vecs[jj]));

        // Document (hierarchical) clustering (max. 65535 docs)
        
    }
}
