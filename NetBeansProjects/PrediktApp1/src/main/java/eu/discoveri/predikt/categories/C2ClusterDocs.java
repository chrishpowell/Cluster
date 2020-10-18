/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt.categories;

import java.io.IOException;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class C2ClusterDocs
{
    public void clusterDocumentStream()
            throws IOException
    {
        // fragment-start{setup-heavy-components}
        // Our documents are in English so we load appropriate language resources.
        // This call can be heavy and an instance of LanguageComponents should be
        // created once and reused across different clustering calls.
        LanguageComponents languageComponents = LanguageComponents.loader().load().language("English");
        // fragment-end{setup-heavy-components}

        // fragment-start{setup-lightweight-components}
        LingoClusteringAlgorithm algorithm = new LingoClusteringAlgorithm();
        // fragment-end{setup-lightweight-components}

        // fragment-start{clustering-document-stream}
        // Create a stream of "documents" for clustering.
        // Each such document provides text content fields to a visitor.
        Stream<Document> documentStream =
            Arrays.stream(ExamplesData.DOCUMENTS_DATA_MINING)
                .map(
                    fields ->
                        (fieldVisitor) -> {
                          fieldVisitor.accept("title", fields[1]);
                          fieldVisitor.accept("content", fields[2]);
                        });
        // fragment-end{clustering-document-stream}

        // fragment-start{clustering}
        // Perform clustering.
        List<Cluster<Document>> clusters;
        clusters = algorithm.cluster(documentStream, languageComponents);

        // Print cluster labels and a document count in each top-level cluster.
        for (Cluster<Document> c : clusters) {
          String label = String.join("; ", c.getLabels());
          System.out.println(label + ", documents: " + c.getDocuments().size());
        }
        // fragment-end{clustering}
      }
}
