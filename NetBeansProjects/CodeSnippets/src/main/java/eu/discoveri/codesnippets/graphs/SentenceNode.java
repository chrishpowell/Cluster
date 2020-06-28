/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.Query;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;


/**
 * Neo4j node for a sentence.
 * @author Chris Powell, Discoveri OU
 * @email info@discoveri.eu
 */
@NodeEntity
public class SentenceNode extends AbstractVertex
{
    // Basic attrs
    private String                sentence = null;
    private double                score = 0.d;
    private List<String>          tokens = null;
    // Maps don't work in neo4j!!
    //private Map<String,TToken>    mapToks = new HashMap<>();
    
    // Subgraph label
    private String                subgraph = null;
    
    // Distance
    private double                dist = Double.MAX_VALUE;
    // Between
    private double                between = 0.d, cbetween = 0.d;
    // Shortest paths (will be changed by edge weight)
    private double                spath = 0.d;
    // 'Prev.' node along path
    @Relationship(type="PREV")
    private SentenceNode          prev = null;

    // Predecessors
    private List<SentenceNode>    preds = new ArrayList<>();

    // Neo4j params
    @Properties
    private Map<String,Object>    params = new HashMap<>();


    /**
     * Constructor.
     * 
     * @param namespace
     * @param name
     * @param sentence
     * @param tokens
     * @param score 
     */
    public SentenceNode(String namespace, String name, String sentence, List<String> tokens, double score)
    {
        super(name,namespace);
        this.sentence = sentence;
        this.score = score;
        this.tokens = tokens;

        // Build map for graph (tokens need to be processed separately)
        params = Map.of("name", name, "sentence",sentence, "score",score);
        
        // Maps do NOT work with Neo4j!!!!
//        tokens.forEach(t -> {
//            mapToks.put(t, new TToken(t,name));
//        });
    }
    
    /**
     * Constructor.
     * @param name
     * @param sentence
     * @param tokens
     * @param score 
     */
    public SentenceNode(String name, String sentence, List<String> tokens, double score)
    {
        this("eu.discoveri.predikt",name,sentence,tokens,score);
    }
    
    public SentenceNode(String name, String sentence)
    {
        this("eu.discoveri.predikt",name,sentence,new ArrayList<String>(),0.d);
    }
    
    /**
     * No arg constructor for loading from db.
     */
    private SentenceNode()
    {
        this("eu.discoveri.predikt","",null,0.0d);
    }


    public String getSentence() { return sentence; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public List<SentenceNode> getPreds() { return preds; }
    public void setPreds(List<SentenceNode> preds) { this.preds = preds; }
    
    public double getDist() { return dist; }
    public void setDist(double dist) { this.dist = dist; }

    public double getBetween() { return between; }
    public void setBetween(double between) { this.between = between; }

    public double getCbetween() { return cbetween; }
    public void setCbetween(double cbetween) { this.cbetween = cbetween; }

    public double getSpath() { return spath; }
    public void setSpath(double spath) { this.spath = spath; }
    public void incrSPath(double vpath) { this.spath += vpath; }

    public SentenceNode getPrev() { return prev; }
    public void setPrev(SentenceNode prev) { this.prev = prev; }

    public List<String> getTokens() { return tokens; }
    
    public String getSubgraph() { return subgraph; }
    public void setSubgraph(String subgraph) { this.subgraph = subgraph; }

    
    /**
     * Create a new sentence node, ready for persisting.
     * @return Ordered set of Queries
     */
    public Set<Query> buildSentenceQuerySet()
    {
        // Ordered Set
        Set<Query> lhs = new LinkedHashSet<>();
        // Map for tokens
        Map<String,Object> ps = new HashMap<>();
        
        // Add first query
        String s = "CREATE ("+getName()+":Sentence) SET "+getName()+"={sid:$sid,name:$name,sentence:$sentence,score:$score}";
        lhs.add( new Query(s,params) );

        // Create tokens of the sentence
        StringBuilder q0 = new StringBuilder();
        int idx = 0;
        for( String t: tokens )
        {
            q0.append("CREATE (t").append(idx).append(getName())
                    .append(":Token {text:$text").append(idx)
                    .append(",tid:$tid").append(idx).append("}) ");

            ps.put("text"+idx,t);
            ps.put("tid"+idx,"t"+idx+getName());
            // Incr idx
            ++idx;
        }
        // Create the query
        lhs.add( new Query(q0.toString(),ps) );
        
        // Build edges
        idx = 0;
        for( String t: tokens )
        {
            StringBuilder q1 = new StringBuilder();
            q1.append(" MATCH (src:Sentence {sid:$sid}) MATCH (tgt:Token {tid:$tid})")
                    .append(" MERGE (src)-[:TOK]-(tgt)");
            
            // Create the query
            lhs.add( new Query(q1.toString(),parameters("sid",getName(),"tid","t"+idx+getName())) );
            // Incr idx
            ++idx;
        }
//        q1.append(" RETURN " +idx);

        return lhs;
    }

    
    /**
     * Save on session
     * @param ss
     * @return 
     */
    public SentenceNode persist( SentenceNodeService ss )
    {
        return ss.createOrUpdate(this);
    }
    
    /**
     * Delete on session
     * @param ss 
     */
    public void delete( SentenceNodeService ss )
    {
        ss.delete(getNid());
    }
    
    /**
     * Find a SentenceNode via nid
     * @param nid
     * @param ss
     * @return 
     */
    public SentenceNode find( SentenceNodeService ss, Long nid )
    {
        return ss.find(nid);
    }

    /**
     * Hash.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + getUUID().hashCode();
        return hash;
    }

    /**
     * Equals.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        final SentenceNode other = (SentenceNode) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getNameNamespace().equals(other.getNameNamespace());
    }
    
    /**
     * Simple output
     * @return 
     */
    @Override
    public String toString()
    {
        return getName()+" ("+tokens.size()+")";
    }
}

class TToken
{
    private String  s1;
    private String  s2;
    
    public TToken(String s1,String s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }
}
