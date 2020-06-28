/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package Sentences;

import eu.discoveri.codesnippets.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class English extends Language
{
    /**
     * Ditch apostrophes.
     * 
     * @param props
     * @param tokens
     * @return 
     */
    @Override
    public List<String> unApostrophe( Properties props, String[] tokens )
    {
        List<String> updToks = new ArrayList<>();
        for( String tok: tokens )
        {
            // Any apostrophes?
            if( tok.contains("'") )
            {
                // Here's, won't etc.
                if( props.containsKey(tok) )
                {
                    updToks.add(props.getProperty(tok));
                }
                else
                {
                    if( !tok.contains("'s") )
                    {
                        if( tok.contains("n't") )
                        {
                            String[] stoks = tok.split("n't");
                            updToks.add(stoks[0]);
                            updToks.add("not");
                            if( stoks.length > 1 )
                                updToks.add(props.getProperty(stoks[1]));
                        }
                        else
                        { // Split as there may be multiple apostrophes
                            String[] stoks = tok.split("'");
                            updToks.add(stoks[0]);
                            for( int idx = 1; idx<stoks.length; idx++ )
                                updToks.add(props.getProperty("'"+stoks[idx]));
                        }
                    }
                    else
                        updToks.add(tok);
                }
            }
            else
            {
                updToks.add(tok);
            }
        }
        
        return updToks;
    }
    
    @Override
    public List<Token> unApostrophe( Properties props, List<Token> tokens )
    {
        List<Token> updToks = new ArrayList<>();
        
        tokens.forEach((tok) -> {
            String stok = tok.getToken();
            // Any apostrophes?
            if( stok.contains("'") )
            {
                // Here's, won't etc.
                if( props.containsKey(stok) )
                {
                    updToks.add(new Token(props.getProperty(stok),""));
                }
                else
                {
                    if( !stok.contains("'s") )
                    {
                        if( stok.contains("n't") )
                        {
                            String[] stoks = stok.split("n't");
                            updToks.add(new Token(stoks[0],""));
                            updToks.add(new Token("not",""));
                            if( stoks.length > 1 )
                                updToks.add(new Token(props.getProperty(stoks[1]),""));
                        }
                        else
                        { // Split as there may be multiple apostrophes
                            String[] stoks = stok.split("'");
                            updToks.add(new Token(stoks[0],""));
                            for( int idx = 1; idx<stoks.length; idx++ )
                                updToks.add(new Token(props.getProperty("'"+stoks[idx]),""));
                        }
                    }
                    else
                        updToks.add(tok);
                }
            }
            else
            {
                updToks.add(tok);
            }
        });
        
        return updToks;
    }
}
