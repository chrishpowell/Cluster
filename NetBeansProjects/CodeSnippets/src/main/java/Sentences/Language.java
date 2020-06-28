/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package Sentences;

import eu.discoveri.codesnippets.Token;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public abstract class Language
{
    /**
     * Ditch apostrophes.
     * 
     * @param props
     * @param tokens
     * @return 
     */
    public abstract List<String> unApostrophe( Properties props, String[] tokens );

    public abstract List<Token> unApostrophe( Properties props, List<Token> tokens );
}
