/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt3.cluster.LemmaScore;
import eu.discoveri.predikt3.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//import java.util.LinkedList;


/**
 *
 * @author chrispowell
 */
public class TfIdf
{
    private static HikariDataSource connection = null;
    static
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/documents?useSSL=false&serverTimezone=CET");
        config.setUsername("chrispowell");
        config.setPassword("karabiner");
        // MySQL settings: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("cachePrepStmts", "true");                 // Only one PS here?
        config.addDataSourceProperty("prepStmtCacheSize", "100");               // Only one PS here?
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");          // Size of PS
        config.addDataSourceProperty("useServerPrepStmts", "true");             // Does this help?
        //
        config.setMaximumPoolSize(100);                                         // Default pool size for MySQL
        config.setConnectionTimeout(10000);
 
        connection = new HikariDataSource(config);
    }
    
    /**
     * Dump the TDIDF map.
     * 
     * @param tfidf 
     */
    public static void dumpTfidf( Map<Integer,List<LemmaScore>> tfidf )
    {
        tfidf.forEach((k,v) -> {
            System.out.println("DocId: " +k);
            v.forEach(l -> {
                System.out.println("   " +l.getScore());
            });
        });
    }
    
    /**
     * Dump TDF
     * @param tdf 
     */
    public static void dumpTdf( Map<Integer,List<LemmaScore>> tdf )
    {
        dumpTfidf(tdf);
    }
    
    /**
     * Dump IDF
     * @param idf
     */
    public static void dumpIdf( Map<String,Double> idf )
    {
        idf.forEach((k,v) -> {
            System.out.println("Key: " +k+ ", Val: " +v);
        });
    }
    
