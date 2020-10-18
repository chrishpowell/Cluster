/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.sentences.LangCode;
import eu.discoveri.predikt.sentences.Token;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author chrispowell
 */
public class PageProduct
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
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
            throws SQLException
    {
        System.out.println("--Enum test--> " +LangCode.valueOf("en"));
        
        
        Connection conn = connection.getConnection();
//        Statement st = conn.createStatement();
//        ResultSet sents = st.executeQuery("select Sentence.id as nid, sentence, langCode, locale, token, lemma, pos from Sentence,Token where sn=Sentence.id and sn in (1,2,3,4)");
//        while( sents.next() )
//        {
//            System.out.println(""+sents.getInt("nid")+"|"+sents.getString("sentence")+"|"+sents.getString("token"));
//        }
        PreparedStatement sents = conn.prepareStatement("select Sentence.id as nid, sentence, langCode, locale, token, lemma, pos,sn from Sentence,Token where sn=Sentence.id and sn in (?,?,?,?)",
                                        ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.FETCH_UNKNOWN);
        sents.setInt(1,1);
        sents.setInt(2,2);
        sents.setInt(3,3);
        sents.setInt(4,4);
        ResultSet res = sents.executeQuery();
        
        List<Token> lt = new ArrayList<>();
        while( res.next() )
        {
            System.out.println("   "+res.getInt("sn")+":"+res.getString("token"));
            lt.add( new Token(res.getString("token"),res.getString("lemma")) );
        }
        res.first();
        SentenceNode snode = new SentenceNode("S1", res.getString("sentence"), LangCode.valueOf(res.getString("langCode")), new Locale(res.getString("locale")), lt, 0.0);
        
        snode.dumpTokens();
        
        conn.close();
    }
}
