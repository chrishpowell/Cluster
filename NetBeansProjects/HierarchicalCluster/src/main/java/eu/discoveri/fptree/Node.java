/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Chris Powell
 *
 */
public class Node<T>
{
    private T data = null;
    private List<Node<T>> children = new ArrayList<>();
    private Node<T> parent = null;

    public Node(T data)
    {
        this.data = data;
    }

    public Node<T> addChild(Node<T> child)
    {
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    public void addChildren(List<Node<T>> children)
    {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    public List<Node<T>> getChildren() { return children; }

    public T getData() { return data; }

    public void setData(T data) { this.data = data; }

    private void setParent(Node<T> parent) { this.parent = parent; }

    public Node<T> getParent() { return parent; }
    
    /**
     * 
     * @param <T>
     * @param node
     * @param appender 
     */
    private static <T> void printTree(Node<T> node, String appender)
    {
        System.out.println(appender + node.getData());
        node.getChildren().forEach(each ->  printTree(each, appender + appender));
    }
}
