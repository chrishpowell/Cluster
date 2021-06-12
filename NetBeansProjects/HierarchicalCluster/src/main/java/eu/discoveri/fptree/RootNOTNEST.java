/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

/**
 *
 * @author chrispowell
 * @param <T>
 */
public class RootNOTNEST<T extends Comparable> extends DTree<T>
{
    public RootNOTNEST(T contents)
    {
        super(contents);
    }
}
