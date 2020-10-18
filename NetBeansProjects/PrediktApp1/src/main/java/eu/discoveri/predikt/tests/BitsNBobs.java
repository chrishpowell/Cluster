/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class BitsNBobs
{
    private static void listString()
    {
        List<String> ss = List.of("The","quick","brown","dog","jumps","over","the","lazy","dog");
        String s = ss.toString();
        System.out.println("> " +s);
        List<String> ls = Arrays.asList(s.split("\\s*,\\s*"));
        ls.forEach(str -> System.out.format(" %s",str));
    }

    public static void main(String[] args)
    {
        listString();
//        System.out.println("> " +10%4);
//        System.out.println("> " +((int)(10/4)+1));
    }
}
