/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import eu.discoveri.predikt.sentences.Token;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author chrispowell
 */
public class ListReplace
{
    public static void main(String[] args)
    {
        List<Token> listToks1 = List.of( new Token("American",""), new Token("81",""), new Token("year",""), new Token("old",""), new Token("inventor",""), new Token("a",""), new Token("pioneer",""), new Token("television","") );
        List<Token> listToks2 = List.of( new Token("American","NN"), new Token("82","JJ"), new Token("month","KK"), new Token("old","LL"), new Token("inventor","MM"), new Token("a","PP"), new Token("pioneer","QQ"), new Token("television","RR") );
        
        listToks1 = listToks2.stream().collect(Collectors.toList());
        listToks1.forEach(t -> {
            System.out.println(""+t.getToken()+":"+t.getPOS());
        });
    }
}
