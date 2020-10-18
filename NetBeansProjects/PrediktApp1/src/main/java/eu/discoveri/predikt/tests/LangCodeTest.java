/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.config.EnSetup;
import eu.discoveri.predikt.config.LangSetup;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LangCodeTest
{
    public static void main(String[] args)
    {
        // Language/locale setup
        LangSetup langSetup = new EnSetup();
        System.out.println("... Setup ["+langSetup.getLangCode().getName()+"]");
        
        System.out.println("Shortcode: "+langSetup.getLangCode().name());
    }
}
