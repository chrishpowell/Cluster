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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class PageById
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
    
    private static Map<String,Integer> lemmaSentCount( Connection conn )
            throws SQLException
    {
        System.out.println("Lemma sentence count:");
        Map<String,Integer> lemmaSentCount = new HashMap<>();

        PreparedStatement tok = conn.prepareStatement("select token,count(distinct(sn)) as sc from Token,CWcount where token=matchWord group by matchWord");
        ResultSet toks = tok.executeQuery();
        while( toks.next() )
            { lemmaSentCount.put(toks.getString("token"),toks.getInt("sc")); }
        
        return lemmaSentCount;
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
        int sentCount=-1, lemmaSentCount=-1;
        
        Connection conn = connection.getConnection();
        Statement st = conn.createStatement();
        
        // Sentence/lemma count
        ResultSet sents = st.executeQuery("select count(*) as sc from Sentence");
        while( sents.next() ) { sentCount = sents.getByte("sc"); }
        Map<String,Integer> lsc = lemmaSentCount(conn);
        
        // Start/End ids
        ResultSet idSt = st.executeQuery("select min(qrscwId) idBegin from CWcount");
        while( idSt.next() ) { idBegin = idSt.getInt("idBegin"); }
        ResultSet idEn = st.executeQuery("select max(qrscwId) idEnd from CWcount");
        while( idEn.next() ) { idEnd = idEn.getInt("idEnd"); }
        System.out.println("" +idBegin+ ":" +idEnd);
        
        PreparedStatement ps = conn.prepareStatement("select matchWord,countQ,countR,qrscwId from CWcount where qrscwId=? order by qrscwId");
        PreparedStatement up = conn.prepareStatement("update QRscoreCW set score = ? where id = ?");
        for( long id=idBegin; id<=idEnd; id++ )
        {
            double score = 0.d;

            ps.setLong(1, id);
            ResultSet res = ps.executeQuery();
            while( res.next() )
            {
                String mw = res.getString("matchWord");
                System.out.println("> [" +res.getInt("qrscwId")+"] "+mw+"/"+res.getLong("countQ")+":"+res.getInt("countR"));
                score += Math.log(res.getInt("countQ")+1.d)*Math.log(res.getInt("countR")+1.d)*Math.log((sentCount+1.d)/(lsc.get(mw)+0.5d));
            }
            
            System.out.println(">> " +score);
            System.out.println("");
            
            // Update the score
            up.setDouble(1,score);
            up.setLong(2,id);
            up.execute();
        }
    }
}
