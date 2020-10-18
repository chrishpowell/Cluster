/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class GenTestToks
{
    /**
     * Get a pooled connection to MySQL.
     * @return 
     */
    public static HikariDataSource getPooledConnection()
    {
        HikariDataSource connection = null;

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
 
        return new HikariDataSource(config);
    }
    
    public static void main(String[] args)
            throws SQLException
    {
        Connection conn = getPooledConnection().getConnection();
        PreparedStatement sent = conn.prepareStatement("select id, sentence from Sentence");
        PreparedStatement tok = conn.prepareStatement("insert into Token values(default,?,default,default,default,?)");
        
        ResultSet res = sent.executeQuery();
        while( res.next() )
        {
            SentenceNode2 sn = new SentenceNode2(res.getInt(1),res.getString(2));
            for( Token2 t: sn.getTokens() )
            {
                tok.setString(1,t.getToken());
                tok.setInt(2, sn.getId());
                tok.execute();
            }
        }
    }
}

/*
 * Sentence
 * --------
 */
class SentenceNode2 implements Comparator<SentenceNode2>, Comparable<SentenceNode2>
{
    private final int           id;
    private final String        sentence;
    private final List<Token2>  lt = new ArrayList<>();

    public SentenceNode2(int id, String sentence)
    {
        this.id = id;
        this.sentence = sentence;
        List<String> ls = Arrays.asList(sentence.split("\\s*[ ]\\s*"));
        ls.forEach(s -> {
            lt.add(new Token2(s,s));
        });
    }

    public int getId() { return id; }
    public String getSentence() { return sentence; }
    public List<Token2> getTokens(){ return lt; }
    
    /**
     * Comparator: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param s1
     * @param s2
     * @return 
     */
    @Override
    public int compare(SentenceNode2 s1, SentenceNode2 s2)
    {
        return s1.lt.size() - s2.lt.size();
    }
    
    /**
     * Comparable: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param other
     * @return 
     */
    @Override
    public int compareTo(SentenceNode2 other)
    {
        return this.lt.size() - other.lt.size();
    }
    
    /**
     * Equals.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        final SentenceNode2 other = (SentenceNode2) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        return hash;
    }
}

/*
 * Token
 * -----
 */
class Token2
{
    private final String  token;
    private final String  lemma;

    public Token2(String token, String lemma)
    {
        this.token = token;
        this.lemma = lemma;
    }

    public String getToken() { return token; }
    public String getLemma() { return lemma; }
}
