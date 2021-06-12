/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.List;

/**
 *
 * @author chrispowell
 */
public class Doc
{
    // Document name
    private final String      name;
    // Items (eg: words) that this document contains
    private final List<Item>  li;

    public Doc(String name, List<Item> li)
    {
        this.name = name;
        this.li = li;
    }

    public String getName() { return name; }
    public List<Item> getLi() { return li; }
    public int getItemSize() { return li.size(); }
    
    public void dumpDoc()
    {
        System.out.println("Doc name: " +name+ ", num items: " +li.size());
        li.forEach(i -> System.out.print(" " +i));
        System.out.println();
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
