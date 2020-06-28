/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets;

/**
 *
 * @author Chris Powell, Discoveri OU
 */
public enum YLang
{
    LANG1("en",new LangSub1("",1)),
    LANG2("de",new LangSub2());
    
    private String  str;
    private Lang    lang;
    YLang(String str,Lang lang)
    {
        this.str = str;
        this.lang = lang;
    }
}
