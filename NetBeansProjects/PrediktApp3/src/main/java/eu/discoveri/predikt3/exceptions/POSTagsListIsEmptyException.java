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
public class POSTagsListIsEmptyException extends Exception
{
    /**
     * Creates a new instance of <code>POSTagsListIsEmptyException</code>
     * without detail message.
     */
    public POSTagsListIsEmptyException() {}

    /**
     * Constructs an instance of <code>POSTagsListIsEmptyException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public POSTagsListIsEmptyException(String msg) { super(msg); }
}
