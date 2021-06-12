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
public class InvalidItemException extends Exception
{
    /**
     * Creates a new instance of <code>InvalidItemException</code> without
     * detail message.
     */
    public InvalidItemException() {}

    /**
     * Constructs an instance of <code>InvalidItemException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidItemException(String msg) { super(msg); }
}
