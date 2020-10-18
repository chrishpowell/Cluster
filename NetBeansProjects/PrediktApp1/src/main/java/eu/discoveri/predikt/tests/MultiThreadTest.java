/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class MultiThreadTest
{
    public static void main(String[] args)
            throws InterruptedException, ExecutionException
    {
        // Set up the futures
        ExecutorService execsrv = Executors.newCachedThreadPool();
        List<Callable<String>> lrts = new ArrayList<>();
        
        // Calls/threads
        List<String> ls = List.of("A","B","C");
        lrts.add( new LongRunningTask(1,ls) );
        lrts.add( new LongRunningTask(2,ls) );
        lrts.add( new LongRunningTask(3,ls) );

        // Run the threads
        List<Future<String>> lfs = execsrv.invokeAll(lrts);
        for( Future<String> fs: lfs )
        {
            System.out.print(" " +fs.get());
        }
        
        System.out.println("\r\nShutting down...");
        execsrv.shutdown();
    }
}
