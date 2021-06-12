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
public class SubsetsException extends Exception
{
    /**
     * Creates a new instance of <code>SubsetsException</code> without detail
     * message.
     */
    public SubsetsException() {}

    /**
     * Constructs an instance of <code>SubsetsException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SubsetsException(String msg) { super(msg); }
}
