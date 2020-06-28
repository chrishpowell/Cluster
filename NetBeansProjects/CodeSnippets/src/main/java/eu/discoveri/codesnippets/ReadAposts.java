/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.codesnippets;

import Sentences.English;
import Sentences.Language;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ReadAposts
{
    private static String apos = "/home/chrispowell/NetBeansProjects/CodeSnippets/src/main/java/eu/discoveri/resources/eng-apostrophes.properties";
    private static String s1 = "They'll say we won't or can't or wouldn't and simply don't work.";
    private static String s2 = "Here's the rub and that's the thing about John's thoughts.";
    private static String s3 = "How'd you say it's going? I wouldn't've said it is.";
    private static String s4 = "Let's count the ways he'd've said I'm apostrophising.";
    
    public static void main(String[] args)
            throws IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(apos));
        Language l = new English();
        
        String[] t1 = s1.toLowerCase().split("\\s");
        List<Token> lt1 = new ArrayList<>();
        for( String tok: t1 )
        {
            lt1.add(new Token(tok,tok));
        }
        System.out.println("s1: ");
//        l.unApostrophe(props,t1).forEach(t -> System.out.print(" "+t));
        List<Token> lt11 = l.unApostrophe(props, lt1);
        lt11.forEach(t -> System.out.print(t.getToken()+" "));
        System.out.println("");
        
        String[] t2 = s2.toLowerCase().split("\\s");
        System.out.println("s2: ");
        l.unApostrophe(props,t2).forEach(t -> System.out.print(" "+t));
        System.out.println("");
        
        String[] t3 = s3.toLowerCase().split("\\s");
        System.out.println("s3: ");
        l.unApostrophe(props,t3).forEach(t -> System.out.print(" "+t));
        System.out.println("");
        
        String[] t4 = s4.toLowerCase().split("\\s");
        System.out.println("s4: ");
        l.unApostrophe(props,t4).forEach(t -> System.out.print(" "+t));
        System.out.println("");
    }
}
