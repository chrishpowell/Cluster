/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.graph;

import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt3.sentences.Token;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;


/**
 *
 * @author chrispowell
 */
public class DbWriteSentenceTokens implements Callable
{
    private final int               tnum;
    private final HikariDataSource  connection;
    private final List<Token>       tokens;
    private final int               id;
    
    
    public DbWriteSentenceTokens( int tnum, HikariDataSource connection, List<Token> tokens, int id )
    {
        this.tnum = tnum;
        this.connection = connection;
        this.tokens = tokens;
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
            
            // Update Sentence table with tokens
            try ( PreparedStatement ins = conn.prepareStatement("insert into documents.Token values(default,?,?,?,?,?)") )
            {
                for( Token t: tokens )
                {
                    ins.setString(1, t.getToken());
                    ins.setString(2, t.getStem());
                    ins.setString(3, t.getLemma());
                    ins.setString(4, t.getPOS());
                    ins.setInt(5, id);
                    ins.execute();
                }
                
                conn.commit();
            }
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED! DbWriteClusterNum: " +sex.getSQLState()+"/"+sex.getLocalizedMessage());
            try
            {
                if( conn != null && !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED! DbWriteClusterNum: Connection closed, " +sex2.getSQLState()+"/"+sex2.getLocalizedMessage());
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
