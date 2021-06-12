/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author chrispowell
 */
public class SubsetsSet
{
    /**
     * All subsets of input.  Each set entry is mapped to a binary field.  Eg:
     * for [a,b,c] we have 000:{},001:{a},010:{b},011:{ab} etc.  
     * @param <T>
     * @param sets
     * @return 
     */
    public static <T> Set<Set<T>> subsets(List<T> sets)
    {
        return subsets(sets,sets.size());
    }
    
    /**
     * k-subsets, that is, limit subset size. Eg: for [a,b,c] limited to k=2,
     * we return [[a],[b],[c],[ab],[ac],[bc]] and do not include [abc].
     * 
     * @param <T>
     * @param values
     * @param maxSize subset size limit (k)
     * @return 
     */
    public static <T> Set<Set<T>> subsets(List<T> values, int maxSize)
    {
        Set<Set<T>> allSubsets = new HashSet<>();

        values.forEach(v -> {
            System.out.println("Working on ---> " +v);
            formSubsets(new HashSet<>(),values,allSubsets,maxSize);
            System.out.println("");
        });
        
        return allSubsets;
    }
    
    /**
     * Recursively build sets from baseSet.
     * 
     * @param <T>
     * @param baseSet The recursive base set Eg: [abc] from which [abcd][abce] are constrcuted
     * @param allSS All subsets (to depth)
     * @param values Unique itemset
     * @param depth Max. size of k-itemset
     * @return 
     */
    private static <T> Set<Set<T>> formSubsets( Set<T> baseSet, List<T> values, Set<Set<T>> allSS, int depth )
    {
        int idx = baseSet.size();
        int vSiz = values.size();
        System.out.println("\r\n1. baseSet: " +baseSet+ ", index in: " +idx+ ", values.size(): " +values.size()+ ", depth: " +depth);

        // Limit to required depth - 1
        if( idx < depth )
        {
            System.out.println("2. => baseSet:" +baseSet+" size: " +idx+ " < depth");
            // Lead items for this subset. Eg: [abc]
            T baseItem = values.get(idx);
            
            // Form next baseSet, eg: [ab],[ac]...[aZ]
            Set<T> newSS = new HashSet<>(baseSet);
            newSS.add(baseItem);

            // Next in baseSet loop. Eg: [a][a,b][a,c]...
            for( int ii=idx; ii<vSiz; ii++ )
            {
                System.out.println("3. baseSet loop... " +ii);
                
                // Is newSS already in all subsets? ************* UGLY!!
                if( allSS.contains(newSS) )
                {
                    System.out.println("newSS already in allSS" +newSS);
                    continue;
                }

                // Add to all subsets
                allSS.add(newSS);
                System.out.println("4. newSS: " +newSS);
                System.out.println("5. allSS: " +allSS);
                System.out.println("<-- Recursing down");
                // Recurse down forming baseSets.  Eg: With depth 4: [a]>[ab]>[abc]->[abcd][abce]
                formSubsets( newSS, values, allSS, depth );
                System.out.println("6. Bombed OUT. ii(idx): " +ii+ ", newSS: " +newSS+ ", allSS: " +allSS); System.out.println("");
            }
        }
        else
            if( vSiz > idx )
            { // Add values beyond depth-1 ??
                System.out.println("7. End of line...");
                Set<T> newSS = new HashSet<>(baseSet);

                for( int ii=baseSet.size()+1; ii<values.size(); ii++ )
                {
                    System.out.println("Adding " +values.get(ii)+ " to baseSet/allSS");
                    newSS.add(values.get(ii));
                    allSS.add(newSS);
                }
            }

        System.out.println("  8. >>> Bombing OUT! (To next in baseSet loop?) baseSet: " +baseSet+ ", index out: " +idx+ ", allSS: " +allSS);
        return allSS;
    }

    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        List<String> sets = Arrays.asList("a1","b2","c3","x9");
        
        Set<Set<String>> allSubsets = subsets(sets);
        System.out.println("" +allSubsets);
    }
}
