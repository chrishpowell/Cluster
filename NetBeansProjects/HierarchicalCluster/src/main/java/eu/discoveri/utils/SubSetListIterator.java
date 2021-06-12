/*
 * SubsetIterator
 */
package eu.discoveri.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;


/**
 * Split set of T into all subsets and implement iterator over result.  Note
 * count is 2^n (or 2^n-1 without empty set).
 * 
 * @author chrispowell
 * @param <T>
 */
public class SubSetListIterator<T> implements Iterator<List<T>>
{
    // Internal bit vector, representing the subset
    private final int[]     bitVector;
    // List of items
    private final List<T>   items;
    // Length of list
    private final int       length;
    // Subsets
    private final List<T>   currentSubSet;
    private long            currentIndex;
    
    
    /**
     * Constructor.
     * @param items 
     */
    public SubSetListIterator( List<T> items )
    {
        this.items = items;
        currentSubSet = new ArrayList<>();
        length = items.size();
        bitVector = new int[length+2];
        currentIndex = 0;
    }
    
    /**
     * Has a next item?
     * @return 
     */
    @Override
    public boolean hasNext()
    {
        return bitVector[length + 1] != 1;
    }

    /**
     * Process next item.
     * @return 
     */
    @Override
    public List<T> next()
    {
        currentIndex++;
        currentSubSet.clear();
        for( int index = 1; index <= length; index++ )
        {
            if( bitVector[index] == 1 )
            {
                T value = items.get(index - 1);
                currentSubSet.add(value);
            }
        }
        
        int i = 1;
        while( bitVector[i] == 1 )
        {
            bitVector[i] = 0;
            i++;
        }
        bitVector[i] = 1;

        return new ArrayList<>(currentSubSet);
    }
    
    /**
     * Create instance of Spliterator over elements of the (instantiated) class.
     * 
     * @return Object to allow traversal of list of all subsets (of type T).
     */
    public Spliterator<List<T>> splitUp()
    {
        return Spliterators.spliteratorUnknownSize(this, 0);
    }
    
    /**
     * Convert the list of subsets to a Set.
     * 
     * @return 
     */
    public Set<T> convertListToSet()
    {
        return currentSubSet.stream().collect(Collectors.toSet());
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public String toString()
    {
        return "[" +currentIndex+ "," +currentSubSet+ "]";
    }
}
