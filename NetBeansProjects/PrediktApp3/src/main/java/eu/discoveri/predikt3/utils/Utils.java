/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.utils;

import eu.discoveri.predikt3.sentences.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Utils
{
    /**
     * Remove duplicates from list1 and its associated list2. Note: Custom classes
     * of type T should express equals()/hashCode() otherwise duplicates may not
     * be compared and hence deemed not to exist.  Note also that a 'copy' List
     * is required for the List.remove() method to work.  Eg:
     * List&left;X&right; x = somemethod.genX; // new ArrayList<>() for X in somemethod
     * List&left;Y&right; y = new ArrayList<>(othermethod.genY()); // No new ArrayList in
     * othermethod.
     * 
     * The arguments themselves are
     * altered, there are no return values.
     * 
     * @param <T> (List of) type implementing equals()
     * @param <U> (List of) type
     * @param list1 'Main' list
     * @param list2 List in one-to-one relation with list1
     */
    public static <T,U> void removeDupsFromLists( List<T> list1, List<U> list2 )
    {
        List<T> dups = new ArrayList<>();
        int ii = 0;
        for( Iterator<T> iter = list1.listIterator(); iter.hasNext(); )
        {
            T l = iter.next();
            if(!dups.contains(l))
            {
                dups.add(l);                    // Add unique entry
                ii++;
            }
            else
            {                                   // It's a duplicate, remove
                iter.remove();
                list2.remove(ii);
            }
        }
    }
    
    /**
     * Find an entry in list.  Finds first, duplicates will be ignored.
     * @param <T>
     * @param list
     * @param entry
     * @return Optional entry
     */
    public static <T> Optional<T> findListEntry( List<T> list, T entry )
    {
        return list.stream().filter(e -> e.equals(entry)).findFirst();
    }
    
    /**
     * Match for Token given token name.
     * 
     * @param list
     * @param name
     * @return 
     */
    public static Optional<Token> findListEntry( List<Token> list, String name )
    {
        return list.stream().filter(e -> e.getToken().equals(name)).findFirst();
    }
    
    
    /**
     * M A I N (Test)
     * ==============
     * @param args 
     */
    public static void main(String[] args)
    {
        List<TTest> tt = Arrays.asList(new TTest("fred",1.0d),new TTest("bill",2.0d),new TTest("henry",3.0d),new TTest("george",4.0d),new TTest("arkady",5.0d));
        
        findListEntry(tt,new TTest("george",3.14d)).ifPresent(e -> System.out.println("Found in list: " +e.toString()));
        if( !findListEntry(tt,new TTest("voldemar",3.14d)).isPresent() )
            System.out.println("NOT Found in list: voldemar");
    }
}

//----------------[Test class(with equals())]----------------------
class TTest
{
    private String  s;
    private double  d;

    public TTest(String s, double d)
    {
        this.s = s;
        this.d = d;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.s);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final TTest other = (TTest)obj;
        return other.s.equals(s);
    }
    
    @Override
    public String toString()
    {
        return s;
    }
}