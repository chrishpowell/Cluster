/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import org.neo4j.driver.Session;
import org.neo4j.graphdb.GraphDatabaseService;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class BatchLoad1
{
    /**
     * Write a batch of nodes and relationships.
     * @param sess
     * @param coll Format: [{uuid:nnn,properties:{name:"xxx",score:n.nn}]
     */
    public static void writeBatch( Session sess, List<Bx> coll )
    {
        // First, create all score nodes
        List<Map<String,Object>> lcx = new ArrayList<>();
        coll.forEach(bx -> {
            bx.getLCx().forEach(cx -> {
                lcx.add(Map.of("uuid",cx.getUUID().toString(),"cxVal",cx.getVal()));
            });
        });
        // Create the parameter map
        Map<String,Object> params = Collections.singletonMap("properties",lcx);
        
        // Create the unwind string
        String q = "UNWIND $properties AS p CREATE (c:Cx) SET c = p";
//        String q = "UNWIND $properties AS p MERGE (c:Cx {uuid: cx.uuid}) ON CREATE SET c+= cx.properties";
//        String q =  "UNWIND $mcoll AS mc " +
//            "MATCH (from1: Bx{uuid: mc.fromBx}) MATCH (to1: Ax{identity: mc.toAx}) CREATE (from1)-[:PAIR]->(to1) " +
//            "MATCH (from2: Bx{uuid: mc.fromBX}) MATCH (to2: Ax{identity: mc.toAx}) CREATE (from2)-[:PAIR]->(to2) " +
//            "MATCH (from3: Bx{uuid: mc.fromBx}) MATCH (to3: Cx) WHERE to3.uuid IN mc.toCx CREATE (from3)-[:SCR]-(to3)";
        

        // Write the relationships
        sess.writeTransaction(tx -> tx.run(q,params));
//        // Cx builder
//        StringBuilder sbCx = new StringBuilder("{:param cx=>[");
//        // First, create all score nodes
//        coll.forEach(bx -> {
//            bx.getLCx().forEach(cx -> {
//                sbCx.append("{uuid:").append(cx.getUUID()).append(",properties:{val:").append(cx.getVal()).append("}},");
//            });
//        });
//        sbCx.append("]}");
//        System.out.println("...> "+sbCx.toString());
//        
//        // Write a :param
//        String s = sess.writeTransaction( new TransactionWork<String>()
//        {
//            @Override
//            public String execute( Transaction tx )
//            {
//                Result res = tx.run( ":BEGIN :param x=>[{uuid:1,name:\"X1\"}] :COMMIT", Collections.EMPTY_MAP );
//                return res.toString();
//            }
//        });
//
//        String cxWrite = "UNWIND $cx AS cx MERGE (c:Cx {uuid: cx.uuid}) ON CREATE SET c+= cx.properties";
        //res = sess.query( cxWrite, Collections.EMPTY_MAP );
        
//        // Setup params, flatten list to long string 
//        String mcoll = ":param mcoll=>["+coll.stream().collect(Collectors.joining(","))+"]}";
//        
////        Result res = sess.query( mcoll, Collections.EMPTY_MAP );
//        
//        // Form subgraph from batch
//        String q =  "UNWIND $mcoll AS mc " +
//                    "MATCH (from1: Bx{uuid: mc.fromBx}) MATCH (to1: Ax{identity: mc.toAx}) CREATE (from1)-[:PAIR]->(to1) " +
//                    "MATCH (from2: Bx{uuid: mc.fromBX}) MATCH (to2: Ax{identity: mc.toAx}) CREATE (from2)-[:PAIR]->(to2) " +
//                    "MATCH (from3: Bx{uuid: mc.fromBx}) MATCH (to3: Cx) WHERE to3.uuid IN mc.toCx CREATE (from3)-[:SCR]-(to3)";
////        res = sess.query( q, Collections.EMPTY_MAP );
//
//        System.out.println("--> mcoll: " +mcoll);
//        System.out.println("--> call: "  +q);
    }
    
    public static void main(String[] args)
    {
        // Session
        System.out.println("... Setup graph database");
        Driver gdb = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j","karabiner"));
        
        Ax ax0 = new Ax(0);
        Ax ax1 = new Ax(1);
        
        List<Cx> lcx = List.of(new Cx(0),new Cx(1),new Cx(2),new Cx(3));
        List<Bx> lbx = List.of(new Bx(ax0,ax1,lcx,0.25));
        
        try( Session sess = gdb.session() )
        {
            writeBatch(sess, lbx);
        }
    }
}
