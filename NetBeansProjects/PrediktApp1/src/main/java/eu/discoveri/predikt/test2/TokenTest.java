/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import eu.discoveri.predikt.sentences.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author chrispowell
 */
public class TokenTest
{
    public static void main(String[] args)
            throws Exception
    {
        // Immutable list
        List<Token> ts = List.of( new Token("American",""), new Token("inventor",""), new Token("a",""), new Token("pioneer",""), new Token("television","") );
        // Dynamic list
        List<Token> tokens = new ArrayList<>(ts);
        
        for(Iterator<Token> iter = tokens.iterator(); iter.hasNext();)
        {
            Token t = iter.next();
            if(t.getToken().equals("pioneer")) { iter.remove(); }
        }
        
        tokens.forEach(t -> {
            System.out.print(t.getToken()+" ");
        });
        System.out.println("");
    }
}