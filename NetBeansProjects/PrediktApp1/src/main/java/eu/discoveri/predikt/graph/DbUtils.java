/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbUtils
{
    /**
     * Get count of rows in given table of 'documents' db.
     * 
     * @param conn
     * @param tableName
     * @return
     * @throws SQLException 
     */
    public static int countDocumentsTableRows( Connection conn, String tableName )
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
}
