/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.exceptions;

/**
 * Expect count of tokens across sentence pairs.
 * 
 * @author Chris Powell, Discoveri OU
 */
public class TokensCountInSentencesIsZeroException extends Exception
{
    /**
     * Creates a new instance of <code>TokensInSentencesCountIsEmpty</code>
     * without detail message.
     */
    public TokensCountInSentencesIsZeroException(){}

    /**
     * Constructs an instance of <code>TokensInSentencesCountIsEmpty</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public TokensCountInSentencesIsZeroException(String msg) { super(msg); }
}
