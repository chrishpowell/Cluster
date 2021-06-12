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
import java.util.Optional;


/**
 *
 * @author chrispowell
 * @param <T>
 */
public class BottomUpTree<T extends Comparable> extends DTree<T>
{
    private BottomUpTree(T item)
    {
        super(item);
    }
    
    /**
     * Create a root node.
     * 
     * @param <T>
     * @param root item
     * @return 
     */
    public static <T extends Comparable> BottomUpTree<T> initialiseTree(T root)
    {
        BottomUpTree<T> r = new BottomUpTree(root);
        r.setRoot(root);
        return r;
    }
    
    /**
     * Print simple tree.
     * @param <T>
     * @param node
     * @param appender 
     * @param uid 
     */
    public static <T extends Comparable> void printTree(BottomUpTree<T> node, String appender, boolean uid )
    {
        System.out.println(appender + ((BUT)node.getContents()).getNames()+"("+((BUT)node.getContents()).getScore()+")");
        node.getChildren().stream().map(each -> (BottomUpTree<T>)each).forEach(each ->  printTree((BottomUpTree<T>)each, appender + appender, uid));
    }
    
    /**
     * Is tree working?
     */
    public static void testBUT()
    {
        List<String> a = Arrays.asList("a");
        List<String> z = Arrays.asList("z");
        List<String> c = Arrays.asList("c");
        List<String> ab = Arrays.asList("a","b");
        List<String> ac = Arrays.asList("a","c");
        List<String> bd = Arrays.asList("b","d");
        List<String> bc = Arrays.asList("b","c");
        List<String> abc = Arrays.asList("a","b","c");
        List<String> abd = Arrays.asList("a","b","d");
        List<String> abcd = Arrays.asList("b","c","a","d");
        List<String> b = Arrays.asList("b");
        List<String> ace = Arrays.asList("a","c","e");
        
//        List<BUT> lb = Arrays.asList(new BUT(b,1.f), new BUT(c,1.f), new BUT(bc,2.f), new BUT(bd,1.f), new BUT(a,2.f),
//                             new BUT(ab,1.f), new BUT(ac,2.5f), new BUT(abc,1.f), new BUT(abd,1.f), new BUT(z,1.f), new BUT(ace,1.f), new BUT(abcd,1.f));
        
        BottomUpTree<BUT> root = BottomUpTree.initialiseTree(new BUT());
        
        BottomUpTree<BUT> aBut = new BottomUpTree(new BUT(a,2.f));
        root.addChild(aBut);
        BottomUpTree<BUT> acBut = new BottomUpTree(new BUT(ac,2.f));
        aBut.addChild(acBut);
        BottomUpTree<BUT> abcBut = new BottomUpTree(new BUT(abc,2.f));
        acBut.addChild(abcBut);
        BottomUpTree<BUT> aceBut = new BottomUpTree(new BUT(ace,2.f));
        acBut.addChild(aceBut);

        BottomUpTree.printTree(root, " ", false);
    }
    
    //--------------------------------------------------------------------------
    // Map item to containing node to build tree
    private static Map<BUT,BottomUpTree<BUT>> item2Tree = new HashMap<>();
    
    private static void dumpI2T()
    {
        System.out.println("== Item2Tree:");
        item2Tree.forEach((k,v) -> {
            System.out.println(" " +k.getNames()+ ", " +v.getUuid());
        });
    }
    
