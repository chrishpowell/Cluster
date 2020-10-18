/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.exceptions;

/**
 *
 * @author chrispowell
 */
public class NotEnoughSentencesException extends Exception
{

    /**
     * Creates a new instance of <code>NotEnoughSentencesException</code>
     * without detail message.
     */
    public NotEnoughSentencesException() {}

    /**
     * Constructs an instance of <code>NotEnoughSentencesException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotEnoughSentencesException(String msg) { super(msg); }
}
