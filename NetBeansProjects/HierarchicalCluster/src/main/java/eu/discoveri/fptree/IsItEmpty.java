/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class IsItEmpty
{
    public static void main(String[] args)
    {
        List<E> le = List.of((E)Collections.emptyList(),new E(1,"1"),new E(2,"2"),new E(3,"3"));
        
    }
}

//------------------------------------------------------------------------------
class E
{
    private int     value;
    private String  name;

    public E(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public int getValue() { return value; }
    public String getName() { return name; }
}