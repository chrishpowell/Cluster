/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.discoveri.predikt3.graph.SentenceNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author chrispowell
 */
public class SentenceById
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

    private static sentenceById( PreparedStatement ps, long offset )
    {
        int PAGESIZ = 4;
        List<SentenceNode> lsSN = new ArrayList<>();
        for( long id=offset; id<=offset+PAGESIZ-1; id+=PAGESIZ )
        {
            for( int ii=1; ii<=PAGESIZ; ii++ )
                ps.setLong(ii, id+ii-1);

            ResultSet res = ps.executeQuery();
            while( res.next() )
            {
                    { ltkn.add(new Token(restk.getString("token"),restk.getString("lemma"))); }
                lsSN.add(new SentenceNode(ress.getInt("nid"),"",ress.getString("sentence"),ltkn));
            }
        }
    }
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
            throws SQLException
    {
        long idBegin=-1, idEnd=-1;
        int sentCount=-1;
        int PAGESIZ = 4;
        
        Connection conn = connection.getConnection();
        Statement st = conn.createStatement();
        
        // Sentence/lemma count
        ResultSet sents = st.executeQuery("select count(*) as sc from Sentence");
        while( sents.next() ) { sentCount = sents.getInt("sc"); }
        System.out.println("Sentence count: " +sentCount);
        
        // Start/End ids
//        ResultSet idSt = st.executeQuery("select min(qrscwId) idBegin from CWcount");
//        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
//        ResultSet idEn = st.executeQuery("select max(qrscwId) idEnd from CWcount");
//        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
//        System.out.println("" +idBegin+ ":" +idEnd);
        
        // Form statement
        String sql = "select id,score,clusterNum from Sentence where id in (";
        for( int ii=1; ii<=PAGESIZ-1; ii++ )
            sql += "?,";
        sql += "?) order by id";
        PreparedStatement ps = conn.prepareStatement(sql);
        
        // Get pages of Sentences
        for( int ii=1; ii<sentCount; ii+=PAGESIZ )
        {
            List<SentenceNode> lsn = sentenceById(ps,offset);
            lsn.forEach(s -> {
                System.out.println("> [" +s.getNid()+"]: ("+s.getScore()+"), C: "+s.getSentenceClusterNum().);
            });
        }
    }
}
