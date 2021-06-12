/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.Comparator;

/**
 *
 * @author chrispowell
 */
public class ClusterNameComparator implements Comparator<Cluster>
{
    @Override
    public int compare(Cluster c1, Cluster c2)
    {
        return c1.getName().compareTo(c2.getName());
    }
}
