/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.function.BiConsumer;
import org.carrot2.clustering.Document;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class AstroDocCluster implements Document
{
    private final long      id;
    private final String    langCode,
                            title,
                            content;

    /**
     * Constructor
     * @param id
     * @param langCode
     * @param title
     * @param content 
     */
    public AstroDocCluster(long id, String langCode, String title, String content)
    {
        this.id = id;
        this.langCode = langCode;
        this.title = title;
        this.content = content;
    }

    public long getId() { return id; }
    public String getLangCode() { return langCode; }
    public String getContent() { return content; }
    
    /**
     * Carrot2/ling03g Document interface implementation.
     * @param bc 
     */
    @Override
    public void visitFields(BiConsumer<String, String> bc)
    {
          bc.accept("id", Long.toString(this.id));
          bc.accept("langCode", this.langCode);
          bc.accept("title", this.title);
          bc.accept("content", this.content);
    }
}
