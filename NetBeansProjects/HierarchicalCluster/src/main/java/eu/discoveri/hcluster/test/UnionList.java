/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.hcluster.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import eu.discoveri.fptree.Item;
import java.util.Arrays;


/**
 *
 * @author chrispowell
 */
public class UnionList
{
    public static void main(String[] args)
    {
        List<Item> li = new ArrayList<>();
        Item a = new Item("a",1);
        Item b = new Item("b",2);
        Item c = new Item("c",3);
        Item d = new Item("d",4);
        Item z = new Item("z",26);
        
        List<Item> slm1 = List.of(a,b,d); // Immutable
//        List<Item> sl0 = Arrays.asList(z,a,b,d); // Immutable
        List<Item> sl0 = new ArrayList<>();
        sl0.add(z);
        sl0.add(a);
        sl0.add(b);
        sl0.add(d);
        sl0.remove(z);

        List<Item> slA = Collections.EMPTY_LIST;
//        Collections.addAll(slA = new ArrayList<>(),b);
        List<Item> sl1 = Collections.EMPTY_LIST;
        Collections.addAll(sl1 = new ArrayList<>(),b,c,d);
        List<Item> sl9 = Collections.EMPTY_LIST;
        Collections.addAll(sl9 = new ArrayList<>(),b);
        List<Item> sl8 = Collections.EMPTY_LIST;
        Collections.addAll(sl8 = new ArrayList<>(),b,c);
        List<Item> sl2 = Collections.EMPTY_LIST;
        Collections.addAll(sl2 = new ArrayList<>(),z);
        
        List<Item> sl7 = Arrays.asList(b,d);
//        List<Item> sl6 = List.of(a,b);

        List<List<Item>> sll = Collections.EMPTY_LIST;
        Collections.addAll(sll = new ArrayList<>(),sl1);
        
        System.out.println("   sl0 is modifiable? " +sl0.getClass().isInstance(new ArrayList<Item>()));
        System.out.println("   sl0 is unmodifiable? " +sl0.getClass().isInstance(Collections.unmodifiableList(new ArrayList<Item>())));
        System.out.println("   sl1 is modifiable? " +sl1.getClass().isInstance(new ArrayList<Item>()));
        System.out.println("   sl1 is unmodifiable? " +sl1.getClass().isInstance(Collections.unmodifiableList(new ArrayList<Item>())));

        sl0.retainAll(sl1);
        
//        System.out.println("..> " +(sl0.get(2).equals(sl1.get(2))));
//        
//        for( List<Item> litem: sll )
//        {
//            litem.stream().filter(i -> ( !li.contains(i) )).forEachOrdered(i -> {
//                li.add(i);
//            });
//        }
        // Doesn't do equal test on adding!  Hence above.
//        for( List<Item> litem: sll )
//        {
//            li.addAll(litem);
//        }

        sl0.forEach(i -> {
            System.out.print(" " +i.getName());
        });
        
        System.out.println("\r\n------------Empty (union) test------------------");
        sl0.addAll(slA);
        sl0.forEach(i -> {
            System.out.println("0> " +i.getName());
        });
    }
}
