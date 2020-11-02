/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.test;

import eu.discoveri.predikt3.sentences.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class CleanTokenTest
{
    static String stok = "18. 12/12 SLIDES SHARE SHARE TWEET SHARE EMAIL 1/12 SLIDES Next Slide AdChoices 1 2 3 4 YOU MAY LIKE Ad Microsoft Savvy Americans do this to earn an extra $1,394 per month in retirement The Motley Fool New Car Gadget Magically Removes Scratches & Dents NanoMagic 23 Gadgets That Could Sell Out Before the Holidays Gadgets Post More from Astrofame The Reason Why Each Zodiac Sign Is Difficult To Love Your Weekly Horoscope: August 24 - 30 Who Is Your Zodiac Sign Sexually Incompatible With? ...";
    public static void main(String[] args)
    {
        List<Token> newToks = new ArrayList<>();
        
        List<String> ls = Arrays.asList(stok.split("\\s*[ ]\\s*"));
        for( String tk: ls )
        {
//            System.out.print(" [" +tk+ "]");
            newToks.add(new Token(tk,"")); 
        }
        
        System.out.println("\r\n....> " +newToks.size());
    }
}
