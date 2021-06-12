/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author chrispowell
 */
public class STTest
{
    public static void main(String[] args)
    {
        ClusterTree<Cluster> fc = ClusterTree.initialiseTree(new Cluster("root",new ArrayList<>()));
        
        Cluster c1 = new Cluster("c1",Collections.EMPTY_LIST);
        Cluster c2 = new Cluster("c2",Collections.EMPTY_LIST);
        Cluster c11 = new Cluster("c11",Collections.EMPTY_LIST);
        Cluster c12 = new Cluster("c12",Collections.EMPTY_LIST);
        Cluster c122 = new Cluster("c122",Collections.EMPTY_LIST);
        Cluster c121 = new Cluster("c121",Collections.EMPTY_LIST);
        Cluster c1221 = new Cluster("c1221",Collections.EMPTY_LIST);
        Cluster c21 = new Cluster("c21",Collections.EMPTY_LIST);
        
        ClusterTree<Cluster> ct1 = (ClusterTree)fc.addChild(new ClusterTree(c1));
        ClusterTree<Cluster> ct2 = (ClusterTree)fc.addChild(new ClusterTree(c2));
        ClusterTree<Cluster> ct11 = (ClusterTree)ct1.addChild(new ClusterTree(c11));
        ClusterTree<Cluster> ct12 = (ClusterTree)ct1.addChild(new ClusterTree(c12));
        ClusterTree<Cluster> ct122 = (ClusterTree)ct12.addChild(new ClusterTree(c122));
        ClusterTree<Cluster> ct121 = (ClusterTree)ct12.addChild(new ClusterTree(c121));
        ClusterTree<Cluster> ct1221 = (ClusterTree)ct122.addChild(new ClusterTree(c1221));
        ClusterTree<Cluster> ct21 = (ClusterTree)ct2.addChild(new ClusterTree(c21));
        
        ClusterTree.printTree(fc, " ", false);
        System.out.println("-------------------------------------------");
        ClusterTree.dumpAllSubtrees(fc, " ");
    }
}
