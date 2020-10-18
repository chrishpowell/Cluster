/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.utils;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.IntStream;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class CharacterUtils
{
    /**
     * Display character info about string.
     * @param str 
     */
    public static void displayCharInfo( String str )
    {
        System.out.format("%12s\t%-10s\t%-10s\t%-20s%n", "Character", "Codepoint", "UTF_8", "UTF_16");
        try( Scanner s = new Scanner(str) )
        {
            while( s.hasNext() )
            {
                String ch = s.next();
                String codepoint = getCodepoint(ch);
                byte[] u8 = encode(ch, StandardCharsets.UTF_8);
                String u8Hex = bytesToHex(u8);
                
                // Java prefixes two bytes (fe ff) to fixed length (4 bytes)  
                byte[] u16 = encode(ch, StandardCharsets.UTF_16);
                String u16Hex = bytesToHex(u16);
                System.out.format("\t%s\t%-10s\t%-10s\t%-20s%n", ch, codepoint, u8Hex, u16Hex);
            }
        }
    }

    private static byte[] encode(String data, Charset charset)
    {
        return data.getBytes(charset);
    }

    private static String getCodepoint(String ch)
    {
        return String.format("U+%04X", ch.codePointAt(0));
    }

    private static String bytesToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for( byte b : bytes )
        {
            sb.append(String.format("%02x ", b));
        }
        
        return sb.toString();
    }
    
    /**
     * Return stream of ints of input string.
     * @param s
     * @return 
     */
    public static IntStream bytestoIntStream( String s )
    {
        return CharBuffer.wrap(s.toCharArray()).chars();
    }
}
