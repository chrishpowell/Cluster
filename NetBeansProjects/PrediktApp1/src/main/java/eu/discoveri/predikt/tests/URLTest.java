/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class URLTest
{
    public static void main(String[] args)
            throws MalformedURLException, IOException
    {
        URL website = new URL("https://www.prokerala.com/astrology/horoscope");
	URLConnection connection = website.openConnection();
        InputStream is = connection.getInputStream();
        
        String result = new BufferedReader(new InputStreamReader(is))
                            .lines().collect(Collectors.joining("\n"));
        
        System.out.println("....> " +result);
    }
}
