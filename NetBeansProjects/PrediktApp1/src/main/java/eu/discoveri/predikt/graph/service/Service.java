/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.graph.GraphEntity;
import java.util.Map;
import org.neo4j.ogm.model.Result;


/**
 *
 * @author Chris Powell, Discoveri OU
 */
interface Service<T extends GraphEntity>
{
    public T find(Long id);

    public Iterable<T> findAll();
    
    public Result findByCypher( String cypher, Map<String,?> parameters );

    public void delete(Long id);
    
    public void deleteByCypher( String cypher, Map<String,?> parameters );

    public T createOrUpdate(T object);
}
