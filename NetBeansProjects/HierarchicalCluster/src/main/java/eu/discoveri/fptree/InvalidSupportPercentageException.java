/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

/**
 * Support level (percentage) exception.
 * 
 * @author chrispowell
 */
public class InvalidSupportPercentageException extends Exception
{
    /**
     * Creates a new instance of <code>InvalidSupportPercentageException</code>
     * without detail message.
     */
    public InvalidSupportPercentageException() {}

    /**
     * Constructs an instance of <code>InvalidSupportPercentageException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidSupportPercentageException(String msg) { super(msg); }
}
