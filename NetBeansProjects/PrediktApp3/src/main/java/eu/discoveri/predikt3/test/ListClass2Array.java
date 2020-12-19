/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import java.util.Arrays;
import java.util.List;


/**
 *
 * @author chrispowell
 */
public class ListClass2Array
{
    public static void main(String[] args)
    {
        List<TwoString> l2s = Arrays.asList(new TwoString("A1","A2"),new TwoString("Z9","X9"),new TwoString("fred","bill"));
        int asiz = l2s.size();
        String[][] sa = new String[asiz][asiz];

        
        System.out.println("..> " +sa[0][0]);
    }
}

//------------------------------------------------------------------------------
class TwoString
{
    private final String  s1,s2;

    public TwoString(String s1, String s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }

    public String getS1() { return s1; }
    public String getS2() { return s2; }
}
