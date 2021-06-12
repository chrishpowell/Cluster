/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class GlobalSupport
{
    private final List<Doc> allDocs;

    /**
     * Constructor.
     * @param allDocs 
     */
    public GlobalSupport(List<Doc> allDocs)
    {
        this.allDocs = allDocs;
    }
    
    
    /**
     * List of (unique) items in this list of docs (usually a cluster)..
     * 
     * @return 
     */
    public List<Item> uniqueItemList()
    {
        List<Item> uniqueItemList = new ArrayList<>();

        allDocs.forEach(d -> {
            d.getLi().stream()
                    .filter(i -> (!uniqueItemList.contains(i)))
                    .forEachOrdered(i -> {uniqueItemList.add(i);});
        });

        return uniqueItemList;
    }
    
    /**
     * Calculate cluster support (count).
     * The cluster support of an item in this cluster is the percentage of docs.
     * in this cluster that contain that item.
     * 
     * @return 
     */
    public Map<Item,Integer> globalSuppCnt()
    {
        Map<Item,Integer>   gSupp = new HashMap<>();
        
        // Count items
        uniqueItemList().forEach(itm -> {
            allDocs.forEach(d1 -> {
                if( d1.getLi().contains(itm) )
                {
                    if( gSupp.containsKey(itm) )
                    {
                        int itmCount = gSupp.get(itm);
                        gSupp.replace(itm,++itmCount);
                    }
                    else
                        gSupp.put(itm, 1);
                }
            });
        });
        
        return gSupp;
    }
    
    /**
     * Calculate cluster support (percentage) from count. 
     * 
     * @param gsCount
     * @return 
     */
    public Map<Item,Float> globalSuppPct( Map<Item,Integer> gsCount )
    {
        Map<Item,Float> gsPercent = new HashMap<>();
        float docCount = (float)allDocs.size();
        
        gsCount.forEach((c,p) -> {
            gsPercent.put(c, p/docCount);
        });
        
        return gsPercent;
    }
}
