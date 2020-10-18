/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.cluster;

import eu.discoveri.predikt3.graph.GraphEntity;


/**
 * (Louvain) cluster in which sentence appears.
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SentenceClusterNum extends GraphEntity
{
    // Cluster number, -1 if no cluster.
    private int  clusterNum = -1;

    public SentenceClusterNum(int clusterNum, String name, String namespace)
    {
        super(name, namespace);
        this.clusterNum = clusterNum;
    }
    
    public SentenceClusterNum()
    {
        this(-1,"","");
    }

    public int getClusterNum() { return clusterNum; }
}
