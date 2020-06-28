/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Token
{
    private String  name;
    private String  lemma;
    
    public Token(String name, String lemma)
    {
        this.name = name;
        this.lemma = lemma;
    }
    
    public String getToken() { return name; }
    public void setToken(String token) { this.name = token; }
    public String getLemma() { return lemma; }
}
