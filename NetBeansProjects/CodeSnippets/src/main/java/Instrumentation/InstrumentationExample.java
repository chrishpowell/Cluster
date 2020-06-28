/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package Instrumentation;

import java.util.ArrayList;
import java.util.List;
import eu.discoveri.instrumentation.InstrumentationAgent;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class InstrumentationExample
{
    public static void printObjectSize(Object object)
    {
        System.out.println("Object type: " + object.getClass() +
          ", size: " + InstrumentationAgent.getObjectSize(object) + " bytes");
    }
 
    public static void main(String[] arguments)
    {
        String emptyString = "";
        String string = "Estimating Object Size Using Instrumentation";
        
        String[] stringArray = { emptyString, string, "eu.discoveri" };
        String[] anotherStringArray = new String[100];
        List<String> stringList = new ArrayList<>();
        List<Double> l1 = Arrays.asList(1.1d,2.2d), l2 = Arrays.asList(3.3d,4.4d);
        List<String> ls = Arrays.asList("A","B");
        Map<String,RObject> map = Stream.of(new Object[][] {
            {"A",new RObject("AA",l1)},
            {"B",new RObject("BB",l2)}
        }).collect(Collectors.toMap(data->(String)data[0],data->(RObject)data[1]));
        RObject rob = new RObject("CC",l1);
        Map<Integer,Double> aa = Stream.of(new Object[][] {
            {0,0.0},{1,1.1},{2,2.2},{3,3.3},{4,4.4},
            {5,5.5},{6,6.6},{7,0.0},{8,1.1},{9,2.2},
            {10,3.3},{11,4.4},{12,5.5},{13,6.6},
            {14,7.7},{15,0.0},{16,1.1},{17,2.2},
            {18,3.3},{19,4.4},{20,5.5},{21,6.6},
            {22,0.0},{23,1.1},{24,2.2},{25,3.3},
            {26,4.4},{27,5.5},{28,6.6},{29,7.7}
        }).collect(Collectors.toMap(data->(Integer)data[0],data->(Double)data[1]));
        StringBuilder stringBuilder = new StringBuilder(100);
        int maxIntPrimitive = Integer.MAX_VALUE;
        int minIntPrimitive = Integer.MIN_VALUE;
        Integer maxInteger = Integer.MAX_VALUE;
        Integer minInteger = Integer.MIN_VALUE;
        long zeroLong = 0L;
        double zeroDouble = 0.0;
        boolean falseBoolean = false;
        Object object = new Object();
 
        class EmptyClass {}
        EmptyClass emptyClass = new EmptyClass();
 
        class StringClass
        {
            public String s;
        }
        StringClass stringClass = new StringClass();
 
        printObjectSize(emptyString);
        printObjectSize(string);
        printObjectSize(stringArray);
        printObjectSize(map);
        printObjectSize(aa);
        printObjectSize(rob);
        printObjectSize(l1);
        printObjectSize(anotherStringArray);
        printObjectSize(stringList);
        printObjectSize(stringBuilder);
        printObjectSize(maxIntPrimitive);
        printObjectSize(minIntPrimitive);
        printObjectSize(maxInteger);
        printObjectSize(minInteger);
        printObjectSize(zeroLong);
        printObjectSize(zeroDouble);
        printObjectSize(falseBoolean);
        printObjectSize(Day.TUESDAY);
        printObjectSize(object);
        printObjectSize(emptyClass);
        printObjectSize(stringClass);
    }
 
    public enum Day
    {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}

class RObject
{
    private final String          s;
    private final List<Double>    d;

    public RObject(String s, List<Double> d)
    {
        this.s = s;
        this.d = d;
    }

    public String getS() { return s; }
    public List<Double> getD() { return d; }
}
