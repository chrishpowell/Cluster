/*
 * Licence.
 */
package eu.discoveri.fptree;

/**
 *
 * @author chrispowell
 */
public class EmptySetofClusterException extends Exception
{
    /**
     * Creates a new instance of <code>EmptySetofClusterException</code> without
     * detail message.
     */
    public EmptySetofClusterException() {}

    /**
     * Constructs an instance of <code>EmptySetofClusterException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public EmptySetofClusterException(String msg) { super(msg); }
}
