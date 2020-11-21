/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.test;

/**
 *
 * @author chrispowell
 */
public class PrediktException extends Exception
{
    /**
     * Creates a new instance of <code>PrediktException</code> without detail
     * message.
     */
    public PrediktException() {}

    /**
     * Constructs an instance of <code>PrediktException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PrediktException(String msg) { super(msg); }
}
