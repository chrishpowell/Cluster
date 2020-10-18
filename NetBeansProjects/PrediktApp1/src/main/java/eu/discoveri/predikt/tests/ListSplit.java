/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * Split list into countable groups (not attribute value).
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ListSplit
{
    public static void main(String[] args)
    {
        List<Integer> intList = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);

        Map<Integer, List<Integer>> groups = intList.stream().collect(Collectors.groupingBy(s -> (s - 1) / 3));
        Map<Integer, Set<Integer>> groupsl = intList.stream().collect(Collectors.groupingBy(s -> (s - 1) / 3,Collectors.toSet()));
        List<List<Integer>> subSets = new ArrayList<>(groups.values());
        
        List<String> sl = List.of("a","b","c","d","e","f","g","h","i","j");
        final AtomicInteger ctr = new AtomicInteger(0);
        // Split into "4" lists
        Collection<List<String>> llc = sl.stream()
                                    .collect(Collectors.groupingBy(s -> ctr.getAndIncrement()/4))
                                    .values();
        List<List<String>> lls = new ArrayList(llc);

        System.out.println("Subsets: " +lls);
    }
}
