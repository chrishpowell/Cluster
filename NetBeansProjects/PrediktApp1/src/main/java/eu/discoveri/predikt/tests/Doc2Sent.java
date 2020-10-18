/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.documents.DocumentDb;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphUtils;
import eu.discoveri.predikt.graph.Populate;
import eu.discoveri.predikt.graph.SentenceNode;
import eu.discoveri.predikt.graph.service.SentenceNodeService;
import static eu.discoveri.predikt.main.ProcessDocs.populateGraphWithSents;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neo4j.ogm.session.Session;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Doc2Sent
{
   /**
     * Read docs from (MySQL) database, generate sentences.
     * 
     * @param conn1
     * @param popl
     * @param sns
     * @throws SQLException 
     */
    static int  eIdx = -1;
    public static void populateGraphWithSents( Connection conn1, Populate popl )
            throws SQLException
    {
        // Count of sentences written
        long totSents = 0;
        
        System.out.println("... Get all documents from Rdb");
        PreparedStatement docs = conn1.prepareStatement("select * from Document");
        ResultSet rsDocs = docs.executeQuery();
        
        // Get sentences of the docs
        System.out.println("... Form sentences from docs (Stream of List)");
        
        while( rsDocs.next() )
        {
            eIdx++;
            Stream.Builder<List<SentenceNode>> llsn = Stream.builder();
        
            // (Sentences per document, memory should be OK)
            List<SentenceNode> lsn = popl.extractSentences(rsDocs.getString("content"),eIdx);
            System.out.println("Num. sentences in doc: " +eIdx+ " is " +lsn.size());
            llsn.add(lsn);
            
            // Merge to one list (per document)
            System.out.println("... Form complete (flattened/single) sentence list");
            List<SentenceNode> glsn = llsn.build().flatMap(x -> x.stream()).collect(Collectors.toList());
            
            // Total size
            totSents += glsn.size();
        }
    
        System.out.println("    > Num. documents: " +eIdx+ ", Tot. num. sentences written: " +totSents);
    }
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
            throws Exception
    {
        // Set up OpenNLP (TokenizerME, SentenceDetectorME, POSTaggerME, [Simple]Lemmatizer)
        System.out.println("... Setup NLP");
        Populate popl = new Populate( eu.discoveri.predikt.sentences.LangCode.en );

        // Connect to lemma db
        System.out.println("... Connect to Document database");
        Connection conn1 = DocumentDb.docDb();
        
        // 1b. Get sentences from document db. (What if 250k sentences?)
        populateGraphWithSents( conn1, popl );
        
        // Close
        conn1.close();
    }
}
