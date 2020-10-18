/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt3.sentences;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class German extends Language
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
            }
            else
            {
                updToks.add(tok);
            }
        }
        
        return updToks;
    }
    
    /**
     * Ditch apostrophes.  @TODO: UPDATE THIS!!
     * 
     * @param props
     * @param tokens
     * @return 
     */
    @Override
    public List<Token> unApostrophe( Properties props, List<Token> tokens )
    {
        throw new UnsupportedOperationException("German");
    }
    
    /**
     * Ditch Out Of Band characters (such as " for degree secs in English).
     * 
     * @param doc
     * @return 
     */
    @Override
    public String remOOBChars( String doc )
    {
        throw new UnsupportedOperationException("Mandarin");
    }
}
