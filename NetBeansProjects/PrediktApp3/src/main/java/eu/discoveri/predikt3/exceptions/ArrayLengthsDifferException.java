/*
 */
package eu.discoveri.predikt3.exceptions;

/**
 * Used when two arrays (eg: tokens and POStags which supposedly match one2one)
 * lengths do not match.
 * 
 * @author Chris Powell, Discoveri OU
 */
public class ArrayLengthsDifferException extends Exception
{
    /**
     * Creates a new instance of <code>ArrayLengthsDifferException</code>
     * without detail message.
     */
    public ArrayLengthsDifferException() {}

    /**
     * Constructs an instance of <code>ArrayLengthsDifferException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ArrayLengthsDifferException(String msg) { super(msg); }
}
