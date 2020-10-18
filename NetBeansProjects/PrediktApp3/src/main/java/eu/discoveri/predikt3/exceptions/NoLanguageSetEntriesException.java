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
public class NoLanguageSetEntriesException extends Exception
{
    /**
     * Creates a new instance of <code>NoLanguageSetEntriesException</code>
     * without detail message.
     */
    public NoLanguageSetEntriesException() {}

    /**
     * Constructs an instance of <code>NoLanguageSetEntriesException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NoLanguageSetEntriesException(String msg) { super(msg); }
}
