/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.louvaincluster;

import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.LouvainAlgorithm;
import cwts.networkanalysis.Network;

import eu.discoveri.config.Constants;

import java.util.Random;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Clusters
{
    public static Clustering generate( int numNodes, int[][]edges, double[] edgeWeights )
    {
        Network n = new Network( numNodes, false, edges, edgeWeights, false, false );
        System.err.println("Network consists of " + n.getNNodes() + " nodes and " + n.getNEdges() + " edges.");

        LouvainAlgorithm algor = new LouvainAlgorithm(Constants.LARESOLUTION, Constants.LARANSTARTS, new Random());
        Clustering ic = new Clustering(n.getNNodes());
        
        Clustering fc = null;
        double maxQuality = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < Constants.LARANSTARTS; i++)
        {
            Clustering c = ic.clone();
            algor.improveClustering(n, c);
            double quality = algor.calcQuality(n, c);
            if( Constants.LARANSTARTS > 1 )
                System.err.println("Quality function in random start " + (i + 1) + " equals " + quality + ".");
            if( quality > maxQuality )
            {
                fc = c;
                maxQuality = quality;
            }
        }
        
        return fc;
    }
}
