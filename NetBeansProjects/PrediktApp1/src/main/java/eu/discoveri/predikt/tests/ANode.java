/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.GraphEntity;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.session.Session;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ANode extends GraphEntity
{
    @Relationship(type="BN")
    private List<BNode> bns = new ArrayList<>();
    
    public ANode(String name, List<BNode> bns)
    {
        super(name,"eu.discoveri");
        this.bns = bns;
    }
    
    public ANode(String name)
    {
        this(name,new ArrayList<>());
    }
    
    public ANode(){}
    
    public void persist( Session session )
    {
        session.save(this, 1);
    }
    
    public List<BNode> addBNode( String bn )
    {
        this.bns.add(new BNode(bn));
        return bns;
    }
}
