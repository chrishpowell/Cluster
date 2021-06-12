/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.hcluster.test;

import eu.discoveri.fptree.SubSetListIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author chrispowell
 * @param <C>
 */
public class ComboTest<C>
{
    public Stream<List<C>> combos(Set<C> values)
    {
        if (values.isEmpty())
            return Stream.of(new ArrayList<>());
        else
            return values.stream().flatMap(value -> 
                combos(values.stream()
                    .filter(v -> !v.equals(value))
                    .collect(Collectors.toSet())).peek(r -> r.add(value)));
    }
    
    public Stream<List<C>> subsets(List<C> values)
    {
        SubSetListIterator ssi = new SubSetListIterator(values);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ssi, 0), false);
    }
            
    public static void main(String[] args)
    {
        ComboTest<Item> ct = new ComboTest();
        Set<Item> si = Collections.EMPTY_SET;
        Collections.addAll(si = new HashSet<>(),new Item("a",1),new Item("b",2),new Item("c",3),new Item("d",4));
        List<Item> sl = Collections.EMPTY_LIST;
        Collections.addAll(sl = new ArrayList<>(),new Item("a",1),new Item("b",2),new Item("c",3),new Item("d",4));

        Stream<List<Item>> li = ct.subsets(sl);
        li.forEach((ll -> {
            ll.forEach(lll -> {
                System.out.print(" " +lll.getName());
            });
            System.out.println();
        }));
    }
}

class Item
{
    private String  name;
    private int     count;

    public Item(String name, int count)
    {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public int getCount() { return count; }
}
