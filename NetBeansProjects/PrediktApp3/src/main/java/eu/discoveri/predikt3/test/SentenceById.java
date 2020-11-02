/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt3.cluster.SentenceClusterNum;
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

    /**
     * Form prepared statement for getting sentenceById.
     * @return 
     */
    private static PreparedStatement ps4SentenceById(Connection conn,int pageSiz)
            throws SQLException
    {
        String sql = "select id,sentence,clusterNum from Sentence where id in (";
        for( int ii=1; ii<=pageSiz-1; ii++ )
            sql += "?,";
        sql += "?) order by id";
        
        return conn.prepareStatement(sql);
    }
    
    private static List<SentenceNode> sentenceById( PreparedStatement ps, int pageSiz, long offset )
            throws SQLException
    {
        List<SentenceNode> lsSN = new ArrayList<>();
        for( long id=offset; id<=offset+pageSiz-1; id+=pageSiz )
        {
            for( int ii=1; ii<=pageSiz; ii++ )
                ps.setLong(ii, id+ii-1);

            ResultSet res = ps.executeQuery();
            while( res.next() )
            {
                SentenceNode sn = new SentenceNode(res.getInt("id"),"",res.getString("sentence"));
                sn.setSentenceClusterNum(new SentenceClusterNum(res.getInt("clusterNum")));
                lsSN.add(sn);
            }
        }
        
        return lsSN;
    }
    /**
     * M A I N
     * =======
     * @param args 
     * @throws java.sql.SQLException 
     */
    public static void main(String[] args)
            throws SQLException
    {
        int sentCount=-1;
        int PAGESIZ = 4;
        
        Connection conn = connection.getConnection();
        Statement st = conn.createStatement();
        
        // Sentence/lemma count
        ResultSet sents = st.executeQuery("select count(*) as sc from Sentence");
        while( sents.next() ) { sentCount = sents.getInt("sc"); }
        System.out.println("Sentence count: " +sentCount);
        
        // Form statement
        PreparedStatement ps = ps4SentenceById(conn,PAGESIZ);
        
        // Get pages of Sentences
        for( int ii=1; ii<=sentCount; ii+=PAGESIZ )
        {
            List<SentenceNode> lsn = sentenceById(ps,PAGESIZ,ii);
            lsn.forEach(s -> {
                System.out.println("> [" +s.getNid()+"]: ("+s.getSentence()+"), C: "+s.getSentenceClusterNum().getClusterNum());
            });
        }
    }
}
