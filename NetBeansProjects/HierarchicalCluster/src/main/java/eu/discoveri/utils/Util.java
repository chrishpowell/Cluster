/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.utils;

/**
 *
 * @author chrispowell
 */
public class Util
{
    /**
     * Count set bits in an integer.
     * 
     * @param num
     * @return 
     */
    public static int bitCount(int num)
    {
        int count = 0;
        while( num > 0 )
        {
            num &= (num - 1);
            count++;
        }
        
        return count;
    }
}
