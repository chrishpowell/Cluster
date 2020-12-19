/*******************************************************************************

 ******************************************************************************/
package eu.discoveri.hiercluster;

import java.util.*;

/**
 * 
 * @author chrispowell
 */
public class DefaultClusteringAlgorithm implements ClusteringAlgorithm
{
    /**
     * 
     * @param distances
     * @param clusterNames
     * @param linkageStrategy
     * @return 
     */
    @Override
    public Cluster performClustering(double[][] distances,
                                     String[] clusterNames, LinkageStrategy linkageStrategy)
    {
        checkArguments(distances, clusterNames, linkageStrategy);
        
        /* Setup model */
        List<Cluster> clusters = createClusters(clusterNames);
        DistanceMap linkages = createLinkages(distances, clusters);

        /* Process */
        HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
        while( !builder.isTreeComplete() )
        {
            builder.agglomerate(linkageStrategy);
        }

        return builder.getRootCluster();
    }

    /**
     * 
     * @param distances
     * @param clusterNames
     * @param linkageStrategy
     * @param threshold
     * @return 
     */
    @Override
    public List<Cluster> performFlatClustering(double[][] distances,
                                               String[] clusterNames, LinkageStrategy linkageStrategy, Double threshold)
    {
        checkArguments(distances, clusterNames, linkageStrategy);
        
        /* Setup model */
        List<Cluster> clusters = createClusters(clusterNames);
        DistanceMap linkages = createLinkages(distances, clusters);

        /* Process */
        HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
        return builder.flatAgg(linkageStrategy, threshold);
    }

    /**
     * 
     * @param distances
     * @param clusterNames
     * @param linkageStrategy 
     */
    private void checkArguments(double[][] distances, String[] clusterNames,
                                LinkageStrategy linkageStrategy)
    {
        if (distances == null || distances.length == 0
                || distances[0].length != distances.length)
        {
            throw new IllegalArgumentException("Invalid distance matrix");
        }
        
        if (distances.length != clusterNames.length)
        {
            throw new IllegalArgumentException("Invalid cluster name array");
        }
        
        if (linkageStrategy == null)
        {
            throw new IllegalArgumentException("Undefined linkage strategy");
        }
        
        int uniqueCount = new HashSet<>(Arrays.asList(clusterNames)).size();
        if (uniqueCount != clusterNames.length)
        {
            throw new IllegalArgumentException("Duplicate names");
        }
    }

    /**
     * 
     * @param distances
     * @param clusterNames
     * @param weights
     * @param linkageStrategy
     * @return 
     */
    @Override
    public Cluster performWeightedClustering(double[][] distances, String[] clusterNames,
                                             double[] weights, LinkageStrategy linkageStrategy)
    {
        checkArguments(distances, clusterNames, linkageStrategy);

        if (weights.length != clusterNames.length)
        {
            throw new IllegalArgumentException("Invalid weights array");
        }

        /* Setup model */
        List<Cluster> clusters = createClusters(clusterNames, weights);
        DistanceMap linkages = createLinkages(distances, clusters);

        /* Process */
        HierarchyBuilder builder = new HierarchyBuilder(clusters, linkages);
        while (!builder.isTreeComplete())
        {
            builder.agglomerate(linkageStrategy);
        }

        return builder.getRootCluster();
    }

    /**
     * 
     * @param distances
     * @param clusters
     * @return 
     */
    private DistanceMap createLinkages(double[][] distances,
                                       List<Cluster> clusters)
    {
        DistanceMap linkages = new DistanceMap();
        for (int col = 0; col < clusters.size(); col++)
        {
            for (int row = col + 1; row < clusters.size(); row++)
            {
                ClusterPair link = new ClusterPair();
                Cluster lCluster = clusters.get(col);
                Cluster rCluster = clusters.get(row);
                link.setLinkageDistance(distances[col][row]);
                link.setlCluster(lCluster);
                link.setrCluster(rCluster);
                linkages.add(link);
            }
        }
        
        return linkages;
    }

    /**
     * 
     * @param clusterNames
     * @return 
     */
    private List<Cluster> createClusters(String[] clusterNames)
    {
        List<Cluster> clusters = new ArrayList<>();
        for (String clusterName : clusterNames)
        {
            Cluster cluster = new Cluster(clusterName);
            cluster.addLeafName(clusterName);
            clusters.add(cluster);
        }
        
        return clusters;
    }

    /**
     * 
     * @param clusterNames
     * @param weights
     * @return 
     */
    private List<Cluster> createClusters(String[] clusterNames, double[] weights)
    {
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < weights.length; i++)
        {
            Cluster cluster = new Cluster(clusterNames[i]);
            cluster.setDistance(new Distance(0.0, weights[i]));
            clusters.add(cluster);
        }
        
        return clusters;
    }
}
