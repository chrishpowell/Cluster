/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.documents.DocumentDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.dbutils.ResultSetIterator;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.Document;             // Note: This is a functional interface
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.language.LanguageComponents;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DbStream
{
    static int cId = 0;
    public static void main(String[] args)
            throws Exception
    {
        // Connect to document db
        System.out.println("... Connect to doc db");
        
        // Get the docs into result set
        Stream<AstroDocCluster> sd;
        try (Connection conn1 = DocumentDb.docDb())
        {
            // Get the docs into result set
            System.out.println("... Get all documents from Rdb");
            PreparedStatement docs = conn1.prepareStatement("select * from Document");
            ResultSet rsDocs = docs.executeQuery();
            
            // Convert to stream
            Iterable<Object[]> rsi = ResultSetIterator.iterable(rsDocs);
            sd = StreamSupport.stream(rsi.spliterator(), false)
                    .map( rowf -> new AstroDocCluster((Long)rowf[0],(String)rowf[3],(String)rowf[4],(String)rowf[6]) );
        
            // Now, classify the docs
            // Our documents are in English so we load appropriate language resources.
            // This call can be heavy and an instance of LanguageComponents should be
            // created once and reused across different clustering calls.
            LanguageComponents languageComponents = LanguageComponents.loader().load().language("English");
            LingoClusteringAlgorithm lca = new LingoClusteringAlgorithm();

            // Suggest num. clusters
            lca.desiredClusterCount.set(20);

            // Perform clustering.
            List<Cluster<Document>> clusters;
            clusters = lca.cluster(sd, languageComponents);

            // Print cluster labels and a document count in each top-level cluster.
            Statement stmt = conn1.createStatement();
            for( Cluster<Document> c: clusters )
            {
                String label = String.join("; ", c.getLabels());
                System.out.println(label + ", num. docs: " + c.getDocuments().size());
                System.out.println("     ");
                
                c.getDocuments().forEach(a -> {
                    System.out.print(" [" +((AstroDocCluster)a).getId()+"]");
                    String q = "UPDATE Document set cId='" +cId+ "', cTags='" +c.getLabels().toString().replaceAll(", ", " ").replaceAll("\\[|\\]", "").replaceAll("'", "\'")
                            + "' WHERE id='" +((AstroDocCluster)a).getId()+ "'";

                    try {
                        stmt.executeUpdate(q);
                    } catch (SQLException ex) {
                        Logger.getLogger(DbStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                
                ++cId;
            }
        }
    }
}
