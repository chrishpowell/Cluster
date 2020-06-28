/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.codesnippets;

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class NFTest
{
    public static void main(String[] args)
    {
        Locale esLocale = Locale.forLanguageTag("es-ES");
        NumberFormat esNF = NumberFormat.getInstance(esLocale);
        
        System.out.println("..> " +esNF.format(100.99));
    }
}
