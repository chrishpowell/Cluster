/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import eu.discoveri.utils.Subsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 * @author chrispowell
 */
public class SupportCount
{
    // Global support (score) per item
    private static Map<Item,Double> globalSupp = new HashMap<>();
    
    // Set of freq. items (sorted incr. support)
    private static List<Item>  freqItemSupIncr;
    private static List<Item>  freqItemSupDesc;
    
    
    /**
     * Get global support per Item
     * @return 
     */
    public static Map<Item,Double> getGlobalSupp() { return globalSupp; }
    
    /**
     * Calculate document support (count). The support count of an item (for
     * this list/set of docs) is the count of docs in the given list/set that
     * contain the item.
     * 
     * @param docs the list/set of docs
     * @param items the list of items being counted in the set of docs
     * @return Item to doc count containing the item
     */
    public static Map<Item,Integer> supportCount( Collection<Doc> docs, List<Item> items )
    {
        Map<Item,Integer>   cSupp = new HashMap<>();
        
        // Count docs that contain each item
        items.forEach(itm -> {
            docs.forEach(d1 -> {
                if( d1.getLi().contains(itm) )
                {
                    if( cSupp.containsKey(itm) )
                    {
                        int itmCount = cSupp.get(itm);
                        cSupp.replace(itm,++itmCount);
                    }
                    else
                        cSupp.put(itm, 1);
                }
            });
        });
        
        return cSupp;
    }
    
    /**
     * Calculate documents support (percentage) from count. The support percentage
     * of an item (for this list/set of docs) is the percentage of docs in this
     * list/set that contain that item.
     * 
     * @param docs list/set of docs
     * @param items
     * @param minSupp Minimum percentage for support
     * @return Item to doc percentage (of the given set of docs) containing the item
     */
    public static Map<Item,Double> supportPercent( Collection<Doc> docs, List<Item> items, double minSupp )
    {           
        Map<Item,Double> csPercent = new HashMap<>();
        
        supportCount(docs,items).forEach((i,c) -> {
            double pctCount = c/(float)docs.size();
            if( pctCount >= minSupp )
                csPercent.put(i, pctCount);
        });
        
        return csPercent;
    }
    
    /**
     * Global support calculation (%) per item in a given document set.  Support
     * is added to map if item calculates to a value above the given minimum
     * support value (%): Constants.MINGSUP.
     * 
     * @param lds document set (list)
     * @param uniqueItemList list of unique items
     * @return 
     * @throws eu.discoveri.fptree.InvalidSupportPercentageException 
     */
    public static Map<Item,Double> globalSupport( List<Doc> lds, List<Item> uniqueItemList )
            throws InvalidSupportPercentageException
    {
        // Check we're in correct % range.
        if( Constants.MINGSUP <= 0.d || Constants.MINGSUP >100.d )
            throw new InvalidSupportPercentageException("Input %: " +Constants.MINGSUP);
                
        globalSupp = supportPercent(lds, uniqueItemList, Constants.MINGSUP);
        
        return globalSupp;
    }
    
    /**
     * Set frequent item support (count).
     * *** Assume already sorted in desc. order (@TODO: ************ Production)
     * 
     * @param freqItemSup 
     */
    public static void setFreqItemSup( List<Item> freqItemSup )
    {
        // Freq items, ascending order
        freqItemSupIncr = new ArrayList(freqItemSup);
        Collections.sort(freqItemSupIncr);
        
        // Now add list freq items (in desc order)
        freqItemSupDesc = new ArrayList(freqItemSup);
        Collections.sort(freqItemSupDesc,Collections.reverseOrder());
    }
    
    public static List<Item> getFreqItemSupIncr() { return freqItemSupIncr; }
    public static List<Item> getFreqItemSupDesc() { return freqItemSupDesc; }
    
    /**
     * Global Support sorted by % value increasing
     * @return 
     */
    public static Map<Item,Double> globalSuppIncr()
    {
        return globalSupp.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(oldVal,newVal)->oldVal,LinkedHashMap::new));
    }

    /**
     * Global Support sorted by % value decreasing
     * @return 
     */
    public static Map<Item,Double> globalSuppDesc()
    {
        return globalSupp.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((e1,e2) -> e2.compareTo(e1)))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(oldVal,newVal)->oldVal,LinkedHashMap::new));
    }
    
    /**
     * Brute force mechanism for creating freq.itemsets.
     * Compute support (count) for "candidates" (all subsets of unique
     * itemsets) of each tuple in database.  Eg, unique itemset [abc] generates
     * candidates (subsets 2^n-1): [a][b][c][ab][ac][bc][abc].  Support could
     * be calculated from eg, doc0: [ab], doc1: [ac] giving a=2,b=1,c=1,ab=1,ac=1.
     * 
     * @param lds
     * @return 
     */
    public static Map<String,FISets> bruteForceFreqItems( List<Doc> lds )
    {
        // For each document...
        lds.forEach(d -> {
            System.out.println("--> Doc: " +d.getName());
            
            // Subsets of items of this doc eg, doc0: [[a][b][ab]]
            Set<Set<Item>> sli = Subsets.subsetsSet(d.getLi());

            // Count the subsets
            sli.forEach(dss -> {
                FISets.incrMap(dss);
            });
        });

        
        return FISets.getMap();
    }

    
//------------------------------------------------------------------------------
    /**
     * Dump all values of global support (per item)
     */
    public static void dumpGlobalSupport()
    {
        globalSupp.forEach((i,d) -> {
            System.out.println("  Item: " +i+ ", supp%: " +d);
        });
    }
}

//==============================================================================
/*
 * Support (counts) for candidates (all subsets of unique itemsets)
 */
class FISets
{
    private static Map<String,FISets>   mFis = new HashMap<>();
    
    private Set<Item>   si; // Eg:[a,b,c], key=abc
    private long        count;

    public FISets(Set<Item> si, long count)
    {
        this.si = si;
        this.count = count;
    }

    public Set<Item> getSi() { return si; }
    public long getCount() { return count; }
    public void setCount( long newVal ) { count = newVal; }
    public static Map<String,FISets> getMap() { return mFis; }
    
    /**
     * Increment candidate count
     * @param li 
     */
    public static void incrMap( Set<Item> li )
    {
        String key = li.stream().map(s -> s.getName()).collect(Collectors.joining());
        System.out.println("...> Key: " +key);
        if( mFis.containsKey(key) )
        {
            FISets fis = mFis.get(key);
            fis.setCount(fis.getCount()+1);
            mFis.put(key, fis);
        }
        else
            mFis.put(key, new FISets(li,1));
    }
}
