/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package Sentences;

import java.util.List;
import java.util.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Spanish extends Language
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
        throw new UnsupportedOperationException("Spanish");
    }
}
