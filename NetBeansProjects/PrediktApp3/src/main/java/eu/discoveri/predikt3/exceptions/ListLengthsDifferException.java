/*
 */
package eu.discoveri.predikt3.exceptions;

/**
 * Used when two lists (eg: tokens and POStags which supposedly match one2one)
 * lengths do not match.
 * 
 * @author Chris Powell, Discoveri OU
 */
public class ListLengthsDifferException extends Exception
{
    /**
     * Creates a new instance of <code>ListLengthsDifferException</code> without
     * detail message.
     */
    public ListLengthsDifferException() {}

    /**
     * Constructs an instance of <code>ListLengthsDifferException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ListLengthsDifferException(String msg) { super(msg); }
}
