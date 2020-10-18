/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DocumentDb
{
    private static final String USER = "chrispowell";
    private static final String PWD = "karabiner";
    
    /**
     * Connection for Lemma db
     * @return
     * @throws SQLException 
     */
    public static Connection lemmaDb()
            throws SQLException
    {
        String URL = "jdbc:mysql://localhost:3306/lemma?useSSL=false&serverTimezone=CET";
        return DriverManager.getConnection(URL,USER,PWD);
    }
    
    /**
     * Connection for documents db
     * @return
     * @throws SQLException 
     */
    public static Connection docDb()
            throws SQLException
    {
        String URL = "jdbc:mysql://localhost:3306/documents?useSSL=false&serverTimezone=CET";
        return DriverManager.getConnection(URL,USER,PWD);
    }

}
