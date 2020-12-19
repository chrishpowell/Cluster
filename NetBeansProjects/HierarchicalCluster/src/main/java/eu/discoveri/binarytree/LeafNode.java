/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;


/**
 *
 * @author chrispowell
 */
public class LeafNode extends Node implements Comparable<LeafNode>
{
    private final Pair  nodeVal;
    
    public LeafNode( Pair nodeVal )
    {
        super(null,null,0.d);
        this.nodeVal = nodeVal;
    }
    
//    /**
//     * 'Normal' insert
//     * @param node
//     * @param p 
//     */
//    public void insert(Node node, Pair p)
//    {
//        // (vals) p < current
//        if( p.compareTo(node.getNodeVal()) <= 0 )
//        {
//            if( node.getLeft() != null )
//            {
//                insert(node.getLeft(), p);
//            }
//            else
//            {
//                System.out.println(" Inserted " + p + " to left of " + node.getNodeVal());
//                node.setLeft(new Node(p));
//            }
//        }
//        else
//            // (vals) p >= current
//            if( p.compareTo(node.getNodeVal()) == 1 )
//            {
//                if( node.getRight() != null )
//                {
//                    insert(node.getRight(), p);
//                }
//                else
//                {
//                    System.out.println("  Inserted " + p + " to right of " + node.getNodeVal());
//                    node.setRight(new Node(p));
//                }
//            }
//    }

    public Pair getNodeVal() { return nodeVal; }

    @Override
    public int compareTo(LeafNode o)
    {
        return nodeVal.compareTo(o.getNodeVal());
    }
}
