/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.documents.DocumentDb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SentenceAccess
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
        config.setMaximumPoolSize(100);
        config.setConnectionTimeout(3000);
 
        connection = new HikariDataSource(config);
    }
    
    public static void main(String[] args)
            throws Exception
    {
        Connection conn = connection.getConnection();
        int docCnt = DbUtils.countDocumentsTableRows( conn, "Document" );
        System.out.println("Doc. count: " +docCnt);
        
        PreparedStatement ps = conn.prepareStatement("select sentence from Sentence limit ?,?");
        ps.setInt(1,2); ps.setInt(2,4);
        ResultSet rs = ps.executeQuery();
        while( rs.next() )
        {
            System.out.println(">> " +rs.getString("sentence"));
        }
    }
}
