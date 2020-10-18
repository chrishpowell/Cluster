/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class MergeStrings
{
    public static void main(String[] args)
    {
        List<String> ls = List.of("abc","def","ghi","jkl","mno","pqr");
        String s = ls.stream().collect(Collectors.joining(","));
        System.out.println("..> " +s);
    }
}
