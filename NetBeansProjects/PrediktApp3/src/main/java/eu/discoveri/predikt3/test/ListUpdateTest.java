/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class ListUpdateTest
{
    public static void main(String[] args)
    {
        Map<Integer,List<StrDbl>> msd = new HashMap<>();
        List<StrDbl> sd = new ArrayList<>();
        sd.add(new StrDbl("fred",1.d));
        sd.add(new StrDbl("bill",0.5d));
        sd.add(new StrDbl("henry",0.334d));
        sd.add(new StrDbl("george",0.01234d));
        List<StrDbl> sd1 = new ArrayList<>();
        sd1.add(new StrDbl("fred",0.75d));
        sd1.add(new StrDbl("bill",0.876d));
        sd1.add(new StrDbl("henry",0.0012d));
        sd1.add(new StrDbl("george",1.01d));
        
        msd.put(4, sd);
        msd.put(1, sd);
        msd.put(3, sd1);
        msd.put(2, sd);
        
        msd.forEach((k,v) -> {
            System.out.println("-> " +k);
            v.forEach(l -> {
                System.out.println("  > " +l.getDbl());
            });
        });

        System.out.println("\r\nDouble entry values:");
        
        msd.entrySet().forEach(m -> {
            m.getValue().forEach(lsd -> {
                double d = lsd.getDbl();
                lsd.setDbl(d*2.d);
            });
        });
        
        msd.forEach((k,v) -> {
            System.out.println("-> " +k);
            v.forEach(l -> {
                System.out.println("  > " +l.getDbl());
            });
        });
    }
}

class StrDbl
{
    private String  str;
    private double  dbl;

    public StrDbl(String str, double dbl)
    {
        this.str = str;
        this.dbl = dbl;
    }

    public double getDbl() { return dbl; }
    public void setDbl(double dbl) { this.dbl = dbl; }
}
