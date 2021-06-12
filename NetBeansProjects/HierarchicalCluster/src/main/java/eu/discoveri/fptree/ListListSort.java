/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author chrispowell
 */
public class ListListSort
{
    public static void main(String[] args)
    {
//        List<String> a = Arrays.asList("a");
//        List<String> z = Arrays.asList("z");
//        List<String> c = Arrays.asList("c");
//        List<String> ab = Arrays.asList("a","b");
//        List<String> ac = Arrays.asList("a","c");
//        List<String> bd = Arrays.asList("b","d");
//        List<String> bc = Arrays.asList("b","c");
//        List<String> abc = Arrays.asList("a","b","c");
//        List<String> abd = Arrays.asList("a","b","d");
//        List<String> abcd = Arrays.asList("b","c","a","d");
//        List<String> b = Arrays.asList("b");
//        List<String> ace = Arrays.asList("a","c","e");
//
//        // Sorted list
//        List<BUT> lb = Arrays.asList(new BUT(b,1.f), new BUT(c,1.f), new BUT(bc,2.f), new BUT(bd,1.f), new BUT(a,2.f),
//                                     new BUT(ab,1.f), new BUT(ac,2.f), new BUT(abc,1.f), new BUT(abd,1.f), new BUT(z,1.f), new BUT(ace,1.f), new BUT(abcd,1.f));
        
                List<String> a = Arrays.asList("a");
        List<String> z = Arrays.asList("z");
        List<String> c = Arrays.asList("c");
        List<String> ab = Arrays.asList("a","b");
        List<String> ac = Arrays.asList("a","c");
        List<String> bd = Arrays.asList("b","d");
        List<String> bc = Arrays.asList("b","c");
        List<String> abc = Arrays.asList("a","b","c");
        List<String> abd = Arrays.asList("a","b","d");
        List<String> abcd = Arrays.asList("b","c","a","d");
        List<String> b = Arrays.asList("b");
        List<String> ace = Arrays.asList("a","c","e");

        // Sorted list
        List<BUT> lb = Arrays.asList(new BUT(b,1.f), new BUT(c,1.f), new BUT(bc,2.f), new BUT(bd,1.f), new BUT(a,2.f),
                                     new BUT(ab,1.f), new BUT(ac,2.f), new BUT(abc,1.f), new BUT(abd,1.f), new BUT(z,1.f), new BUT(ace,1.f), new BUT(abcd,1.f));

        // Sort by length descending
        Collections.sort(lb);
//      0  b c a d
//      1  a b c
//      2  a b d
//      3  a c e
//      4  b c
//      5  b d
//      6  a b
//      7  a c
//      8  b
//      9  c
//      10 a
//      11 z

        lb.forEach(l -> {
            l.getNames().forEach(n -> System.out.print(" "+n));
            System.out.println("");
        });
        
        // Set intersection
        List<String> s1 = lb.get(2).getNames().stream()
                            .filter(lb.get(5).getNames()::contains)
                            .collect(Collectors.toList());
        System.out.println("..> " +s1);
        System.out.println();
        
        // Do above using BUT class (List<BUT>)
        List<String> sc = lb.get(2).getNames();
        System.out.println("2: " +sc);
        System.out.println("Scores> " +lb.get(4).getNames()+":"+lb.get(4).getScore()+ ", " +lb.get(5).getNames()+":"+lb.get(5).getScore()+ ", " +lb.get(6).getNames()+":"+lb.get(6).getScore()+ ", " +lb.get(7).getNames()+":"+lb.get(7).getScore() );
        
        // Get a parent BUT
//        List<BUT> ls = lb.stream()
//                        .filter(p -> p.getNames().size()==sc.size()-1 )
//                        .filter(p -> sc.containsAll(p.getNames()))
//                        .collect(Collectors.toList());
//        ls.forEach(x -> System.out.println(" >> "+x.getNames()));
        Optional<BUT> lbb = lb.stream()
                .filter(p -> p.getNames().size()==sc.size()-1 )
                .filter(p -> sc.containsAll(p.getNames()))
                .max(Comparator.comparingDouble(BUT::getScore));
        
        if( lbb.isPresent() )
            System.out.println("::> " +lbb.get().getNames());
        else
            System.out.println("Huh?");
    }
}
