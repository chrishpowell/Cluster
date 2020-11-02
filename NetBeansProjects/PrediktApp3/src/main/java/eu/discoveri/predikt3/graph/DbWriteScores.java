/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;

import com.zaxxer.hikari.HikariDataSource;
import eu.discoveri.predikt3.cluster.CWcount;
import eu.discoveri.predikt3.cluster.QRscoreCW;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;


/**
 * Write scores (qrscw) in a thread.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbWriteScores implements Callable
{
    private final int               tnum;
    private final HikariDataSource  connection;
    private final QRscoreCW         qrscw;
    
    /**
     * Constructor.
     * @param tnum Thread num.
     * @param connection
     * @param qrscw
     */
    public DbWriteScores( int tnum, HikariDataSource connection, QRscoreCW qrscw )
    {
        this.tnum = tnum;
        this.connection = connection;
        this.qrscw = qrscw;
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
            
            // Create QRscoreCW entry and get the id
            PreparedStatement ps1 = conn.prepareStatement("insert into QRscoreCW values(default,default,?,?)",Statement.RETURN_GENERATED_KEYS);
            ps1.setLong(1, qrscw.getNodeQ().getNid());
            ps1.setLong(2, qrscw.getNodeR().getNid());
            ps1.execute();
            //The id
            int qrscwId = 0;
            ResultSet rs = ps1.getGeneratedKeys();
            if( rs.next() )
                qrscwId = rs.getInt(1);
            
            // Now for the list of scores across all matched words of the two sentcountences
            PreparedStatement ps2 = conn.prepareStatement("insert into CWcount values(default,?,?,?,?)");
            for( CWcount cw: qrscw.getCwCount() )
            {
                ps2.setString(1,cw.getMatchedWord());
                ps2.setInt(2,cw.getCountQ());
                ps2.setInt(3,cw.getCountR());
                ps2.setInt(4,qrscwId);
                ps2.execute();
            }

            conn.commit();
            ps1.close();
            ps2.close();
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED! DbWriteScores: " +sex.getSQLState()+"/"+sex.getLocalizedMessage());
            try
            {
                if( conn != null && !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED! DbWriteScores: Connection closed, " +sex2.getSQLState()+"/"+sex2.getLocalizedMessage());
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
