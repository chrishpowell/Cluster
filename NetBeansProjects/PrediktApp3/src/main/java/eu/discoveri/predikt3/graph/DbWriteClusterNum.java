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
 * Write cluster num (qrscw) in a thread.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbWriteClusterNum implements Callable
{
    private final int               tnum;
    private final HikariDataSource  connection;
    private final int               lIdx;
    private final long              id;
    
    /**
     * Constructor.
     * @param tnum Thread num.
     * @param connection
     * @param lIdx
     * @param id
     */
    public DbWriteClusterNum( int tnum, HikariDataSource connection, int lIdx, long id )
    {
        this.tnum = tnum;
        this.connection = connection;
        this.id = id;
        this.lIdx = lIdx;
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
            
            // Update Sentence
            PreparedStatement up = conn.prepareStatement("update Sentence set louvainIdx = ? where id = ?");
            up.setDouble(1, lIdx);
            up.setLong(2, id);
            up.execute();

            conn.commit();
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED " +sex.getMessage());
            try
            {
                if( conn != null && !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED, Connection closed, " +sex2.getSQLState());
                sex2.printStackTrace();
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
