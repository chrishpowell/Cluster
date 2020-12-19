/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import java.io.PrintStream;

/**
 *
 * @author chrispowell
 */
public class BinaryTree
{
    private LeafNode    root;
    
    public void add( Pair p )
    {
        root = addRecursive( root, p );
    }
    
    private LeafNode addRecursive( LeafNode current, Pair p )
    {
        if( current == null )
            return new LeafNode(p);
        
        // (vals) p < current
        if( p.compareTo(current.getNodeVal()) <= 0 )
            current.setLeft( addRecursive(current.getLeft(),p) );
 
        // (vals) p >= current
        if( p.compareTo(current.getNodeVal()) == 1 )
            current.setRight( addRecursive(current.getRight(),p) );
        
        return current;
    }
    
    public void insert(Pair p)
    {
        root = insert(root, p);
    }
    
    private LeafNode insert(LeafNode node, Pair p)
    {
        if( node == null )
        {
            return new LeafNode(p);
        }
        else
            if( p.compareTo(node.getNodeVal()) <= 0 )
            {
                node.setLeft(insert(node.getLeft(), p));
            }
            else
                if( p.compareTo(node.getNodeVal()) == 1 )
                {
                    node.setRight(insert(node.getRight(), p));
                }
                else
                {
                    throw new RuntimeException("duplicate Key!");
                }

        return rebalance(node);
    }
    
    private LeafNode rebalance(LeafNode z)
    {
        updateHeight(z);
        double balance = getBalance(z);
        if (balance > 1) {
            if (height(z.getRight().getRight()) > height(z.getRight().getLeft())) {
                z = rotateLeft(z);
            } else {
                z.setRight(rotateRight(z.getRight()));
                z = rotateLeft(z);
            }
        } else if (balance < -1) {
            if (height(z.getLeft().getLeft()) > height(z.getLeft().getRight())) {
                z = rotateRight(z);
            } else {
                z.setLeft(rotateLeft(z.getLeft()));
                z = rotateRight(z);
            }
        }
        return z;
    }

    private LeafNode rotateRight(LeafNode y)
    {
        LeafNode x = y.getLeft();
        LeafNode z = x.getRight();
        x.setRight(y);
        y.setLeft(z);
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private LeafNode rotateLeft(LeafNode y)
    {
        LeafNode x = y.getRight();
        LeafNode z = x.getLeft();
        x.setLeft(y);
        y.setRight(z);
        updateHeight(y);
        updateHeight(x);
        return x;
    }
    
    private void updateHeight(LeafNode n)
    {
        n.setHeight(1 + Math.max(height(n.getLeft()), height(n.getRight())));
    }

    private double height(LeafNode n)
    {
        return n == null ? -1 : n.getHeight();
    }
    
    public double getBalance(LeafNode n)
    {
        return (n == null) ? 0 : height(n.getRight()) - height(n.getLeft());
    }
    
    public boolean isEmpty() { return root == null; }
    
    public int getSize() { return getSizeRecursive(root); }

    private int getSizeRecursive(LeafNode current)
    {
        return current == null ? 0 : getSizeRecursive(current.getLeft()) + 1 + getSizeRecursive(current.getRight());
    }

    public boolean containsNode(Pair p) { return containsNodeRecursive(root, p); }
    
    private boolean containsNodeRecursive(LeafNode current, Pair p)
    {
        if( current == null ) { return false; }
        if( p.equals(current.getNodeVal()) ) { return true; }

        return p.compareTo(current.getNodeVal()) == -1
          ? containsNodeRecursive(current.getLeft(), p)
          : containsNodeRecursive(current.getRight(), p);
    }
    
    private String traversePreOrder(LeafNode root)
    {
        if( root == null ) { return ""; }

        StringBuilder sb = new StringBuilder();
        sb.append(root.getNodeVal().getDoc0()).append(":").append(root.getNodeVal().getDoc1());

        String pointerRight = "└──";
        String pointerLeft = (root.getRight() != null) ? "├──" : "└──";

        traverseNodes(sb, "", pointerLeft, root.getLeft(), root.getRight() != null);
        traverseNodes(sb, "", pointerRight, root.getRight(), false);

        return sb.toString();
    }

    private void traverseNodes(StringBuilder sb, String padding, String pointer, LeafNode node, boolean hasRightSibling)
    {
        if (node != null) {

            sb.append("\n");
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getNodeVal().getDoc0()).append(":").append(node.getNodeVal().getDoc1());

            StringBuilder paddingBuilder = new StringBuilder(padding);
            if (hasRightSibling) {
                paddingBuilder.append("│  ");
            } else {
                paddingBuilder.append("   ");
            }

            String paddingForBoth = paddingBuilder.toString();
            String pointerRight = "└──";
            String pointerLeft = (node.getRight() != null) ? "├──" : "└──";

            traverseNodes(sb, paddingForBoth, pointerLeft, node.getLeft(), node.getRight() != null);
            traverseNodes(sb, paddingForBoth, pointerRight, node.getRight(), false);
        }
    }

    public void dumpTree(PrintStream os)
    {
        os.print(traversePreOrder(root));
    }
}
