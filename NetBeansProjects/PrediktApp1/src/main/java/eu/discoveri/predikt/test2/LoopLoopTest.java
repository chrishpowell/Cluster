/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

/**
 *
 * @author chrispowell
 */
public class LoopLoopTest
{
    public static void main(String[] args)
    {
        int nDocs = 5;
        for( int ii=0; ii<nDocs; ii++ )
            for( int jj=ii+1; jj<nDocs; jj++ )
                System.out.println("ii: " +ii+ ", jj: " +jj);
    }
}
