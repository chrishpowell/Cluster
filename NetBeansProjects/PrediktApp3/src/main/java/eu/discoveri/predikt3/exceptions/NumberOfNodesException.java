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
public class NumberOfNodesException extends Exception
{
    /**
     * Creates a new instance of <code>NumberOfNodesException</code> without
     * detail message.
     */
    public NumberOfNodesException() {}

    /**
     * Constructs an instance of <code>NumberOfNodesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NumberOfNodesException(String msg) { super(msg); }
}
