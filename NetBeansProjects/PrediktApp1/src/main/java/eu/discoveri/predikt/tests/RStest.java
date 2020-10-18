/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Session;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.Result;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class RStest
{
    /**
     * Drop all constraints and indexes.
     * @param sess 
     */
    public static void dropIndexes( Session sess )
    {
        // Constraints
        Result res = sess.run("CALL db.constraints", TransactionConfig.empty());
        while( res.hasNext() )
        {
            Map<String,Object> sn = res.next().asMap();
            sn.entrySet().forEach(v -> {
                if( v.getKey().equals("name") )
                    sess.run( "drop constraint $con", Map.of("con",v.getValue()),  TransactionConfig.empty());
            });
        }
        
        // Indexes
        res = sess.run("CALL db.indexes", TransactionConfig.empty());
        while( res.hasNext() )
        {
            Map<String,Object> sn = res.next().asMap();
            sn.entrySet().forEach(v -> {
                if( v.getKey().equals("name") )
                    sess.run("drop index $idx", Map.of("idx",v.getValue()), TransactionConfig.empty());
            });
        }
    }
    
    public static void main(String[] args)
    {
        // Session
        System.out.println("... Setup graph database");
        Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "karabiner"));

        // Synchronous part
        try( Session sess = driver.session() )
        {
            // Clear database nodes and edges
            System.out.println("... Purge db");
            sess.run("MATCH(n) DETACH DELETE n", TransactionConfig.empty() );

            // Drop and create indexes
            System.out.println("    > Drop and create indexes");
            dropIndexes(sess);
    //        GraphUtils.indexSN( sess );
        }
        
        // Asynchronous part
        AsyncSession asess = driver.asyncSession();  // Note not Autocloseable
        RNode rn0 = new RNode("R0");
        rn0.persist(asess);
        
        Iterable<ANode> ani = sess.query(RNode.class,"match (a:ANode) where a.name=$name return a", Map.of("name","A0"));
        for( ANode an: ani )
        {
            an.addBNode("fred");
            an.persist(sess);
        }
        
        // Close
        driver.close();
    }
}
