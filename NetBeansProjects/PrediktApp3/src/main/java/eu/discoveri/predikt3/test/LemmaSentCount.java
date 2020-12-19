/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class LemmaSentCount
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
        Connection conn = connection.getConnection();
        Map<String,Integer> lemmaSentCount = new HashMap<>();
        Instant start = Instant.now();
        System.out.println("Start datetime: " +start.toString());
        
        PreparedStatement sCount = conn.prepareStatement("select count(distinct(sn)) as sc from Token where lemma=?");
        Statement st = conn.createStatement();
        ResultSet lemmas = st.executeQuery("select matchWord from CWcount group by matchWord");
        while( lemmas.next() )
        {
            String lemma = lemmas.getString("matchWord");
            sCount.setString(1, lemma);
            ResultSet toks = sCount.executeQuery();
            while( toks.next() )
                { lemmaSentCount.put(lemma,toks.getInt("sc")); }
        }

        System.out.println("  ..> Lemma sentence count: " +lemmaSentCount.size()+ " complete (secs): " +Duration.between(start,Instant.now()).toSeconds());
    }
}
