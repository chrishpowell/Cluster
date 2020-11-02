/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;


/**
 * Write scores (qrscw) in a thread.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbWriteSimilarity implements Callable
{
    private final int               tnum;
    private final HikariDataSource  connection;
    private final double            score;
    private final long              id;
    
    /**
     * Constructor.
     * @param tnum Thread num.
     * @param connection
     * @param score
     * @param id
     */
    public DbWriteSimilarity( int tnum, HikariDataSource connection, double score, long id )
    {
        this.tnum = tnum;
        this.connection = connection;
        this.score = score;
        this.id = id;
    }
    
    /**
     * Do the deed.
     */
    @Override
    public Integer call()
            throws Exception
    {
        Connection conn = null;
        try
        {
            conn = connection.getConnection();
            conn.setAutoCommit(false);
            
            // Update QRscoreCW score
            PreparedStatement up = conn.prepareStatement("update QRscoreCW set score = ? where id = ?");
            up.setDouble(1, score);
            up.setLong(2, id);
            up.execute();

            conn.commit();
            up.close();
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED! DbWriteSimilarity: " +sex.getSQLState()+"/"+sex.getLocalizedMessage());
            try
            {
                if( conn != null && !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED! DbWriteSimilarity: Connection closed, " +sex2.getSQLState()+"/"+sex2.getLocalizedMessage());
            }
            finally
            {
                if( conn != null )
                    conn.close();
            }
        }
        
        return tnum;
    }
}
