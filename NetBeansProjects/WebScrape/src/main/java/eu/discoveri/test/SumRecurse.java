/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author chrispowell
 */
public class SumRecurse
{
    private static int findNextNonDup( List<Integer> li, Set<Integer> ld )
    {
        for( int i: li )
        {
            if( !ld.contains(i) )
            {
                ld.add(i);
                return i;
            }
        }
        
        return 0;
    }
    
    private static int calcSum( List<Integer> li, int count, Set<Integer> ld )
    {
        int total = 0;
        if( count >= li.size() )
            return 0;
        
        System.out.println("------------["+count+"]-------------------");
        int addVal = findNextNonDup(li,ld);
        System.out.println("Add this val: " +addVal);
        ld.forEach(l -> {
            System.out.print(" "+l.intValue());
        });

        System.out.println("\r\n   so now adding: " +addVal+ " to ld and calling next in recurse.");
        total = addVal + calcSum( li, ++count, ld );
        
        System.out.println("\r\n-----------------------------------");
        System.out.println("");
        
        return total;
    }
    
    public static void main(String[] args)
    {
        Set<Integer> ld = new HashSet<>();
        List<Integer> li = List.of(4,3,3,8,2,3,9);
        
        System.out.println(">> " +calcSum(li,0,ld));
    }
}
