/*
 * Scrape Web via Bing, store doc links; (GetDocuments) get docs, clean and cluster docs
 */
package eu.discoveri.predikt.webscrape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
import org.carrot2.source.microsoft.v7.Bing7DocumentSource;
import org.carrot2.source.microsoft.v7.Bing7DocumentSourceDescriptor;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Gather
{
    public static void createClusters( String query, int numResults )
            throws IOException
    {
        // Controller to manage the processing pipeline
        final Controller controller = ControllerFactory.createPooling();

        // Attributes
        final Map<String,Object> attributes = new HashMap<>();

        // API key here! Key2:[b5f7b02d68484c278b663ebcfec86565]
        Bing7DocumentSourceDescriptor.attributeBuilder(attributes).apiKey("3dc9b0801a9f4856b8e5fc86387e4727");

        // Query and the required number of results
        attributes.put(CommonAttributesDescriptor.Keys.QUERY, query);
        attributes.put(CommonAttributesDescriptor.Keys.RESULTS, numResults);

        // Process (pipeline): Bing search/STC clustering
        System.out.println("Searching and clustering...");
        final ProcessingResult result = controller.process(attributes, Bing7DocumentSource.class, STCClusteringAlgorithm.class);

        // Documents and cluster
        final List<Document> documents = result.getDocuments();
        final List<Cluster> clusters = result.getClusters();
        documents.forEach(d -> {
            System.out.println("----> Site: [" +d.getField(Document.CLICK_URL)+"]");
        });

        // Get documents put into 'bucket'
        System.out.println("Storing search results...");
        GetDocuments.storeDocuments(clusters);

        // Display
        //ConsoleFormatter.displayResults(result);
    }
    
    public static void main(String[] args)
            throws IOException
    {
        // Query and num results reqd.
        createClusters( "horoscope",50 );
    }
}
