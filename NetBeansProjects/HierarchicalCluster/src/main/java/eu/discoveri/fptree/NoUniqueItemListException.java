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
public class NoUniqueItemListException extends Exception
{
    /**
     * Creates a new instance of <code>NoUniqueItemListException</code> without
     * detail message.
     */
    public NoUniqueItemListException() {}

    /**
     * Constructs an instance of <code>NoUniqueItemListException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoUniqueItemListException(String msg) { super(msg); }
}
