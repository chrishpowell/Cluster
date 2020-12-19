/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import java.util.List;

/**
 *
 * @author chrispowell
 */
public class ListClass2Array
{
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        List<DClass> lcd = List.of(new DClass("One",0.112d),new DClass("Two",0.248d),new DClass("Three",0.777d),new DClass("Four",0.505d),new DClass("Five",0.9876d));
        double[] dcd = lcd.stream().mapToDouble(m -> m.getScore()).toArray();
        
        for( double d: dcd )
            System.out.println("> " +d);
    }
}

//------------------------------------------------------------------------------
class DClass
{
    private final String    name;
    private final double    score;

    public DClass(String name, double score)
    {
        this.name = name;
        this.score = score;
    }

    public double getScore() { return score; }
}
