/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;


/**
 *
 * @author chrispowell
 */
public class MaxInCollection
{
    public static void main(String[] args)
    {
        Set<Maxes> sm = Set.of(new Maxes("a",2.0d), new Maxes("b",2.0d),new Maxes("c",2.5d),new Maxes("d",2.0d));
        OptionalDouble od = sm.stream().mapToDouble(x -> x.getVal()).max();
        
        System.out.println("od: " +od.getAsDouble());
        
        List<Map<Maxes,Double>> lmmd = List.of(Map.of(new Maxes("a",2.0d),2.0d, new Maxes("b",2.0d),2.0d, new Maxes("c",2.5d),2.5d, new Maxes("d",2.0d),2.0d));

//        List<Optional<Map.Entry<Maxes,Double>>> od1 = lmmd.stream().map(m -> m.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue))).collect(Collectors.toList());
//        lmmd.stream().map(m -> m.entrySet().stream().max((Map.Entry(k,v) e1, Map.Entry(k,v) e2) -> e1.getValue().compareTo(e2.getValue()))));
        double[] vMax = {Double.MIN_VALUE};
        Maxes m = new Maxes("",0.0d);
        lmmd.forEach(lm -> {
            lm.forEach((k,v) -> {
                if( v > vMax[0] )
                {
                    vMax[0] = v;
                    m.setVal(v); m.setName(k.getName());
                }
            });
        });
        System.out.println("vMax = " +m.getName());
    }
}

class Maxes
{
    private String  name;
    private double  val;

    public Maxes(String name, double val)
    {
        this.name = name;
        this.val = val;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getVal() { return val; }
    public void setVal(double val) {this.val = val; }
}