    /**
     * Build tree.  Not part of BottomUpTree!
     * 
     * @param lb
     * @param c
     * @param root
     */
    public void add2Tree(List<BUT> lb, BottomUpTree<BUT> c, BottomUpTree<BUT> root)
    {
        // What was parent is now child as we move up a branch
        BUT child = c.getContents();
        
        // Input items
        List<String> cNames = child.getNames();
        int childSiz = cNames.size();
        
        /*
         * Find parents
         */
            // Find parent with max score
            Optional<BUT> op = lb.stream()
              .filter(p -> p.getNames().size()==childSiz-1)         // Parent has one less item than child
              .filter(p -> cNames.containsAll(p.getNames()))        // Intersect item sets
              .max(Comparator.comparingDouble(BUT::getScore));      // Get max. parent score
            
            // No parent?  Then root will adopt... :-)
            if( !op.isPresent() )
            {
                // At top level, add to root (parent) if not yet done
                if( !item2Tree.containsKey(child) )
                    item2Tree.put(child, c);
                
                // And has child previously been added to root (via a different branch)?
                if( !root.getChildren().contains(c) )
                    root.addChild(c);
            }
            else
            {
                // Attach parent to child
                BUT pt = op.get();
                if( item2Tree.containsKey(pt) )
                {   // Already processed parent
                    BottomUpTree par = item2Tree.get(pt);
                    
                    // Has child already been added to parent (via a different branch)?
                    if( !par.getChildren().contains(c) )
                        par.addChild(c);
                    
                    // Next level up
                    add2Tree(lb,par,root);
                }
                else
                {   // Ok, create a node for the parent and flag as processed
                    BottomUpTree par = new BottomUpTree(pt);
                    par.addChild(c);
                    item2Tree.put(pt, par);

                    // Next level up
                    add2Tree(lb,par,root);
                }
            }
    }


    /**
     * M A I N
     * =======
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
            throws Exception
    {
        List<String> a = Arrays.asList("a");
        List<String> z = Arrays.asList("z");
        List<String> c = Arrays.asList("c");
        List<String> ab = Arrays.asList("a","b");
        List<String> ac = Arrays.asList("a","c");
        List<String> bd = Arrays.asList("b","d");
        List<String> bc = Arrays.asList("b","c");
        List<String> abc = Arrays.asList("a","b","c");
        List<String> abd = Arrays.asList("a","b","d");
        List<String> abcd = Arrays.asList("b","c","a","d");
        List<String> b = Arrays.asList("b");
        List<String> ace = Arrays.asList("a","c","e");
        List<String> argh = Arrays.asList("x","y");                     // No 1-item parent!

        // Unsorted list
        List<BUT> lb = Arrays.asList(new BUT(b,1.f), new BUT(c,1.f), new BUT(bc,2.f), new BUT(bd,1.f), new BUT(a,2.f),
                                     new BUT(ab,1.f), new BUT(ac,2.5f), new BUT(abc,1.f), new BUT(abd,1.f), new BUT(argh,1.0f), new BUT(z,1.f), new BUT(ace,1.f), new BUT(abcd,1.f));

        // Sort by name list size descending (ie leaves to root)
        Collections.sort(lb);
        
        BottomUpTree<BUT> root = BottomUpTree.initialiseTree(new BUT());
        lb.stream()
            .filter(l -> !item2Tree.containsKey(l))                     // Not yet processed?
            .forEach( l ->
                { root.add2Tree(lb,new BottomUpTree<>(l),root); });     // Create branch of tree
        
        BottomUpTree.printTree(root, " ", false);
        
        //------------------------------
//        System.out.println("\r\nTest tree");
//        testBUT();
    }
}

//------------------------------------------------------------------------------
class BUT implements Comparable<BUT>
{
    private final List<String>  names;
    private final double        score;

    public BUT(List<String> names)
    {
        this(names,0.0f);
    }
    
    public BUT(List<String> names, double score)
    {
        this.names = names;
        this.score = score;
    }
    
    public BUT()
    {
        this(Arrays.asList("root")); 
    }

    public List<String> getNames() { return names; }
    public double getScore() { return score; }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        names.forEach(n -> {
            sb.append(n).append(":");
        });
        
        return sb.toString();
    }

    /**
     * Size descending
     * @param b
     * @return 
     */
    @Override
    public int compareTo(BUT b)
    {
        return b.getNames().size() - this.names.size();
    }
}
