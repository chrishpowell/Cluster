/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.exceptions;


/**
 *
 * @author Chris Powell, Discoveri OU
 */
public class MissingMatchedWordException extends Exception
{
    /**
     * Creates a new instance of <code>MissingMatchedWordException</code>
     * without detail message.
     */
    public MissingMatchedWordException() {}

    /**
     * Constructs an instance of <code>MissingMatchedWordException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public MissingMatchedWordException(String msg) { super(msg); }
}
