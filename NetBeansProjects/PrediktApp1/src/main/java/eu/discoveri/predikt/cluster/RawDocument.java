/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.cluster;

import java.net.URI;
import java.net.URISyntaxException;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class RawDocument
{
    private DocumentCategory    docCategory;
    private URI                 source;
    private String              text;

    /**
     * Constructor.
     * @param docCategory
     * @param source
     * @param text 
     */
    public RawDocument(DocumentCategory docCategory, URI source, String text)
    {
        this.docCategory = docCategory;
        this.source = source;
        this.text = text;
    }

    /**
     * Constructor.  No categorisation.
     * @param source
     * @param text 
     */
    public RawDocument(URI source, String text)
    {
        this(new DocumentCategory(),source,text);
    }
    
    /**
     * Constructor. No source.
     * @param docCategory
     * @param text
     * @throws URISyntaxException 
     */
    public RawDocument(DocumentCategory docCategory,String text)
            throws URISyntaxException
    {
        this(docCategory,new URI(""),text);
    }

    /*
     * Getters.
     */
    public DocumentCategory getDocCategory() { return docCategory; }
    public URI getSource() { return source; }
    public String getText() { return text; }
}
