/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.GraphEntity;
import eu.discoveri.predikt.graph.service.BxService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class Bx extends GraphEntity
{
    @Relationship(type="BA")
    private Ax              Qnode;
    @Relationship(type="BA")
    private Ax              Rnode;
    @Relationship(type="BC")
    private List<Cx>        lcx;
    
    private double          bxVal;

    
    public Bx( Ax Qnode, Ax Rnode, List<Cx> lcx, double bxVal )
    {
        super("");
        this.Qnode = Qnode;
        this.Rnode = Rnode;
        this.lcx = lcx;
        this.bxVal = bxVal;
    }
    
    /**
     * No arg constructor for neo4j
     */
    public Bx(){}
    
    /**
     * Get scores
     * @return 
     */
    public List<Cx> getLCx() { return lcx; }
    
    /**
     * Save on session
     * @param bxs
     * @return 
     */
    public Bx persist( BxService bxs )
    {
        return bxs.createOrUpdate(this);
    }
    
    
    /**
     * Write a batch of nodes and relationships.
     * @param sess
     * @param coll Format: [{uuid:nnn,properties:{name:"xxx",score:n.nn}]
     */
    public static void writeBatch( Session sess, List<Bx> coll )
    {
        // Form subgraph from batch
//        String q =  "UNWIND $mcoll AS mc " +
//                    "MATCH (from1: Bx{uuid: mc.fromBx}) MATCH (to1: Ax{identity: mc.toAx}) CREATE (from1)-[:PAIR]->(to1) " +
//                    "MATCH (from2: Bx{uuid: mc.fromBX}) MATCH (to2: Ax{identity: mc.toAx}) CREATE (from2)-[:PAIR]->(to2) " +
//                    "MATCH (from3: Bx{uuid: mc.fromBx}) MATCH (to3: Cx) WHERE to3.uuid IN mc.toCx CREATE (from3)-[:SCR]-(to3)";
        
        // First clear the session (speeds up relationship creation)
        sess.clear();

        // Write the batch
        try( Transaction tx = sess.beginTransaction() )
        {
            coll.forEach(bx -> {
                sess.save(bx,1);
            });
            
            tx.commit();
        }
    }
}
