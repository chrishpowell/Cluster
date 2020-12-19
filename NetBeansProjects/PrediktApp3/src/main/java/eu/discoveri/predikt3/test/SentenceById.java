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
    
    /**
     * Enter up ps variables to get list of Sentences.
     * @param ps
     * @param pageSiz size of list of sentences
     * @param offset start of 'next' set of sentences
     * @return
     * @throws SQLException 
     */
    private static List<SentenceNode> sentenceById( PreparedStatement ps, int pageSiz, long offset )
            throws SQLException
    {
        List<SentenceNode> lsSN = new ArrayList<>();

        // Enter the PS variables
        for( int ii=1; ii<=pageSiz; ii++ )
            ps.setLong(ii, offset+ii-1);

        // Get the relevant rows
        ResultSet res = ps.executeQuery();
        while( res.next() )
        {
            SentenceNode sn = new SentenceNode(res.getInt("id"),"",res.getString("sentence"));
            sn.setSentenceClusterNum(new SentenceClusterNum(res.getInt("clusterNum")));
            lsSN.add(sn);
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
        // Start/Last sentence ids
        int sid = -1, lid = -1;
        int PAGSIZ = 4;
        
        Connection conn = connection.getConnection();
        Statement st = conn.createStatement();
        
        // Sentence/lemma count
        ResultSet sents = st.executeQuery("select min(id) as sid, max(id) as lid from Sentence");
        while( sents.next() )
        {
            sid = sents.getInt("sid");
            lid = sents.getInt("lid");
        }
        System.out.println("Sentence count (assuming no gaps in start/end ids): " +(lid-sid+1));
        
        // Form statement
        PreparedStatement ps = ps4SentenceById(conn,PAGSIZ);
        
        System.out.println("> *Unique* pairs from list size: " +(lid-sid+1)+ " in groups of: " +PAGSIZ+ " delimited by []\r\n");
        
        //......................................................................
        for( int ii=sid; ii<=lid; ii+=PAGSIZ )                                  // ii = 1, 5, 9
        {
                List<SentenceNode> lsnP = sentenceById(ps,PAGSIZ,ii);           // P = 1234, 5678, 9abc, ...

            // Compare sentences
            for( SentenceNode sP: lsnP )
            {
                for( int rr=sP.getNid()+1; rr<=lid; rr+=PAGSIZ )                // [1:(2345); 1:(6789); 1:(abcd) .. 1:(yz)] [2:(3456); ...]
                {
                    List<SentenceNode> lsnQ = sentenceById(ps,PAGSIZ,rr);       // Q = [2345, 6789, abcd, .. (..sentCount)] [3456, 789a, ...]

                    System.out.print("[");
                    for( SentenceNode sQ: lsnQ )
                    {
                        System.out.print(" {" +sP.getNid()+":"+sQ.getNid() +"}");
                    }
                    System.out.println(" ]");
                }
            }
            System.out.println("");
        }
        //......................................................................
    }
}
