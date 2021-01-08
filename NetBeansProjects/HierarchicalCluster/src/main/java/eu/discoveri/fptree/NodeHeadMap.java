/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chrispowell
 */
public class NodeHeadMap<T>
{
    private static final NodeHeadMap INST = new NodeHeadMap();
    private Map<T,FTTree<T>> nodeHead = new HashMap<>();
    
    public static synchronized NodeHeadMap getInstance()
    {
        return INST;
    }
    
    public Map<T,FTTree<T>> getMap()
    {
        return nodeHead;
    }
}
