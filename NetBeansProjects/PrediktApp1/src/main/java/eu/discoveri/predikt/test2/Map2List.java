/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author chrispowell
 */
public class Map2List
{
    static Map<String,Set<String>> map2list = new HashMap<>();
    
    public static void main(String[] args)
    {
        Set<String> l1 = new HashSet<>();
        Set<String> l2 = new HashSet<>();
        Set<String> l3 = Set.of("C3");
        
        map2list.put("L1", l1);
        map2list.put("L2", l2);
        map2list.put("L3", l3);
        
        Set<String> sc = map2list.get("L2");
        sc.add("B2");
        sc.add("fred");
        
        map2list.forEach((k,v) -> {
            System.out.println("Key: " +k);
            v.forEach(s -> {
                System.out.print(" " +s);
            });
            System.out.println("");
        });
    }
}
