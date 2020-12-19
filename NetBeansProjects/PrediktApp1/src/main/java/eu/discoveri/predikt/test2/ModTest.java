/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

/**
 *
 * @author chrispowell
 */
public class ModTest
{
    public static void main(String[] args)
    {
        int end = 80;
        int docnt = 5;
        int modd = end/docnt;
        
        System.out.println("> 0:" +0%modd+ ", 1:" +1%modd+ ", 15:" +15%modd+ ", 16:" +16%modd+ ", 17:" +17%modd+ ", 31:" +31%modd+ ", 32:" +32%modd+ ", 33:" +33%modd+ ", 79:" +79%modd+ ", 80:" +80%modd );
    }
}
