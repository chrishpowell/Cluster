/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.sentences;


/**
 * Language code to model.
 * 
 * @author Chris Powell, Discoveri OU
 */
public enum LangCode
{
    en("English"),
    es("Spanish"),
    pt("Portuguese"),
    fr("French"),
    de("German"),
    ru("Russian"),
    hi("Hindi"),
    zh("Chinese");          // Mandarin

    private final String    name;
    
    LangCode(String name)
    {
        this.name = name;
    }

    public String getName() { return name; }
}
