/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author chrispowell
 */
public class TreeFlatten
{
    private String  name;
    private int     value;
    private List<TreeFlatten> children = new ArrayList<>();
 
    public TreeFlatten(String name, int value, List<TreeFlatten> children)
    {
        this.name = name;
        this.value = value;
        this.children.addAll(children);
    }
 
    public int getValue() { return value; }
    public String getName() { return name; }
 
    public List<TreeFlatten> getChildren()
    {
        return Collections.unmodifiableList(children);
    }
 
    public Stream<TreeFlatten> flattened()
    {
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(TreeFlatten::flattened));
    }
    
    public Stream<List<TreeFlatten>> subsets(List<TreeFlatten> values)
    {
        SubSetListIterator ssi = new SubSetListIterator(values);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ssi, 0), false);
    }
    
    @Override
    public String toString()
    {
        return name+":"+value;
    }
    
    /**
     * M A I N
     * =======
     * @param args 
     */
    public static void main(String[] args)
    {
        TreeFlatten layer2 = new TreeFlatten("layer2",0,new ArrayList<>());
        TreeFlatten result3 = new TreeFlatten("result3",0,List.of(layer2));
        TreeFlatten layer1 = new TreeFlatten("layer1",0,new ArrayList<>());
        TreeFlatten flow1 = new TreeFlatten("flow1",0,List.of(layer1,result3));
        
        System.out.println("Flattened tree:");
        List<TreeFlatten> ltf = flow1.flattened().map(s -> (TreeFlatten)s).collect(Collectors.toList());
        ltf.forEach(System.out::println);
        
        System.out.println("\r\nSubsets:");
        Supplier<Stream<List<TreeFlatten>>> lfti = () -> flow1.subsets(ltf);
        lfti.get().forEach(l -> l.forEach(f -> {System.out.println("###> " +f.getName());}));
    }
}
