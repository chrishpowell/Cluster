/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.sentences;

import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class English extends Language
{
    // Apostrophe strings match
    static Pattern p = Pattern.compile("'*");
     
    /**
     * Ditch apostrophes. Sentence should have been tokenized.
     * 
     * @param props
     * @param tokens
     * @return 
     * @throws TokensListIsEmptyException 
     */
    @Override
    public List<String> unApostrophe( Properties props, String[] tokens )
            throws TokensListIsEmptyException
    {
        if( tokens.length < 1 )
            throw new TokensListIsEmptyException("No tokens in array!");
                
        List<String> updToks = new ArrayList<>();
        for( String tok: tokens )
        {
            // Any apostrophes?
            if( tok.contains("'") || tok.contains("\u2019s") )
            {
                // Here's, won't etc.
                if( props.containsKey(tok) )
                {
                    updToks.add(props.getProperty(tok));
                }
                else
                {
                    if( !tok.contains("'s") && !tok.contains("\u2019s")  )
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
                    {
                        if( tok.contains("'s") )
                            updToks.add(tok.replace("'s",""));
                        else
                            updToks.add(tok.replace("\u2019s",""));
                    }
                }
            }
            else
            {
                updToks.add(tok);
            }
        }
        
        return updToks;
    }
    
    /**
     * Ditch apostrophes.Sentence should have been tokenised.
     * 
     * @param props
     * @param tokens
     * @return 
     * @throws TokensListIsEmptyException 
     */
    @Override
    public List<Token> unApostrophe( Properties props, List<Token> tokens )
            throws TokensListIsEmptyException
    {
        if( tokens.size() < 1 )
            throw new TokensListIsEmptyException("No tokens in list!");
                
        List<Token> updToks = new ArrayList<>();
        
        tokens.forEach((tok) -> {
            String stok = tok.getToken();
            // Any apostrophes?
            if( stok.contains("'") || stok.contains("\u2019") )
            {
                // Here's, won't etc.
                if( props.containsKey(stok) )  // Fix for unicode
                {
                    updToks.add(new Token(props.getProperty(stok)));
                }
                else
                {
                    if( !(stok.contains("'s") || stok.contains("\u2019\u0073")) )  // Not 's
                    {
                        if( stok.contains("n't") )
                        {
                            String[] stoks = stok.split("n't");
                            updToks.add(new Token(stoks[0]));
                            updToks.add(new Token("not"));
                            if( stoks.length > 1 )
                                updToks.add(new Token(props.getProperty(stoks[1])));
                        }
                        else
                        if( stok.contains("\u006e\u2019\u0074") )               // n't in Unicode
                        {
                            String[] stoks = stok.split("\u006e\u2019\u0074");
                            updToks.add(new Token(stoks[0]));
                            updToks.add(new Token("not"));
                            if( stoks.length > 1 )
                                updToks.add(new Token(props.getProperty(stoks[1])));
                        }
                        else
                        if( stok.contains("\u2019") )                           // ' Unicode
                        { // Split as there may be multiple apostrophes
                            String[] stoks = stok.split("\u2019");
                            updToks.add(new Token(stoks[0]));
                            for( int idx = 1; idx<stoks.length; idx++ )
                                updToks.add(new Token(props.getProperty("'"+stoks[idx])));
                        }
                        else
                        if( p.matcher(stok).matches() )                         // Any number of '
                        {
                            // Do nothing, skip this token  @TODO: Improve this!!
                        }
                        else
                        { // Split as there may be multiple apostrophes
                            String[] stoks = stok.split("'");
                            updToks.add(new Token(stoks[0]));
                            for( int idx = 1; idx<stoks.length; idx++ )
                                updToks.add(new Token(props.getProperty("'"+stoks[idx])));
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
   
    /**
     * Ditch Out Of Band characters (such as " for degree secs in English?).
     * 
     * @param s
     * @return 
     */
    @Override
    public String remOOBChars( String s )
    {
        return s.replaceAll("null", "");
    }
}
