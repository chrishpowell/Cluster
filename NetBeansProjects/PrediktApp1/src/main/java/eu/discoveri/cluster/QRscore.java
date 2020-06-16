/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.cluster;

import eu.discoveri.predikt.graph.GraphEntity;
import eu.discoveri.predikt.graph.SentenceNode;
import java.io.Serializable;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
@NodeEntity
public class QRscore extends GraphEntity implements Serializable
{
    // Serialization
    private static final long   serialVersionUID = 111L;
    
    @Relationship(type="NODEKEY")
    private SentenceNode        node1;
    @Relationship(type="NODEKEY")
    private SentenceNode        node2;
    // Score
    private Double              score;
    
    /**
     * Constructor.
     */
    public QRscore(){}

    /*
     * Getters
     */
    public SentenceNode getNode1() { return node1; }
    public SentenceNode getNode2() { return node2; }
    public Double getScore() { return score; }
    
    /*
     * Setters
     */
    public void setNode1(SentenceNode node1) { this.node1 = node1; }
    public void setNode2(SentenceNode node2) { this.node2 = node2; }
    public void setScore(Double score) { this.score = score; }
}
