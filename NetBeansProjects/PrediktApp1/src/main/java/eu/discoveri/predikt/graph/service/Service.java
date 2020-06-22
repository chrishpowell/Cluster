/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.graph.GraphEntity;
import java.util.Collection;
import java.util.Map;


/**
 *
 * @author Chris Powell, Discoveri OU
 */
interface Service<K, T extends GraphEntity>
{
    public T find(Long id);
    
    public Collection<T> findByKey( String keyName, K key );

    public Iterable<T> findAll();
    
    public Iterable<T> findByCypher( String cypher, Map<String,?> parameters );

    public void delete(Long id);
    
    public void deleteByCypher( String cypher, Map<String,?> parameters );

    public T createOrUpdate(T object);
}
