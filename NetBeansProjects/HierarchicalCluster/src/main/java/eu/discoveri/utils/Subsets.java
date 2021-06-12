/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.utils;

import eu.discoveri.fptree.SubsetsException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 *
 * @author chrispowell
 */
public class Subsets
{
    private static int valSiz = 0;
    
    /**
     * Generate all subsets of items (excluding null). Nota Bene: Not
     * computationally efficient.
     * 
     * @param <T>
     * @param values List (T) from which all subsets are generated.
     * @return Stream of List of type T
     */
    public static <T> Stream<List<T>> subsetsList(List<T> values)
    {
        // Create Iterator from input
        SubSetListIterator<T> ssi = new SubSetListIterator(values);
        // Stream the subsets from partitioned elements of the input (Spliterator(s))
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ssi, 0), false).dropWhile(t -> t.isEmpty());
    }
    
    /**
     * Generate all subsets of items (excluding null).  NB: Not efficient, do not
     * use in production, use FPGrowth.
     * 
     * @param <T>
     * @param values List (T) from which all subsets are generated.
     * @return A Set of Sets of type T
     */
    public static <T> Set<Set<T>> subsetsAllSets(List<T> values)
    {
        Set<Set<T>> allSubsets = new HashSet<>();
        
        valSiz = values.size();
        for( int ii=0; ii<(1<<valSiz); ii++ )
        {
            Set<T> ss = new HashSet<>();
            for( int jj=0; jj<valSiz; jj++ )
            {
                if( (ii & (1 << jj)) > 0 )
                {
                    ss.add(values.get(jj));
                }
            }
            if( !ss.isEmpty() )
                allSubsets.add(ss);
        }
        
        return allSubsets;
    }
    
    /**
     * Generate all k-subsets of items (excluding null). Note a k-subset is a
     * subset with a size of 1..maxSize. NB: Not efficient, do not use in
     * production, use FPGrowth.
     * 
     * @param <T>
     * @param values
     * @param maxSize
     * @return 
     */
    public static <T> Set<Set<T>> subsetsAllSets(List<T> values, int maxSize)
    {
        Set<Set<T>> allSubsets = new HashSet<>();
        
        valSiz = values.size();
        for( int ii=0; ii<(1<<valSiz); ii++ )
        {
            Set<T> ss = new HashSet<>();
            for( int jj=0; jj<valSiz; jj++ )
            {
                if( (ii & (1 << jj)) > 0 )
                {
                    ss.add(values.get(jj));
                }
            }
            
            if( !ss.isEmpty() && ss.size() <= maxSize )
                allSubsets.add(ss);
        }
        
        return allSubsets;
    } 

    /**
     * Cull subsets to sets of length maxSize.  NB, not efficient.
     * 
     * @param <T>
     * @param allSubsets
     * @param maxSize
     * @return 
     * @throws eu.discoveri.fptree.SubsetsException 
     */
    public static <T> Set<Set<T>> subsetsWithMaxSize(Set<Set<T>> allSubsets,int maxSize)
            throws SubsetsException
    {
        int sl = allSubsets.size();
        if( sl < 2 )
        {
            throw new SubsetsException("Not enough subsets to process, size=" +sl);
        }
        if( maxSize < 1 || maxSize >= valSiz )
        {
            throw new SubsetsException("maxSize should be between 1 and " +valSiz+ " inclusive");
        }

        // Copy all subsets for culling
        Set<Set<T>> allShortSubsets = new HashSet<>(allSubsets);
        
        // Cull
        allShortSubsets.removeIf(s -> s.size() > maxSize);
        
        return allShortSubsets;
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
             throws SubsetsException
    {
        List<String> testSet = Arrays.asList("a1","b2","c3","x9");
        
        Set<Set<String>> allSS = subsetsAllSets(testSet);
        System.out.println("testSet subsets: " +allSS);
        
        Set<Set<String>> culledSS = subsetsWithMaxSize(allSS,2);
        System.out.println("culled subsets: " +culledSS);
        
        Set<Set<String>> limitSS = subsetsAllSets(testSet,2);
        System.out.println("limit subsets: " +limitSS);
    }
}
