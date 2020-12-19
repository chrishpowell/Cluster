/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.cluster;

/**
 * Categories of documents scraped from Web.
 * 
 * @author chrispowell
 */
public class SimilarCategory
{
    // Number/category for a document
    private final long      categoryNum;
    private final String    categoryDesc;

    /**
     * Constructor.
     * @param categoryNum Number or category for a document
     * @param categoryDesc Description of this category
     */
    public SimilarCategory(long categoryNum, String categoryDesc)
    {
        this.categoryNum = categoryNum;
        this.categoryDesc = categoryDesc;
    }
    
    
    /**
     * Category number.
     * @return 
     */
    public long getCategoryNum() { return categoryNum; }

    /**
     * Category description.
     * @return 
     */
    public String getCategoryDesc() { return categoryDesc; }
}
