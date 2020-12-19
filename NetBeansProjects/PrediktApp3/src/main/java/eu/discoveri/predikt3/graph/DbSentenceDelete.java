/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.graph;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;


/**
 *
 * @author chrispowell
 */
public class DbSentenceDelete implements Callable
{
    private final int               tnum;
    private final HikariDataSource  connection;
    private final int               id;

    public DbSentenceDelete(int tnum, HikariDataSource connection, int id)
    {
        this.tnum = tnum;
        this.connection = connection;
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
            
            // Delete duff Sentence
            try ( PreparedStatement del = conn.prepareStatement("delete from Sentence where id = ?") )
            {
                del.setInt(1,id);
                del.execute();
                
                conn.commit();
            }
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED! DbSentenceDelete: " +sex.getSQLState()+"/"+sex.getLocalizedMessage());
            try
            {
                if( conn != null && !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED! DbSentenceDelete: Connection closed, " +sex2.getSQLState()+"/"+sex2.getLocalizedMessage());
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
