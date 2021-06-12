/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author chrispowell
 */
public class SSItest
{
        
    
    /**
     * M A I N (Test)
     * ==============
     * @param args 
     */
    public static void main(String[] args)
    {
        List<X> ls = List.of(new X(1,"a"),new X(2,"b"),new X(3,"c"),new X(4,"d"));
        SubSetListIterator ssi = new SubSetListIterator(ls);
        Spliterator<List<X>> sls = ssi.splitUp();

        // Either this...
//        while(sls.tryAdvance(sl ->
//        {
//            Set<X> lset = new HashSet<>(sl);
//            System.out.print(lset);
//        })) System.out.print(" ");
//        System.out.println("\r\n---------------");
        
        // ... or this (could use Supplier)
        Stream<List<X>> ssls = StreamSupport.stream(sls, false).dropWhile(item -> item.isEmpty()); // No empty set pls
        ssls.forEach(sl ->
        {
            Set<X> lset = new HashSet<>(sl);
            System.out.print(lset+" ");
        });
    }
}

//------------------------------------------------------------------------------
class X
{
    private int     value;
    private String  name;

    public X(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public int getValue() { return value; }
    public String getName() { return name; }
    
    @Override
    public String toString()
    {
        return name +":"+ value;
    }
}
