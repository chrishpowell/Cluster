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
public class NoGlobalSuppItemsException extends Exception
{
    /**
     * Creates a new instance of <code>NoGlobalSuppItemsException</code> without
     * detail message.
     */
    public NoGlobalSuppItemsException() {}

    /**
     * Constructs an instance of <code>NoGlobalSuppItemsException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NoGlobalSuppItemsException(String msg) { super(msg); }
}
