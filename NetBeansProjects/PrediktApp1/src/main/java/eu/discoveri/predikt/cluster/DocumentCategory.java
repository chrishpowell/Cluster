/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.cluster;

import eu.discoveri.predikt.graph.GraphEntity;

/**
 * Document category from DenseFly at SentenceNode level.
 * 
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DocumentCategory extends GraphEntity
{
    // Number or category for a document
    private final int categoryNum;

    /**
     * Constructor.
     * @param categoryNum Number or category for a document
     * @param name Name of category (eg:"Ships", "Ceiling wax")
     * @param namespace  Namespace of name.
     */
    public DocumentCategory(int categoryNum, String name, String namespace)
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

    public DocumentCategory()
    {
        this(-1,"","");
    }

    /**
     * Category number.
     * @return 
     */
    public int getCategoryNum() { return categoryNum; }
}
