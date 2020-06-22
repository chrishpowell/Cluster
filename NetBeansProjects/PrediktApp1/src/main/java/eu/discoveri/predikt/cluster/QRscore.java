/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.cluster;

import eu.discoveri.predikt.graph.GraphEntity;
import eu.discoveri.predikt.graph.SentenceNode;

import org.neo4j.ogm.annotation.Relationship;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscore extends GraphEntity
{
    @Relationship(type="SCORE")
    private SentenceNode            node1;
    @Relationship(type="SCORE")
    private SentenceNode            node2;
    // Score
    private double                  score;
    
    /**
     * Constructor.
     * @param node1
     * @param node2
     * @param score
     */
    public QRscore(SentenceNode node1, SentenceNode node2, Double score )
    {
        super(node1.getName()+node2.getName(),"eu.discoveri.predikt");
        
        this.node1 = node1;
        this.node2 = node2;
        this.score = score;
    }
    
    /**
     * No arg constructor for db load.
     */
    public QRscore(){}

    /*
     * Getters
     */
    public SentenceNode getNode1() { return node1; }
    public SentenceNode getNode2() { return node2; }
    public Double getScore() { return score; }
}
