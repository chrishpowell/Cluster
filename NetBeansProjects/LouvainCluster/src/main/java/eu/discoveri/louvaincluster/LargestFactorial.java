/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.louvaincluster;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LargestFactorial
{
    public static void main(String[] args)
    {
        int fact = 1;
        double rem = (double)Long.MAX_VALUE;
        while( rem >= 1.0d )
        {
            ++fact;
            rem = rem/(double)fact;
        }
        
        System.out.println("Fact: " +(fact-1));
        
        System.out.println("Max. nodes: " +((1.0+Math.sqrt((double)4*Integer.MAX_VALUE+1))/2.));
    }
}
