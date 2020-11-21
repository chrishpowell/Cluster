/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.webscrape;

/**
 * For Document db.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Content
{
    private final String    title;
    private final String    content;
    private final String    lang;
    
    /**
     * Constructor.
     * 
     * @param lang  NB: @TODO: ===: Make LangCode
     * @param title
     * @param content 
     */
    public Content( String lang, String title, String content )
    {
        this. title = title;
        this.content = content;
        this.lang = lang;
    }

    public String getLangCode() { return lang; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
