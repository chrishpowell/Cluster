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
public class SentenceNotPersistedException extends Exception
{
    /**
     * Creates a new instance of <code>SentenceNotPersistedException</code>
     * without detail message.
     */
    public SentenceNotPersistedException() {}

    /**
     * Constructs an instance of <code>SentenceNotPersistedException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public SentenceNotPersistedException(String msg) { super(msg); }
}
