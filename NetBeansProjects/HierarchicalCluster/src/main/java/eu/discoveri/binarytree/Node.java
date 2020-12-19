/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import java.io.PrintStream;
import java.util.UUID;

/**
 *
 * @author chrispowell
 */
public class Node
{
    private final UUID  uuid;
    private Node        left, right;
    private double      height;                 // Normalised level of this node

    /**
     * Constructor.
     * @param left
     * @param right
     * @param height 
     */
    public Node(Node left, Node right, double height)
    {
        this.uuid = UUID.randomUUID();
        this.left = left;
        this.right = right;
        this.height = height;
    }

    /**
     * Traverse the dendrogram.
     * @param root
     * @return 
     */
    private String traversePreOrder(Node root)
    {
        if( root == null ) { return ""; }

        StringBuilder sb = new StringBuilder();
        sb.append(root.getUUID().toString());

        String pointerRight = "└──";
        String pointerLeft = (root.getRight() != null) ? "├──" : "└──";

        traverseNodes(sb, "", pointerLeft, root.getLeft(), root.getRight() != null);
        traverseNodes(sb, "", pointerRight, root.getRight(), false);

        return sb.toString();
    }

    private void traverseNodes(StringBuilder sb, String padding, String pointer, Node node, boolean hasRightSibling)
    {
        if (node != null) {

            sb.append("\n");
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getUUID().toString());

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

    public void dumpTree(LeafNode root, PrintStream os)
    {
        os.print(traversePreOrder(root));
    }

    public UUID getUUID() { return uuid; }
    public Node getLeft() { return left; }
    public void setLeft(Node left) { this.left = left; }
    public Node getRight() { return right; }
    public void setRight(Node right) { this.right = right; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
