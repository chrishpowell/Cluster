/*
 */
package eu.discoveri.predikt3.exceptions;

/**
 * No text in Sentence.
 * 
 * @author Chris Powell, Discoveri OU
 */
public class SentenceIsEmptyException extends Exception
{
    /**
     * Creates a new instance of <code>SentenceIsEmptyException</code> without
     * detail message.
     */
    public SentenceIsEmptyException() {}

    /**
     * Constructs an instance of <code>SentenceIsEmptyException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SentenceIsEmptyException(String msg) { super(msg); }
}
