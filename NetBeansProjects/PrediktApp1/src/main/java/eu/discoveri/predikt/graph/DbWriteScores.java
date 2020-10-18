/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import com.zaxxer.hikari.HikariDataSource;
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
    private final FakeQRscoreCW     qrscw;
    
    /**
     * Constructor.
     * @param tnum Thread num.
     * @param connection
     * @param qrscw
     */
    public DbWriteScores( int tnum, HikariDataSource connection, FakeQRscoreCW qrscw )
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
        // (Not try-with-resources as we want conn in catch clause)
        try
        {
            conn = connection.getConnection();
            conn.setAutoCommit(false);

            // Create QRscoreCW entry and get the id
            PreparedStatement ps1 = conn.prepareStatement("insert into QRscoreCW values(default,default,?,?)",Statement.RETURN_GENERATED_KEYS);
            ps1.setInt(1, qrscw.getNodeQ().getId());
            ps1.setInt(2, qrscw.getNodeR().getId());
            ps1.execute();

            // The id
            int qrscwId = 0;
            ResultSet rs = ps1.getGeneratedKeys();
            if( rs.next() )
                qrscwId = rs.getInt(1);
            
            // Now for the list of scores across all matched words of the two sentences
            PreparedStatement ps2 = conn.prepareStatement("insert into CWcount values(default,?,?,?,?)");
            for( FakeCWcount cw: qrscw.getCwCount() )
            {
                ps2.setString(1,cw.getMatchedWord());
                ps2.setInt(2,cw.getCountQ());
                ps2.setInt(3,cw.getCountR());
                ps2.setInt(4,qrscwId);
                ps2.execute();
            }

            conn.commit();
            conn.close();
        }
        catch( SQLException sex )
        {
            System.out.println("FAILED " +sex.getMessage());
            try
            {
                if( !conn.isClosed() )
                    conn.rollback();
            }
            catch( SQLException sex2 )
            {
                System.out.println("FAILED, Connection closed, " +sex2.getSQLState());
                sex2.printStackTrace();
            }
            finally
            {
                conn.close();
            }
        }

        return tnum;
    }
}
