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
public class Docmnt
{
    private String      name;
    private List<Item>  words;

    /**
     * Constructor.
     * 
     * @param name
     * @param words 
     */
    public Docmnt(String name, List<Item> words)
    {
        this.name = name;
        this.words = words;
    }

    public String getName() { return name; }
    public List<Item> getWords() { return words; }
}