//    /**
//     * Turn the tfidf map into a vector.
//     * 
//     * @param maxDocId
//     * @param maxLemmaCount
//     * @param tfidf
//     * @return 
//     */
//    public static List<TfIdfVec> vectorise0( int maxDocId, int maxLemmaCount, Map<Map<String,Integer>,Double> tfidf )
//    {
//        // Total terms
//        int[] idx = new int[maxDocId];
//        double[] vecs = new double[maxLemmaCount];
//        List<TfIdfVec> lVecs = new ArrayList<>();
//
//        
//        System.out.println("TF-IDF build: (" +tfidf.size()+ "/" +maxLemmaCount+ ")");
//        for( Map.Entry<Map<String,Integer>,Double> m: tfidf.entrySet() )
//        {
//            for( Map.Entry<String,Integer> m1: m.getKey().entrySet() )
//            {
//                System.out.println("Term: " +m1.getKey()+ ", Doc num: " +m1.getValue()+ ", doc entry: " +(idx[m1.getValue()-1])+ ", value: " +m.getValue());
//                // vec[Doc][incremental count for that Doc]
////                vecs[m1.getValue()-1][idx[m1.getValue()-1]++] = m.getValue();
//                // (term/lemma, docId, docEntry, termCount?, value)
//                lVecs.add( new TfIdfVec(m1.getKey(),m1.getValue()-1,idx[m1.getValue()-1]++,0,m.getValue()) );
//            }
//        }
//        
////        return vecs;
//        return lVecs;
//    }


    
    /**
     * M A I N
     * =======
     * @param args
     * @throws SQLException 
     */
    static double tfidfMax = Double.MIN_VALUE;                      // Max. value tfidf score
    static double csimMax  = Double.MIN_VALUE;                      // Cos sim. max. (most similar)
    static double tfidfVal = Double.MIN_VALUE;                      // 
    static int totDocs = 0;
    public static void main(String[] args)
            throws SQLException
    {
        // TF0: Count/ratio of lemmas per doc <DocId,<lemma,count/docsize>>
        Map<Integer,List<LemmaScore>>       tf0 = new HashMap<>();
        // IDF: lemma,<Double> log(D/lemmadoccount)
        Map<String,Double>                  idf = new HashMap<>();
        // TFIDF: List of lemma scores per doc(Id)
        Map<Integer,List<LemmaScore>>       tfidf0 = new HashMap<>();
        // Count of non-null lemmas in a document
        Map<Integer,Integer>                totLcd = new HashMap<>();
        
        // Db connect
        Connection conn = connection.getConnection();
        
        // Normalize
        boolean norm021 = true;

        // Create statement
        Statement st = conn.createStatement();
        
        //... Max Doc ID (Note:, there may be "holes" after some docs removed)
        ResultSet allDocs = st.executeQuery("select max(dId) as td from Document");
        while( allDocs.next() )
        {
            totDocs = allDocs.getInt("td");
        }
        
        //... Largest document by lemma count.
        int maxLemmaCount = 0;
        ResultSet mlCnt = st.executeQuery("select max(lcnt) as mlcnt from (select dId,count(lemma) as lcnt from Document,Sentence,Token where docId=dId and sn=Sentence.id group by dId) as m");
        while( mlCnt.next() )
        {
            maxLemmaCount = mlCnt.getInt("mlcnt");
        }
        
        //... TF: Term Frequency
        // First, get count of terms (tokens/lemmas) per document
        ResultSet lcd = st.executeQuery("select dId,count(Token) as ct from Token,Sentence,Document where lemma != \"\" and Sentence.id=sn and docId=dId group by dId");
        while( lcd.next() )
        {
            totLcd.put(lcd.getInt("dId"), lcd.getInt("ct"));
        }
        
        // Second, get count per (distinct) term (token/lemma) in each document
        int cId = -1;
        ResultSet tfs = st.executeQuery("select lemma,dId,count(lemma) as cl from Token,Sentence,Document where lemma != \"\" and Sentence.id=sn and Sentence.docId=dId group by lemma,dId");
        while( tfs.next() )
        {
            List<LemmaScore> lls = new ArrayList<>();
            int dId = tfs.getInt("dId");
            String lemma = tfs.getString("lemma");
            double cl = (double)tfs.getInt("cl");
            if( cId != dId )
            {
                lls.add( new LemmaScore(lemma,cl) );
                tf0.put(dId, lls);
                cId = dId;
            }
            else
                tf0.get(dId).add(new LemmaScore(lemma,cl));
        }
//        dumpTfidf(tf0);
        
        //... IDF: Inverse Document Frequency
        // Count of documents in which term (lemma/token) appears
        ResultSet idfs = st.executeQuery("select lemma,count(dId) as d from Token,Sentence,Document where lemma!=\"\" and Sentence.id=sn and Sentence.docId=dId group by lemma");
        while( idfs.next() )
        {
            idf.put(idfs.getString("lemma"),(double)idfs.getInt("d"));
        }

        // Close database
        st.close();
        conn.close();
        
//        dumpTdf(tf0);
//        dumpIdf(idf);
        
        //----------------------------------------------------------------------
        // TF-IDF (per term/lemma)
        for( Map.Entry<Integer,List<LemmaScore>> tf1: tf0.entrySet() )
        {
            // Write tfidf value
            tf1.getValue().forEach(l -> {
                // TF*IDF algo
                tfidfVal = l.getScore()*Math.log10((double)totDocs/idf.get(l.getTerm()));
                l.setScore(tfidfVal);

                // Check for max. value (for normalization)
                if( tfidfMax < tfidfVal )
                    tfidfMax = tfidfVal;
            });

            // TF-IDF
            tfidf0.put(tf1.getKey(), tf1.getValue());
        }

        // Normalise to [0..1]
        if( norm021 )
        {
            tfidf0.forEach((k,v) -> {
                // Update values
                v.forEach(v1 -> {
                    double csim = v1.getScore()/tfidfMax;
                    v1.setScore(csim);
                
                    // Check for max. value (most similar)
                    if( csim > csimMax )
                        csimMax = csim;
                });
            });
        }
        
//        dumpTfidf(tfidf0);
//        DocSimilarity.dumpCosineSimilarity(totDocs, tfidf0);
       
        // Form pairs score
        List<Pair> lpair = new ArrayList<>();
        int minDoc = Collections.min(tfidf0.keySet()),
            maxDoc = Collections.max(tfidf0.keySet());
        for( int ii=minDoc; ii<=maxDoc; ii++ )
        {
            for( int jj=ii+1; jj<=maxDoc; jj++ )
            {
                // Beware gaps in the map (removed docs)
                if( tfidf0.containsKey(ii) && tfidf0.containsKey(jj) )
                {
                    double score = Utils.cosineSimilarity(LemmaScore.toScoreArray(tfidf0.get(ii)), LemmaScore.toScoreArray(tfidf0.get(jj)));
                    lpair.add(new Pair(ii,jj,score));
                }
            }
        }
        
//        lpair.forEach(p -> {
//            System.out.println("> " +p);
//        });

        // Cluster
        List<Pair> redPairs = lpair.stream().filter(p0 -> p0.getCosScore() > 0.25d).collect(Collectors.toList());
        for( Iterator<Pair> iter = redPairs.iterator(); iter.hasNext(); )
        {
            Pair minPair = Collections.min(redPairs);
            redPairs.remove(minPair);
            
            // Put into binary tree
            System.out.println(">> " +minPair);
        }

    }
}


