/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.hcluster.test;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author chrispowell
 */
public class HashMatch
{
    public static void main(String[] args)
    {
        Set<String> hs0 = new HashSet<>();
        hs0.add("first");
        hs0.add("second");
        hs0.add("third");
        hs0.add("many");
        
        Set<String> hs1 = new HashSet<>();
        hs1.add("first");
        
        hs0.retainAll(hs1);
        
        System.out.println(hs0+":"+hs0.size());
    }
}
