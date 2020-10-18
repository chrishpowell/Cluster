/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ESTest
{
    public static void main(String[] args)
            throws InterruptedException, ExecutionException
    {
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
        List<Callable<String>> lrts = new ArrayList<>();
        List<String> lts = List.of("A","B","C");

//        Callable<String> ct = () -> {
//            TimeUnit.MILLISECONDS.sleep(300);
//            return "Executing";
//        };
        lrts.add(new ESC(99,lts));
        lrts.add(new ESC(98,lts));
        
        List<Future<String>> lfs = execsrv.invokeAll(lrts);
        for( Future<String> fs: lfs )
        {
            System.out.print(" " +fs.get());
        }
        
        System.out.println("\r\nShutting down...");
        execsrv.shutdown();
        execsrv.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}

class ESC implements Callable<String>
{
    private final int           tnum;
    private final List<String>  ls;
    
    public ESC( int tnum, List<String> ls )
    {
        this.tnum = tnum;
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

        return Thread.currentThread().getName();
    }
}
