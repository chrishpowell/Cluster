/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

/**
 *
 * @author chrispowell
 */
public class IiJiLoop
{
    public static void main(String[] args)
    {
        int end = 11, PAGSIZ=4;
        
        System.out.println("> *Unique* pairs from list size: " +end+ " in groups of: " +PAGSIZ+ " delimited by []\r\n");
        
        for( int ii=1; ii<=end; ii+=PAGSIZ )    // 1, 4, 7, 10
        {
//            System.out.println(" <" +ii+ ">");
            System.out.println("P: ");
            for( int pp=ii; pp<ii+PAGSIZ; pp++ ) // 123, 456, 789, 10
            {
                System.out.println("   [" +pp+ "]");
                for( int rr=pp+1; rr<=end; rr+=PAGSIZ ) // pp:1(234); pp:2(567); pp:3(89a)
                {
//                    System.out.println("     (" +rr+ ")");
                    System.out.print("       [ ");
                    for( int qq=rr; qq<(rr+PAGSIZ) && qq <= end; qq++ )
                    {
                        System.out.print("{" +pp+":"+qq+ "} ");
                    }
                    System.out.println("]");
                }
                System.out.println("");
            }
//            System.out.println("");
        }
//        System.out.println("");
    }
}