//==============================================================================
class Pair implements Comparable<Pair>
{
    private final int     doc0, doc1;
    private final double  cosScore;

    /**
     * Constructor.
     * @param doc0
     * @param doc1
     * @param cosScore 
     */
    public Pair(int doc0, int doc1, double cosScore)
    {
        this.doc0 = doc0;
        this.doc1 = doc1;
        this.cosScore = cosScore;
    }

    public int getDoc0() { return doc0; }
    public int getDoc1() { return doc1; }
    public double getCosScore() { return cosScore; }
    
    @Override
    public int compareTo( Pair p )
    {
        if( getCosScore() > p.getCosScore() )
            return 1;
        if( getCosScore() < p.getCosScore() )
            return -1;
        
        return 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 23 * hash + this.doc0;
        hash = 23 * hash + this.doc1;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final Pair other = (Pair) obj;
        if( this.doc0 != other.doc0 ) { return false; }
        if( this.doc1 != other.doc1 ) { return false; }
        
        return true;
    }

    @Override
    public String toString()
    {
        return "[" +doc0+ "]:[" +doc1+ "]> " +cosScore;
    }
}


//class TfIdfVec
//{
//    private final String  term;
//    private final int     docId;
//    private final int     docEntry;
//    private int           termCount;
//    private double        value;
//    
//    /**
//     * Constructor.
//     * 
//     * @param term
//     * @param docId
//     * @param docEntry
//     * @param termCount
//     * @param value 
//     */
//    public TfIdfVec( String term, int docId, int docEntry, int termCount, double value )
//    {
//        this.term = term;
//        this.docId = docId;
//        this.docEntry = docEntry;
//        this.termCount = termCount;
//        this.value = value;
//    }
//    
//    /**
//     * Mutators.
//     * @param termCount 
//     */
//    public void setTermCount( int termCount ) { this.termCount = termCount; }
//    public void setValue( double value ) { this.value = value; }
//
//    public String getTerm() { return term; }
//    public int getDocId() { return docId; }
//    public int getDocEntry() { return docEntry; }
//    public int getTermCount() { return termCount; }
//    public double getValue() { return value; }
//}

class Node
{
    private Node    left, right;
    private Pair    nodeVal;
    
    public Node( Pair nodeVal )
    {
        this.nodeVal = nodeVal;
        left = null; right = null;
    }

    public Node getLeft() { return left; }
    public Node getRight() { return right; }

    public void setLeft(Node left) { this.left = left; }
    public void setRight(Node right) { this.right = right; }

    public Pair getNodeVal() { return nodeVal; }
}

class BinaryTree
{
    private Node    root;
    
    public void add( Pair p )
    {
        root = addRecursive( root, p );
    }
    
    private Node addRecursive( Node current, Pair p )
    {
        if( current == null )
            return new Node(p);
        
        // p < current
        if( p.compareTo(current.getNodeVal()) == -1 )
            current.setLeft( addRecursive(current.getLeft(),p) );

        // p > current
        if( p.compareTo(current.getNodeVal()) == 1 )
            current.setRight( addRecursive(current.getRight(),p) );
        
        return current;
    }
    
    public boolean isEmpty() { return root == null; }
    
    public int getSize() { return getSizeRecursive(root); }

    private int getSizeRecursive(Node current)
    {
        return current == null ? 0 : getSizeRecursive(current.getLeft()) + 1 + getSizeRecursive(current.getRight());
    }

    public boolean containsNode(Pair p) { return containsNodeRecursive(root, p); }
    
    private boolean containsNodeRecursive(Node current, Pair p)
    {
        if( current == null ) { return false; }
        if( p.equals(current.getNodeVal()) ) { return true; }

        return p.compareTo(current.getNodeVal()) == -1
          ? containsNodeRecursive(current.getLeft(), p)
          : containsNodeRecursive(current.getRight(), p);
    }
}