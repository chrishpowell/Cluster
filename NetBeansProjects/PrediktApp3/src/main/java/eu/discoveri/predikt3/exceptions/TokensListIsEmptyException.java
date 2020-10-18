/*
 */
package eu.discoveri.predikt3.exceptions;

/**
 * Tokens for this sentence have not been generated.
 * @author Chris Powell, Discoveri OU
 */
public class TokensListIsEmptyException extends Exception
{
    /**
     * Creates a new instance of <code>TokensListIsEmpty</code> without detail
     * message.
     */
    public TokensListIsEmptyException() {}

    /**
     * Constructs an instance of <code>TokensListIsEmpty</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TokensListIsEmptyException(String msg) { super(msg); }
}
