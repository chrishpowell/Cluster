/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

/**
 *
 * @author chrispowell
 */
public class CT
{
    public static void main(String[] args) {
        Cluster c = new Cluster("x",null);
        ClusterTree<Cluster> ct = ClusterTree.initialiseTree(c);
        
        FTTree<Item> ft = FTTree.initialiseTree(new Item("",0));
    }
}
