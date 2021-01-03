/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 *
 * @author chrispowell
 */
public class FTTreeData
{
    private static SortedMap<String,Integer> sm = new TreeMap<>();
    private static Map<Integer,List<LemmaCount>> docCount = new HashMap<>();
    
    public static void main(String[] args)
    {
        sm.put("beetroot",1001);
        sm.put("asparagus",1602);
        sm.put("apple",853);
        sm.put("lemon",852);
        sm.put("kumquat",7);
        sm.put("orange",852);
        sm.put("date",6);
//        Stream<Map.Entry<String,Integer>> sSM = sm.entrySet().stream().sorted(Map.Entry.comparingByValue(new ReverseComparator()));
//        Map<String,Integer> sortedSM = sSM.collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
//        sortedSM.forEach((k,v) ->
//        {
//            System.out.println("" +k+ "=" +v);
//        });
        
        docCount.put(1, Arrays.asList(new LemmaCount[]{new LemmaCount("date",3),new LemmaCount("beetroot",1),new LemmaCount("lemon",7)}));
        docCount.put(2, Arrays.asList(new LemmaCount[]{new LemmaCount("kumquat",29),new LemmaCount("orange",5)}));
        docCount.put(3, Arrays.asList(new LemmaCount[]{new LemmaCount("orange",99),new LemmaCount("apple",90),new LemmaCount("lemon",3),new LemmaCount("asparagus",1),new LemmaCount("beetroot",9),new LemmaCount("date",25)}));
        
        docCount.forEach((k,v) -> {
            System.out.println("> " +k);
            Map<LemmaCount,Integer> lcOrder = new HashMap();
            v.forEach(lc -> {
                lcOrder.put(lc,sm.get(lc.getLemma()));
            });

            Collections.sort(v,new LemmaCountComparator(lcOrder));
            v.forEach(System.out::println);
        });
    }
    
}

//-----------------------------------------------------------------------------
class LemmaCount
{
    private final String  lemma;
    private final int     count;

    public LemmaCount(String lemma, int count)
    {
        this.lemma = lemma;
        this.count = count;
    }

    public String getLemma() { return lemma; }
    public int getCount() { return count; }
    
    @Override
    public String toString()
    {
        return lemma +"="+ count;
    }
}

class LemmaCountComparator implements Comparator<LemmaCount>
{
    private final Map<LemmaCount,Integer> sortOrder;
    
    public LemmaCountComparator(Map<LemmaCount,Integer> sortOrder)
    {
        this.sortOrder = sortOrder;
    }
    
    /**
     * Compare lemmas via sortOrder
     */
    @Override
    public int compare(LemmaCount lc0, LemmaCount lc1)
    {
        Integer lemmaOrder0 = sortOrder.get(lc0);
        if( lemmaOrder0 == null )
            throw new IllegalArgumentException("Bad lemma encountered: " +lc0.getLemma());
        Integer lemmaOrder1 = sortOrder.get(lc1);
        if( lemmaOrder1 == null )
            throw new IllegalArgumentException("Bad lemma encountered: " +lc1.getLemma());

        return lemmaOrder1 - lemmaOrder0;
    }
}