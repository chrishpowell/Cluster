/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.cluster;

import eu.discoveri.predikt3.graph.GraphEntity;

/**
 * Document category from DenseFly at SentenceNode level.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DocumentCategory extends GraphEntity
{
    // Number or category for a document
    private final SimilarCategory sc;
    private static final SimilarCategory nullCat = new SimilarCategory(-1,""); 

    /**
     * Constructor.
     * @param name Name of category (eg:"Ships", "Ceiling wax")
     * @param namespace  Namespace of name.
     * @param sc
     */
    public DocumentCategory(String name, String namespace, SimilarCategory sc)
    {
        super(name, namespace);
        this.sc = sc;
    }
    
    /**
     * Constructor.

     * @param sc
     * @param name Name of category (eg:"Ships", "Ceiling wax")
     */
    public DocumentCategory(String name, SimilarCategory sc )
    {
        this(name,"eu.discoveri.predikt",sc);
    }
    
    /**
     * Constructor.
     * @param sc
     */
    public DocumentCategory(SimilarCategory sc)
    {
        this("","eu.discoveri.predikt",sc);
    }
    
    /**
     * Constructor.  Null category.
     */
    public DocumentCategory()
    {
        this("","",nullCat);
    }

    /**
     * Get the category for this document.
     * @return 
     */
    public SimilarCategory getSc() { return sc; }
}
