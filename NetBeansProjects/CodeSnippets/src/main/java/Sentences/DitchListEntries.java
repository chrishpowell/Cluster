/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package Sentences;

import eu.discoveri.codesnippets.Token;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DitchListEntries
{
    private List<Token> tokens = new ArrayList<>();

    /**
     * Immutable list
     */
    private void loadTokens()
    {
         tokens.add(new Token("Tok-1",""));
         tokens.add(new Token("Tok-2",""));
         tokens.add(new Token("86.3",""));
         tokens.add(new Token("Tok-4",""));
         tokens.add(new Token("23",""));
         tokens.add(new Token("Tok-6",""));
    }
    
    /**
     * Remove certain entries from list.
     * @param locl
     * @return 
     */
    public List<Token> ditchEntries( Locale locl )
    {
        NumberFormat nf = NumberFormat.getInstance(locl);
        for( Iterator<Token> t = tokens.iterator(); t.hasNext(); )
        {
            Token tok = t.next();
            try
            {
                Number n = nf.parse(tok.getToken());
                t.remove();
            }
            catch( ParseException pex ){}                                       // If not a number, good
        }
        
        return tokens;
    }
    
    public List<Token> getTokens() { return tokens; }
    
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        DitchListEntries dle = new DitchListEntries();
        
        dle.loadTokens();
        dle.ditchEntries(Locale.ENGLISH);
        
        dle.getTokens().forEach(t -> System.out.print(" " +t.getToken()));
    }
}
