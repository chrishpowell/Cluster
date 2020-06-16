/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.louvaincluster;

import cwts.networkanalysis.CPMClusteringAlgorithm;
import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import cwts.networkanalysis.LeidenAlgorithm;
import cwts.networkanalysis.LouvainAlgorithm;
import cwts.networkanalysis.Network;
import cwts.networkanalysis.run.RunNetworkClustering;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LCTest
{
    public static void main(String[] args)
            throws IOException
    {
        int[][] edges = new int[][]{{0, 1, 2, 2, 3, 5, 4},{1, 2, 0, 3, 5, 4, 3}};
//        int[][] edges = new int[2][7];
//        edges[0][0] = 0;
//        edges[1][0] = 1;
//        edges[0][1] = 1;
//        edges[1][1] = 2;
//        edges[0][2] = 2;
//        edges[1][2] = 0;
//        edges[0][3] = 2;
//        edges[1][3] = 3;
//        edges[0][4] = 3;
//        edges[1][4] = 5;
//        edges[0][5] = 5;
//        edges[1][5] = 4;
//        edges[0][6] = 4;
//        edges[1][6] = 3;
//
        double[] edgeWeights = new double[]{1.d,0.d,1.d,10.d,1.d,0.1d,3.d};
        
        
        Network n = new Network( 6, false, edges, edgeWeights, false, false );
        System.err.println("Network consists of " + n.getNNodes() + " nodes and " + n.getNEdges() + " edges.");

        int nRandomStarts = 1;
        double resolution = 0.2d; int numIters = 10; double randomness = 0.01;
        System.out.println("Quality function: CPM");
        System.out.println("Resolution parameter: " +resolution);
        System.out.println("Num. random starts: " +nRandomStarts);
        System.out.println("Num. iterations: " +numIters);
        System.out.println("Randomness param: " +randomness);
        System.out.println("Running Leiden algorithm");
        LouvainAlgorithm algor = new LouvainAlgorithm(0.2, 10, new Random());
//        IterativeCPMClusteringAlgorithm algor = new LeidenAlgorithm(resolution, numIters, randomness, new Random());
        
        System.err.println("Using singleton initial clustering.");
        Clustering ic = new Clustering(n.getNNodes());
        
        Clustering fc = null;
        double maxQuality = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nRandomStarts; i++)
        {
            Clustering c = ic.clone();
            algor.improveClustering(n, c);
            double quality = algor.calcQuality(n, c);
            if (nRandomStarts > 1)
                System.err.println("Quality function in random start " + (i + 1) + " equals " + quality + ".");
            if (quality > maxQuality)
            {
                fc = c;
                maxQuality = quality;
            }
        }
        
        System.out.println("Max. quality: " +maxQuality);
        RunNetworkClustering.writeClustering("/home/chrispowell/NetBeansProjects/LouvainCluster/src/main/java/eu/discoveri/resources/cluster.txt", fc);
        
        System.out.println("Num. clusters: " +fc.getNClusters());
        int[][] npc = fc.getNodesPerCluster();
        System.out.println(Arrays.deepToString(npc));
    }
}
