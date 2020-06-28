/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class HZCTest
{
    // HZC
    private static Config cfg = new Config();
    private static HazelcastInstance hi = Hazelcast.newHazelcastInstance(cfg);
    
    // Non HZC mapping
    private static Map<AbstractMap.SimpleEntry<AClass,AClass>,Double>  cws = new HashMap<>();
    
    // HZCmapping
    private static Map<AbstractMap.SimpleEntry<AClass,AClass>,Double>  commonWords = hi.getMap("commonwords");

    
    /**
     * Map.Entry method.
     * @param k
     * @param v
     * @return 
     */
    private static AbstractMap.SimpleEntry<AClass,AClass> asme( AClass k, AClass v )
    {
        return new AbstractMap.SimpleEntry<>(k,v);
    }

    
    /**
     * M A I N
     * =======
     */
    public static void main(String[] args)
    {
        // Nothing to do with CWs
//        AbstractMap.SimpleEntry<AClass,AClass> a1 = asme( new AClass("A11",1),new AClass("A12",2) );
//        AbstractMap.SimpleEntry<AClass,AClass> a2 = asme( new AClass("A21",2),new AClass("A22",4) );
//        AbstractMap.SimpleEntry<AClass,AClass> a3 = asme( new AClass("A11",3),new AClass("A32",6) );

        // Non HZC
        cws.put(new AbstractMap.SimpleEntry(new AClass("Z11",11),new AClass("Z12",12)), 1.2d);
        cws.put(new AbstractMap.SimpleEntry(new AClass("Z21",21),new AClass("Z22",22)), 2.2d);
        cws.put(new AbstractMap.SimpleEntry(new AClass("Z31",31),new AClass("Z32",32)), 3.2d);
        
        System.out.println("cws size: " +cws.size());
        cws.forEach((k,v) -> {
            System.out.println("cw-> " +k.getKey()+ " : " +k.getValue());
        });
        
        // HZC
        commonWords.put(new AbstractMap.SimpleEntry(new AClass("Z11",11),new AClass("Z12",12)), 1.2d);
        commonWords.put(new AbstractMap.SimpleEntry(new AClass("Z21",21),new AClass("Z22",22)), 2.2d);
        commonWords.put(new AbstractMap.SimpleEntry(new AClass("Z31",31),new AClass("Z32",32)), 3.2d);
        
        System.out.println("commonWords size: " +commonWords.size());
        commonWords.forEach((k,v) -> {
            System.out.println("CW-> " +k.getKey()+ " : " +k.getValue());
        });

        hi.getLifecycleService().shutdown();
    }
}

//------------------------------------------------------------------------------
class AClass implements Serializable
{
    private final String  ss;
    private final int     ii;

    public AClass(String ss, int ii)
    {
        this.ss = ss;
        this.ii = ii;
    }

    public String getSs() { return ss; }
    public int getIi() { return ii; }
    
    @Override
    public String toString()
    {
        return ss+"/"+ii;
    }
}
