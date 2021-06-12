/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import eu.discoveri.utils.Subsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author chrispowell
 */
public class ListIntersect
{
    public static void listi()
    {
        List<String> l0 = Arrays.asList("a","b","c");
        List<String> l1 = Arrays.asList("a","b");
        List<String> l2 = Arrays.asList("a","c");
        List<String> l3 = Arrays.asList("b","c");
        List<String> l4 = Arrays.asList("a","d");
        List<String> lx = Arrays.asList("x");
        
        List<List<String>> lls = Arrays.asList(l0,l1,l2,l3,l4);
        
        // Intersect all "docs" with each other
        for( int ii=0; ii<lls.size(); ii++ )
        {
            List<String> si = lls.get(ii);
            
            for( int jj=ii+1; jj<lls.size(); jj++ )
            {
                // Intersect sets
                Set<String> sj = lls.get(jj).stream()
                        .filter(si::contains).collect(Collectors.toSet());
                
                if( sj.size() == lls.get(jj).size() )
                    System.out.println(">> " +sj);
            }
        }
    }
    
    public static void seti()
    {
        Set<String> l0 = new HashSet<>(Arrays.asList("a","b","c"));
        Set<String> l1 = new HashSet<>(Arrays.asList("a","b"));
        Set<String> l2 = new HashSet<>(Arrays.asList("a","c"));
        Set<String> l3 = new HashSet<>(Arrays.asList("b","c"));
        Set<String> l4 = new HashSet<>(Arrays.asList("a","d"));
        Set<String> lx = new HashSet<>(Arrays.asList("x"));
        
        List<Set<String>> lls = Arrays.asList(l0,l1,l2,l3,l4);
        
        /*
         * Intersect all "docs" with each other
         */
        // "Docs"
        for( int ii=0; ii<lls.size(); ii++ )
        {
            Set<String> si = lls.get(ii);
            List<String> lsi = new ArrayList<>(si);
            Set<Set<String>> ssi = SubsetsSet.subsets(lsi);
            
//            System.out.println("All subsets of l" +ii+ ":");
//            ss.forEach(s -> {
//                System.out.print(" " +s);
//            });
//            System.out.println();
            
            // All other "docs"
            for( int jj=ii+1; jj<lls.size(); jj++ )
            {
                // Does doci intersect well with docj?
                Set<String> sj = lls.get(jj);
                List<String> lsj = new ArrayList<>();
                Set<Set<String>> ssj = SubsetsSet.subsets(lsj);
                
                ssj.retainAll(ssi);
                System.out.println("..> " +ssj);
//                Set<String> intr = ssj.filter(lsi::contains).collect(Collectors.toSet());
                
//                if( sj.size() == lls.get(jj).size() )
//                    System.out.println(">> " +sj);
            }
        }
    }
    
    public static void seti1()
    {
//        Set<String> l0 = new HashSet<>(Arrays.asList("a","b","c"));
//        Set<String> l1 = new HashSet<>(Arrays.asList("a","b"));
//        Set<String> l2 = new HashSet<>(Arrays.asList("a","d"));
        List<String> l0 = Arrays.asList("a","b","c");
        List<String> l1 = Arrays.asList("a","b");
        List<String> l2 = Arrays.asList("a","d");
        
        List<List<String>> docs = Arrays.asList(l0,l1,l2);
        List<String> uniqueList = Arrays.asList("a","b","c","d");
        
        // Subsets of all items
        Set<Set<String>> allSS = SubsetsSet.subsets(uniqueList);
        
        docs.forEach(l -> {
            // Subsets of doc
            Set<Set<String>> lss = SubsetsSet.subsets(l);
            lss.retainAll(allSS);
            
            System.out.println("" +lss);
        });
    }
    
    /**
     * B E S T
     * =======
     */
    public static void seti2()
    {
        List<String> l0 = Arrays.asList("a","b","c","d");
        List<String> l1 = Arrays.asList("a","b");
        
        Set<Set<String>> s0 = SubsetsSet.subsets(l0);
        Set<Set<String>> s1 = SubsetsSet.subsets(l1);
        
        // Preserve original
        Set<Set<String>> s1c = new HashSet<>(s1);
        
        s1c.retainAll(s0);
        System.out.println(""+s1c);
    }
    
    public static void seti3()
    {
        List<String> l0 = Arrays.asList("a","b","c","d");
        List<String> l1 = Arrays.asList("a","b","c");
        
        Set<Set<String>> s0 = SubsetsSet.subsets(l0,3);
        System.out.println("s0 subsets [lim]: " +s0);
        Set<Set<String>> s1 = SubsetsSet.subsets(l1,2);
        System.out.println("s1 subsets [lim]: " +s1);
        
        // Preserve original
        Set<Set<String>> s1c = new HashSet<>(s1);
        
        s1c.retainAll(s0);
        System.out.println(""+s1c);
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        System.out.println("List intersect\r\n------------------------");
        listi();
        
        System.out.println("\r\nSet intersect\r\n------------------------");
//        seti();
        seti3();
    }
}
