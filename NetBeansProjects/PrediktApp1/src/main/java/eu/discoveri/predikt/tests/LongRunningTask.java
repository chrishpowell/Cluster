/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LongRunningTask implements Callable<String>
{
    private final int           tnum;
    private final List<String>  ls;

    public LongRunningTask(int tnum, List<String> ls)
    {
        this.tnum = tnum+1;
        this.ls = ls;
    }

    @Override
    public String call()
    {
        // Do stuff and return some String
        ls.forEach( _i -> {
            try { TimeUnit.MILLISECONDS.sleep(100); }
            catch( Exception e ){}
        });
        
        System.out.print(" " +Thread.currentThread().getName());
        return Thread.currentThread().getName();
    }
}
