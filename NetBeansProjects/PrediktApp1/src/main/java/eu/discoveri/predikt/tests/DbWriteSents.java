/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.sql.PreparedStatement;
import java.util.concurrent.Callable;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbWriteSents implements Callable
{
    private final int                 tnum;
    private final PreparedStatement   ps;

    public DbWriteSents(int tnum, PreparedStatement ps)
    {
        this.tnum = tnum;
        this.ps = ps;
    }
    
    @Override
    public Integer call()
            throws Exception
    {
        ps.executeBatch();

        ps.close();
        ps.getConnection().close();
        
        return tnum;
    }
}
