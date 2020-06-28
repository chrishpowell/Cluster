/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@predikt.eu
 */
@NodeEntity
public class SentenceTokenNode extends AbstractVertex
{
    private String      sentence;
    private List<Token> tokens;
    private double      score;

    // Neo4j params (if you want name to appear on graph, for example)
    @Properties
    private Map<String,Object>    params = new HashMap<>();
    
    /**
     * Constructor.
     * 
     * @param name
     * @param sentence
     * @param tokens
     * @param score 
     */
    public SentenceTokenNode(String name, String sentence, List<Token> tokens, double score)
    {
        super("",name);
        this.sentence = sentence;
        this.score = score;
        this.tokens = tokens;

        // Build map for graph (tokens need to be processed separately)
        params.put("name", name);
    }
    
    public SentenceTokenNode(String name, String sentence)
    {
        this(name,sentence,null,0.d);
    }
    
    /**
     * Save on session
     * @param ss
     * @return 
     */
    public SentenceTokenNode persist( SentenceTokenNodeService ss )
    {
        return ss.createOrUpdate(this);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        final SentenceTokenNode other = (SentenceTokenNode) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getUUID().equals(other.getUUID());
    } 
}
