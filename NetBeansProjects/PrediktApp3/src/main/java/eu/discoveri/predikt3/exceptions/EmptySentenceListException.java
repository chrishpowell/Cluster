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
public class EmptySentenceListException extends Exception
{
    /**
     * Creates a new instance of <code>EmptySentenceListException</code> without
     * detail message.
     */
    public EmptySentenceListException() {}

    /**
     * Constructs an instance of <code>EmptySentenceListException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public EmptySentenceListException(String msg) { super(msg); }
}
