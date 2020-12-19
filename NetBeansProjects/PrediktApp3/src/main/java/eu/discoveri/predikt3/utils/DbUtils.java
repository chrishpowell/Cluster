/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt3.cluster.ClusterDocSent;

import eu.discoveri.predikt3.config.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbUtils
{
    /**
     * Get a pooled connection to MySQL Document Db.
     * @return 
     */
    public static HikariDataSource getPooledDocDbConnection()
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
        // Connection pool size for MySQL (mysql>set global Max_connections=#)
        config.setMaximumPoolSize(Constants.MAXCONNECTIONS);
        config.setConnectionTimeout(Constants.TIMEOUTMS);
 
        return new HikariDataSource(config);
    }
    
        /**
     * Get a pooled connection to MySQL Document Db.
     * @return 
     */
    public static HikariDataSource getPooledLemmaDbConnection()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/lemma?useSSL=false&serverTimezone=CET");
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
    
    
    /**
     * Get count of rows in given table of 'documents' db.
     * 
     * @param conn
     * @param tableName
     * @return
     * @throws SQLException 
     */
    public static int countDocumentsDbTableRows( Connection conn, String tableName )
            throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) as count from documents." +tableName);
        
        while( rs.next() )
            { return rs.getInt("count"); }
        
        return -1;
    }

    /**
     * Empty given table of 'documents' db.
     * 
     * @param conn
     * @param tableName
     * @throws SQLException 
     */
    public static void emptyDocumentsTable( Connection conn, String tableName )
            throws SQLException
    {
        // Empty the table
        PreparedStatement empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
        empty.executeUpdate();
        empty = conn.prepareStatement("truncate documents." +tableName);
        empty.executeUpdate();
        empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1");
        empty.executeUpdate();
    }
    
    /**
     * Empty sentence related tables (Sentence,Token,CWcount,QRscoreCW) and set
     * Sentence id to start at zero (not default 1).
     * @param conn
     * @throws SQLException 
     */
    public static void emptySentenceTables(Connection conn)
            throws SQLException
    {
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
    }
    
        
    /**
     * Count of 'useful' clusters and total sentences: Clusters with more than two sentences.
     * @param conn
     * @return [0]: Clusters count, [1]: Sentences count
     * @throws java.sql.SQLException 
     */
    public static int[] usefulClusters( Connection conn )
            throws SQLException
    {
        int counts[] = new int[2];
        Statement st = conn.createStatement();

        ResultSet numClusters = st.executeQuery("select sum(sc) as ssc, count(*) as scc from (select count(*) as sc from Sentence group by clusterNum having clusterNum > -1 and sc >= " +Constants.SMALLESTCLUSTER+ ") as c");
        while( numClusters.next() )
        {
            counts[0] = numClusters.getInt("ssc");  // Sentences
            counts[1] = numClusters.getInt("scc");  // Clusters
        }
        
        return counts;
    }
    
    /**
     * Documents and sentences of cluster.
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    public static List<ClusterDocSent> clusterDocSent( Connection conn )
            throws SQLException
    {
        List<ClusterDocSent> lcds = new ArrayList<>();
        Statement st = conn.createStatement();
        
        ResultSet rs = st.executeQuery("select clusterNum,docId,id as nid from Sentence group by clusterNum,docId,id having clusterNum > -1 order by clusterNum,docId");
        while( rs.next() )
        {
            lcds.add( new ClusterDocSent(rs.getInt("clusterNum"),rs.getInt("docId"),rs.getInt("nid")) );
        }
        
        return lcds;
    }
    
    
    /**
     * M A I N
     * =======
     * Clear the sentences of documents db.
     * @param args 
     * @throws java.sql.SQLException 
     */
    public static void main(String[] args)
            throws SQLException
    {
        // Connect
        HikariDataSource hds = getPooledDocDbConnection();
        Connection conn = hds.getConnection();
        
        // Empty tables
        PreparedStatement empty = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
        empty.executeUpdate();
        empty = conn.prepareStatement("set sql_mode='NO_AUTO_VALUE_ON_ZERO'");
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
    }
}
