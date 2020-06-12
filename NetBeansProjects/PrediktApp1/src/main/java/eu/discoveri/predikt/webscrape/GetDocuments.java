/*
 * Clean scraped data via JSoup, output clustered documents via Carrot2.
 * Store via Sphinx in future.
 */
package eu.discoveri.predikt.webscrape;

import eu.discoveri.predikt.config.Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.carrot2.core.Cluster;
import org.carrot2.core.Document;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class GetDocuments
{
    /**
     * Get and store documents from cluster search.  Uses default storage
     * details and number clusters to process.
     * 
     * @param clusterList
     * @return
     * @throws IOException 
     */
    public static List<File> storeDocuments( List<Cluster> clusterList )
            throws IOException
    {
        return storeDocuments( clusterList, Constants.TOPNUMCLUSTERS, Constants.STORECLUSTERSPATH, Constants.STORECLUSTERSEXT );
    }
    
    /**
     * Get and store documents from cluster search.  Uses default storage
     * details.
     * 
     * @param clusterList
     * @param topNumClusters Num. clusters to process
     * @return
     * @throws IOException 
     */
    public static List<File> storeDocuments( List<Cluster> clusterList, int topNumClusters )
            throws IOException
    {
        return storeDocuments( clusterList, topNumClusters, Constants.STORECLUSTERSPATH, Constants.STORECLUSTERSEXT );
    }
    
    /**
     * Get and store documents from cluster search.
     * 
     * @param clusterList List of clusters 
     * @param topNumClusters Num. clusters to process
     * @param storeClusterPath
     * @param storeClusterExt
     * @return
     * @throws IOException 
     */
    public static List<File> storeDocuments( List<Cluster> clusterList, int topNumClusters, String storeClusterPath, String storeClusterExt )
    {
        // If an extension, prepend with a dot
        if( storeClusterExt != "" && storeClusterExt != null )
            storeClusterExt = "." + storeClusterExt;

        // List of clusters (files)
        List<File> fileList = new ArrayList<>();

        // For each cluster, get documents in cluster
        for( int ii=0; ii<topNumClusters; ii++ )
        {
            // Get some cluster entries
            Cluster c = clusterList.get(ii);
            String locn = "WholeWorld";
            
            // Get all documents in this cluster and slap into new file
            System.out.println("Connect to search term and strip tags...");
            try( // Create a (new) file with no whitespace in name
                 BufferedWriter writer = new BufferedWriter( new FileWriter(storeClusterPath+c.getLabel().replaceAll("\\s+","")+storeClusterExt,false) ))
            {
                // Get all documents in this cluster and slap into new file
                for( org.carrot2.core.Document d : c.getAllDocuments() )
                {
                    // Get content...
                    org.jsoup.nodes.Document doc = null;
                    try
                    {
//                        locn = d.getField(org.carrot2.core.Document.CLICK_URL);
                        doc = Jsoup.connect(d.getContentUrl()).get();
                    }
                    catch( IOException iox )
                    {
                        System.out.println("Cannot connect to: " +locn+ ", " +iox.getMessage());
                        continue;
                    }
                    // Clean content
                    String cleanText = Jsoup.clean(doc.body().text(),Whitelist.none()).replaceAll("&amp;", "&");
                    
                    // Store
                    writer.append(cleanText);
                }
                // For console display
                //System.out.println("...> Cluster:  " +c.toString());

                // Store the cluster list (log) for future analysis
                try( // Create a (new) file with no whitespace in name
                     BufferedWriter cLog = new BufferedWriter( new FileWriter(storeClusterPath+c.getLabel().replaceAll("\\s+","")+".log",false) ))
                {
                    String clusterLog = clusterToString(1,"dsc",c,3);
                    cLog.write(clusterLog);
                }
                catch( IOException iox )
                    { iox.printStackTrace(); }
            }
            catch( SocketTimeoutException stex )
                { System.out.println("Socket timeout, cannot connect to: " +locn); }
            catch( IOException iox )
                { iox.printStackTrace(); }
        }

        // Return a list of documents of the cluster
        return fileList;
    }


    /**================================[LOG]====================================
     * Format simple string output for document.
     * 
     * @param level
     * @param document
     * @return 
     */
    private static String documentToString( final int level, Document document )
    {
        // Building the string
        StringBuilder sc = new StringBuilder();
        // Formatter appender
        Formatter fmt = new Formatter(sc);

        // Doc Id
        fmt.format("%"+level+"s [%2s] "," ", document.getStringId());
        // Doc title
        fmt.format(" %s%n", document.getTitle());
        // Doc summary
        //fmt.format("%s%n",document.getSummary());
        
        // URL
        fmt.format("%"+(level+5)+"s %s%n"," ",document.getContentUrl());

        return sc.toString();
    }
    
    /**
     * Format string output for cluster.
     * 
     * @param level
     * @param tag  Usually count within cluster
     * @param cluster
     * @param maxNumberOfDocumentsToShow (in cluster)
     * @param clusterDetailsFormatter
     * @return 
     */
    private static String clusterToString( int level, String tag, Cluster cluster,
                                           int maxNumberOfDocumentsToShow )
    {
        // Level must be 1 or more
        if( level <= 0 ) level = 1;
        
        // Building the string
        StringBuilder sc = new StringBuilder();
        // Formatter appender
        Formatter fmt = new Formatter(sc);
        
        // First, the label
        fmt.format("%s: ", cluster.getLabel());

        // Indent up to level and display this cluster's description phrase
        Double score = cluster.getScore();
        fmt.format("%"+level+"s (%d docs", " ",cluster.getAllDocuments().size());
        if( score != null ) fmt.format(", score: %6.2f)%n", score); else fmt.format(")%n");

        // if this cluster has documents, display maxNumberOfDocumentsToShow documents.
        int documentsShown = 0;
        for( final Document document: cluster.getDocuments() )
        {
            if( documentsShown >= maxNumberOfDocumentsToShow ) break;

            documentToString(level + 1, document);
            documentsShown++;
        }
        if (maxNumberOfDocumentsToShow > 0
            && (cluster.getDocuments().size() > documentsShown))
        {
            fmt.format("%"+(level+1)+"s ... and %d more%n", " ",(cluster.getDocuments().size() - documentsShown));
        }

        // Finally, if this cluster has subclusters, descend into recursion.
        int num = 1;
        for (final Cluster subcluster : cluster.getSubclusters())
        {
            clusterToString( level + 1, tag + "." + num, subcluster, maxNumberOfDocumentsToShow );
        }
        
        return sc.toString();
    }
}
