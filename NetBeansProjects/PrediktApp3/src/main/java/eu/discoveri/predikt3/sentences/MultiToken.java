/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.sentences;

/**
 *
 * @author chrispowell
 */
public class MultiToken
{
    String  s;
    int     tokCount;

    public MultiToken(String s, int tokCount)
    {
        this.s = s;
        this.tokCount = tokCount;
    }

    public String getS() { return s; }
    public int getTokCount() { return tokCount; }
}
