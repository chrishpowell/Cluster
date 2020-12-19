/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.cluster;

import com.zaxxer.hikari.HikariDataSource;

import eu.discoveri.predikt3.config.EnSetup;
import eu.discoveri.predikt3.utils.DbUtils;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.Document;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.language.LanguageComponents;


/**
 *
 * @author chrispowell
 */
public class ClusterDocs
{
    // Dump cluster array
    public static void dumpDCArray( String[][] dc )
    {
        for( int kk=0; kk<2; kk++ )
        {
            if( kk==0 )
                System.out.println("Contents: ");
            else
                System.out.println("Titles:");
            for( int jj=0; jj<5; jj++)
            {
                System.out.println("   " +dc[kk][jj]);
            }
        }
    }
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws SQLException
     * @throws IOException 
     */
    public static void main(String[] args)
            throws SQLException, IOException
    {
        BiConsumer<String,String> docHdr = (u,t) -> System.out.println("Url: " +u+ ", Title: " +t);
        
        // Timer
        long totSecs = 0l;
        Instant st = Instant.now();
        System.out.println("*** Start date/time: " +st);
        
        // Language/locale setup
        System.out.println("... Setup [English]");
        EnSetup enSetup = new EnSetup();
        
        // Check
        System.out.println("Clustering for lang: " +enSetup.getLocale().getDisplayName());
        
        // Setup clustering
        String DocsClustering[][] = new String[318][3];
        LanguageComponents lComps = LanguageComponents.loader().load().language(enSetup.getLocale().getDisplayName());
        LingoClusteringAlgorithm lcAlgo = new LingoClusteringAlgorithm();

        // Get a Db connection from pool
        System.out.println("... Connect to databases");
        HikariDataSource docDbPool = DbUtils.getPooledDocDbConnection();
        
        // Get the documents from the db
        try ( Connection docDb1 = docDbPool.getConnection();
              Statement docs = docDb1.createStatement()      )
        {
            ResultSet rs = docs.executeQuery("select dId, langCode, title, url, content from Document");
            
            // Cluster
            int ii = 0;
            while( rs.next() )
            {
                DocsClustering[ii][1] = rs.getString("content");
                DocsClustering[ii][0] = rs.getString("title");
                DocsClustering[ii][2] = rs.getString("dId");
                ++ii;
            }
//            dumpDCArray(DocsClustering);

            System.out.println("Form document stream...");
            Supplier<Stream<Document>> docStream = () -> Arrays.stream(DocsClustering)
                    .map( flds -> (fieldVisitor) -> {
                        fieldVisitor.accept("dId",flds[2]);
                        fieldVisitor.accept("content",flds[1]);
                        fieldVisitor.accept("title",flds[0]);
                    });
            
            docStream.get().forEach(d -> d.visitFields((f0,f1) -> {
                System.out.println("("+f0+"):["+f1+"]\r\n");
            }));
            
            List<Cluster<Document>> clusters = lcAlgo.cluster(docStream.get(), lComps);
            
            // Print cluster labels and a document count in each top-level cluster.
            System.out.println("Clusters...");
            clusters.forEach(c -> {
                String label = String.join("; ", c.getLabels());
                System.out.println(label + ", documents: " + c.getDocuments().size());
            });
        }
    }
}
