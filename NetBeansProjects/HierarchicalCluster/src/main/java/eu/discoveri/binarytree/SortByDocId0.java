/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.binarytree;

import java.util.Comparator;

/**
 *
 * @author chrispowell
 */
public class SortByDocId0 implements Comparator<Pair>
{
    @Override
    public int compare( Pair p0, Pair p1 )
    {
        return p0.getDoc0() - p1.getDoc0();
    }
}
