/*
 */
package eu.discoveri.webscrape;

import java.util.Formatter;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class PadTest
{
    public static void main(String[] args)
    {
        StringBuilder sc = new StringBuilder();
        Formatter fmt = new Formatter(sc);
        
        sc.append("Fred ");
        int level = 19; 
        fmt.format("|%"+level+"s|: %d"," ",12); // This is like SB appending
        sc.append(" Bill");
        fmt.format(" %f ", Math.PI); // Append again
        
        String test = "test";
        fmt.format(" %s%n", test);
        
        System.out.println(sc.toString());
    }
}
