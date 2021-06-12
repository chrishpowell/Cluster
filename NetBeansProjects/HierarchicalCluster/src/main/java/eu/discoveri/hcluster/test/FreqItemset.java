/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.hcluster.test;

import eu.discoveri.fptree.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 * @author chrispowell
 */
public class FreqItemset
{
    private static Map<String,Integer>  fuzzyVal = new HashMap<>();
    private static double fMin = Double.MAX_VALUE, fMax = Double.MIN_VALUE;

    
    public static void main(String[] args)
    {
        Map<String,Map<String,Integer>> featVector = new HashMap<>();
        
        featVector.put("Doc1",Map.of("form",1));
        featVector.put("Doc2",Map.of("flow",1,"form",1,"layer",1,"patient",3));
        featVector.put("Doc3",Map.of("flow",2,"layer",1));
        featVector.put("Doc4",Map.of("flow",2,"layer",2,"result",3));
        featVector.put("Doc5",Map.of("flow",2,"layer",3,"patient",7));
        featVector.put("Doc6",Map.of("flow",1,"layer",2));
        featVector.put("Doc7",Map.of("patient",8,"result",1,"treatment",2));
        featVector.put("Doc8",Map.of("patient",4,"result",3,"treatment",1));
        featVector.put("Doc9",Map.of("patient",3,"treatment",2));
        featVector.put("Doca",Map.of("patient",6,"result",3,"treatment",3));
        featVector.put("Docb",Map.of("flow",1,"form",1,"patient",4));
        featVector.put("Docc",Map.of("patient",9,"result",1,"treatment",1));
        
//        featVector.forEach((k,v) -> {
//            v.forEach((k0,v0) -> {
//                v.forEach((k1,v1) -> {
//                    System.out.println(" ");
//                });
//            });
//        });
        
        
//        featVector.forEach((k0,v0) -> {
//            v0.forEach((k1,v1) -> {
//                if( !fuzzyVal.containsKey(k1) )
//                    fuzzyVal.put(k1,v1);
//                else
//                    fuzzyVal.put(k1, v1+fuzzyVal.get(k1));
//            });
//        });
//        
//        System.out.println("Fuzzy val:");
//        fuzzyVal.forEach((k,v) -> {
//            System.out.println("Key: " +k+ ", Val: " +v);
//        });
//        
        // Set up singleton clusters
        Map<String,Integer> mapSI = new HashMap<>();
        mapSI.put("flow",0);
        mapSI.put("form",1);
        mapSI.put("layer",2);
        mapSI.put("patient",3);
        mapSI.put("result",4);
        mapSI.put("treatment",5);
        
        Map<Integer,Cluster> mapC = new HashMap<>();
        mapC.put(0,new Cluster("flow",0));
        mapC.put(1,new Cluster("form",1));
        mapC.put(2,new Cluster("layer",2));
        mapC.put(3,new Cluster("patient",3));
        mapC.put(4,new Cluster("result",4));
        mapC.put(5,new Cluster("treatment",5));

        // Populate
        double numDocs = featVector.size();
        // Determine cluster scores
        featVector.forEach((k,v) -> {
            v.forEach((k1,v1) -> {
                // Incr count of docs - if present
                Cluster c = mapC.get(mapSI.get(k1));
                c.setDocCount(c.getDocCount()+1);
            });
        });
        System.out.println("");
        
        // Add item list on single keys
        mapC.forEach((k,c) ->
        {
            c.setDocCount(c.getDocCount()/numDocs);
            c.getItems().add(new Item(c.getName(),0));
        });

        // Determine "duals"
        int mcIdx = mapC.size() - 1;
        for( int ii=0; ii<mcIdx; ii++ )
            for( int jj=ii+1; jj<mcIdx; jj++ )
            {
                String cName = mapC.get(ii).getName() +","+ mapC.get(jj).getName();
                mapC.put(jj+mcIdx, new Cluster(cName,jj+mcIdx));
                mapSI.put(cName, jj+mcIdx);
            }
        
        // Determine "triples"
        
        
        // Ci (mapSI)
        System.out.println("C-i:");
        mapSI.forEach((k0,v0) ->
        {
            // Eg: "flow", "flow,form"
            System.out.println("C-i: " +k0);
            String[] itemKeys = k0.split(",");
            System.out.print("  item keys: ");
            Set<String> cKS = Arrays.stream(itemKeys).collect(Collectors.toSet());
            System.out.println(" .> cKS: " +cKS);
                
            for( String key: featVector.keySet() )
            {
                // Items: item/word, idx
                Map<String,Integer> items = featVector.get(key);
                Set<String> itemsKS = items.keySet(); //System.out.println("  ...> itemsKS: " +itemsKS);
                Set<String> iksCopy = new HashSet<>(itemsKS);
                iksCopy.retainAll(cKS); //System.out.println(" ...> size: " +cKS+ ":" +cKS.size());
                
                if( iksCopy.size() > 0 ) //items.containsKey(k0) )
                {
                    System.out.print("" +key);
                    items.forEach((k,v) -> {
                        System.out.print(" " +k+ "(" +v+ ")");
                    });
                    System.out.println("");
                }
            }
            System.out.println("\r\n-----------------------------------------");
        });
//        featVector.forEach((k,v) -> {
//            System.out.print("" +k);
//            v.entrySet().stream()
//                .filter(key -> key.getKey().equals("flow")).
//                .forEach(k1 -> {
//                    // Rows
//                    System.out.print(" " +k1.getKey()+ "(" +k1.getValue()+ ")");
//                });
//            System.out.println("");
//        });
//        System.out.println("");
//        
//        // Cull and print
//        mapC.entrySet().removeIf(m -> m.getValue().getDocCount() <= 0.25);
//        mapC.forEach((k,c) -> {
//            System.out.println("Key: " +k+ ", count%: " +c.getDocCount());
//        });
        

//        System.out.println("Min/max:");
//        featVector.forEach((k0,v0) ->
//        {
//            Collection<Integer> vals = v0.values();
//            double fMn = Collections.min(vals);
//            if( fMn < fMin )
//                fMin = fMn;
//            double fMx = Collections.max(vals);
//            if( fMx > fMax )
//                fMax = fMx;
//        });
//
//        double fAvg = (fMin + fMax) / 2.d;
//        System.out.println("Min.val: " +fMin);
//        System.out.println("Max.val: " +fMax);
//        System.out.println("Avg.val: " +fAvg);
//        
//        // Fuzzy values w[ij](r)
//        double r0 = fMin/2.d;
//        double r1 = (fAvg - fMin)/2.d;
//        double r2 = (fMax - fAvg)/2.d;
//        
//        featVector.forEach((k0,v0) -> {
//            System.out.println("Doc: " +k0);
//            v0.forEach((k1,v1) -> {
//                double  x0 = Double.MIN_VALUE,
//                        x1 = Double.MIN_VALUE,
//                        x2 = Double.MIN_VALUE;
//                
//                if( v1 <= fAvg )
//                {
//                    x0 = v1/fMin;
//                    if( x0 >= 0.5 ) x0 = 1 - x0;
//                }
//
//            });
//        });
    }
}


//==============================================================================
class Cluster
{
    private String          name;
    private int             cNum = Integer.MIN_VALUE;
    private List<Item>      items;
    // Count of docs in which this cluster/item appears
    private double          docCount = 0.0;
    // Children of this node
    private List<Cluster>   children = new ArrayList<>();
    // Parent of this node
    private Cluster         parent = null;
    
    /**
     * Constructor.
     * @param name
     * @param Item 
     */
    public Cluster(String name, int cNum, List<Item> items)
    {
        this.name = name;
        this.items = items;
        this.cNum = cNum;
    }
    
    /**
     * Constructor.  No items.
     * @param name 
     */
    public Cluster(String name, int cNum)
    {
        this(name,cNum,new ArrayList<>());
    }

    public String getName() { return name; }
    public List<Item> getItems() { return items; }

    public double getDocCount() { return docCount; }
    public void setDocCount(double docCount) { this.docCount = docCount; }

    public List<Cluster> getChildren() { return children; }
    public void setChildren(List<Cluster> children) { this.children = children; }

    public Cluster getParent() { return parent; }
    public void setParent(Cluster parent) { this.parent = parent; }
}