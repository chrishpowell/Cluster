/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt.graph.SentenceNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class SentenceAtZero
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
     * Persist a list of sentences.  Auto-commit per sentence.
     * @param conn
     * @param lsn
     * @throws java.sql.SQLException
     */
    public static void persistListSentences( Connection conn, List<SentenceNode> lsn )
            throws SQLException
    {
        // Prepare.
        PreparedStatement sent = conn.prepareStatement("insert into documents.Sentence values(default,?,?,?,?,?,?,default)");
        
        for( SentenceNode sn: lsn )
        {
            sent.setString(1, sn.getSentence());
            sent.setString(2, sn.getLangCode().name());
            sent.setString(3, sn.getLocale().toString());
            sent.setDouble(4, sn.getScore());
            sent.setDouble(5, sn.getPrevScore());
            sent.setInt(6,0);
            sent.execute();
        }
    }
    
    /**
     * Persist sentence with Id zero.Auto-committed.
     * @param conn
     * @param sn
     * @throws java.sql.SQLException
     */
    public static void persistFirstSentenceZero( Connection conn, SentenceNode sn )
            throws SQLException
    {
        Statement st = conn.createStatement();
        st.execute("set sql_mode='NO_AUTO_VALUE_ON_ZERO'");

        // Prepare.
        PreparedStatement sent = conn.prepareStatement("insert into documents.Sentence values(0,?,?,?,?,?,?,default)");

        sent.setString(1, sn.getSentence());
        sent.setString(2, sn.getLangCode().name());
        sent.setString(3, sn.getLocale().toString());
        sent.setDouble(4, sn.getScore());
        sent.setDouble(5, sn.getPrevScore());
        sent.setInt(6,0);
        sent.execute();
    }
    
    public static void main(String[] args)
            throws SQLException
    {
        List<SentenceNode> lsn = List.of(new SentenceNode("", "The quick brown fox jumps!"));
        SentenceNode sn = new SentenceNode("S0","The quick brown fox jumps!");
        Connection conn = connection.getConnection();

        PreparedStatement empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.Sentence");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.Token");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.CWcount");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents.QRscoreCW");
        empty.executeUpdate();
        empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1");
        empty.executeUpdate();

        persistFirstSentenceZero(conn,sn);
        persistListSentences(conn,lsn);
        persistListSentences(conn,lsn);
    }
}
