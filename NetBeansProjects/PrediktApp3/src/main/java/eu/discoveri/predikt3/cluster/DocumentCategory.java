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
    private final long categoryNum;

    /**
     * Constructor.
     * @param categoryNum Number or category for a document
     * @param name Name of category (eg:"Ships", "Ceiling wax")
     * @param namespace  Namespace of name.
     */
    public DocumentCategory(long categoryNum, String name, String namespace)
    {
        super(name, namespace);
        this.categoryNum = categoryNum;
    }
    
    /**
     * Constructor.
     * @param categoryNum Number or category for a document
     * @param name Name of category (eg:"Ships", "Ceiling wax")
     */
    public DocumentCategory(int categoryNum, String name)
    {
        this(categoryNum,name,"eu.discoveri.predikt");
    }

    /**
     * Constructor.
     * @param categoryNum 
     */
    public DocumentCategory(long categoryNum)
    {
        this(categoryNum,"","eu.discoveri.predikt");
    }

    /**
     * Category number.
     * @return 
     */
    public long getCategoryNum() { return categoryNum; }
}
