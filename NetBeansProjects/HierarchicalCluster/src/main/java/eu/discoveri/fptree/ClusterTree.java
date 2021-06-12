/*
 * Licence...
 */
package eu.discoveri.fptree;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Cluster tree.
 * 
 * @author chrispowell
 * @param <T>
 */
public class ClusterTree<T extends Comparable> extends DTree<T>
{
    /**
     * Constructor for generating root node.
     * 
     * @param contents
     * @param rootNode true if root node being constructed.
     */
    protected ClusterTree(T contents, boolean rootNode)
    {
        super(contents,rootNode);
    }
    
    /**
     * Constructor.
     * 
     * @param contents
     */
    public ClusterTree(T contents)
    {
        super(contents);
    }
    
    /**
     * Create a root node.
     * 
     * @param <T>
     * @param root Root of tree (holder)
     * @return 
     */
//    public static <T extends Comparable> ClusterTree<T> initialiseTree(T root)
//    {
//        ClusterTree<T> r = (ClusterTree<T>)DTree.initialiseDTree(root);
//        return r;
//    }
    public static <T extends Comparable> ClusterTree<T> initialise(T root)
    {
        DTree rootNode = new ClusterTree(root,true);
        return (ClusterTree)rootNode;
    }
    
    /**
     * Get children of this node.
     * 
     * @return 
     */
    public List<ClusterTree<T>> getChildren()
    {
        return children.stream().map(c -> (ClusterTree<T>)c).collect(Collectors.toList());
    }
    
    /**
     * Get all leaves of tree.
     * 
     * @param <T> tree contents (eg: Item)
     * @param tree the tree
     * @return 
     */
    public static <T extends Comparable> List<ClusterTree<T>> findAllLeaves( ClusterTree<T> tree )
    {
        return tree.listAllChildren()
                .stream()
                .filter(an -> an.getOffspring().isEmpty())
                .map(an -> (ClusterTree<T>)an)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all leaves which do not have root as parent.
     * 
     * @param <T>
     * @param tree
     * @return 
     */
    public static <T extends Comparable> Map<ClusterTree<T>,ClusterTree<T>> findAllLeavesParentNotRoot( ClusterTree<T> tree )
    {
        DTree<T> root = tree.getRoot();
        return tree.listAllChildren()
                .stream()
                .filter(an -> an.getOffspring().isEmpty() &&
                            !an.getParent().equals(root))
                .collect(Collectors.toMap(an -> (ClusterTree)an,an -> (ClusterTree)an.getParent()));
    }

    /**
     * Get all parents of nodes.
     * 
     * @param <T>
     * @param tree
     * @return 
     */
//    public static <T extends Comparable> List<T> findAllParents( ClusterTree<T> tree )
//    {
//        return tree.listAllParents().stream().map(p -> p.getContents()).collect(Collectors.toList());
//    }
    
    //--------------------------------------------------------------------------
    /**
     * Dump all Cluster subtrees from given node.
     * 
     * @param <T>
     * @param ct 
     * @param leadin 
     */
    public static <T extends Comparable> void dumpAllSubtrees( ClusterTree<T> ct, String leadin )
    {
        // streamChildren() from super class
        ct.streamChildren().forEach(dtree ->
        {
            System.out.print(">");
            printTree((ClusterTree<T>)dtree,leadin,false);
        });
    }
    
    /**
     * Print simple cluster tree.
     * @param <T>
     * @param node
     * @param appender 
     * @param uid 
     */
    public static <T extends Comparable> void printTree(ClusterTree<T> node, String appender, boolean uid )
    {
        System.out.println(appender + ((T)node.getContents()));
        node.getChildren().stream().map(each -> (ClusterTree<T>)each).forEach(each ->  printTree((ClusterTree<T>)each, appender + appender, uid));
    }
}
