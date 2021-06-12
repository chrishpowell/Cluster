/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 *
 * @author chrispowell
 */
public class Item implements Comparable<Item>
{
    // Item (word) name
    private final String  name;
    // Count of Item (word) across doc. set
    private final int     count;
    
    // List of unique item names across all docs
    public static List<Item> uniqueItemList = new ArrayList<>();


    /**
     * Constructor.
     * @param name
     * @param count 
     */
    public Item(String name, int count)
    {
        this.name = name;
        this.count = count;
    }
    
    /**
     * Constructor.
     * 
     * @param name 
     */
    public Item(String name)
    {
        this(name,1);
    }
    
        
    /**
     * Set list of unique items (words) across all docs. Input usually
     * determined via SQL call.
     * 
     * @param uItemList list of unique items.
     */
    public static void setUniqueItemList( List<Item> uItemList ) { uniqueItemList = uItemList; }
    
    /**
     * Get unique list of items across all docs. Usually set via SQL call.
     * @return 
     */
    public static List<Item> getUniqueItemList() { return uniqueItemList; }

    /**
     * List of (unique) items in given  set of documents.  In lieu of DB function
     * call.
     * 
     * @param docs
     * @return 
     */
    public static List<Item> uniqueItemList( List<Doc> docs )
    {
        docs.forEach(d -> {
            d.getLi().stream()
                    .filter(i -> (!uniqueItemList.contains(i)))
                    .forEachOrdered(i -> {uniqueItemList.add(i);});
        });

        return uniqueItemList;
    }

    /**
     * Get name.
     * @return 
     */
    public String getName() { return name; }
    
    /**
     * Get count.
     * @return 
     */
    public int getCount() { return count; }

    /**
     * Hash function.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }

    /**
     * Equality based on Item names.
     * 
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final Item other = (Item) obj;
        return this.name.equals(other.name);
    }

    /**
     * Comparison of items based on Item count.  Count is the count of this
     * Item/word across the doc. set.
     * 
     * @param i
     * @return 
     */
    @Override
    public int compareTo(Item i)
    {
        return this.count - i.count;
    }
    
    /**
     * Display name, count, support count. That is, count of this Item/word across
     * doc. set and support count (see: SupportCount::globalSupp()) - which may
     * be null before having populated support counts.  Note: support count is
     * the percentage of docs in the doc list/set that contain the item.
     * Format: name (count)[support count]
     * 
     * @return 
     */
    @Override
    public String toString()
    {
        // NB: Relies on global support having been populated (otherwise will show null)
        return name +"("+ count+ ")[" +SupportCount.getGlobalSupp().get(this)+ "]";
    }
}
