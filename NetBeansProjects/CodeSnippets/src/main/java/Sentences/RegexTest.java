/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package Sentences;

import java.util.regex.Pattern;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class RegexTest
{
    static Pattern p = Pattern.compile("'*");
    
    public static void main(String[] args)
    {
        System.out.println("Matches? Single quote> " +p.matcher("'").matches());
        System.out.println("Matches? Multiple quote> " +p.matcher("'''").matches());
    }
}
