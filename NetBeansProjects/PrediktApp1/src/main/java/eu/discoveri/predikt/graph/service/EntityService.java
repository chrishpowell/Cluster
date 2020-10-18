/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphEntity;

import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @param <K> Key (for finding by key)
 * @param <T> Any class extending GraphEntity
 * @email info@astrology.ninja
 */
public abstract class EntityService<K,T extends GraphEntity> implements Service<K,T>
{
    // Session.  Note Session is not thread safe.
    protected Session session = DiscoveriSessionFactory.getInstance().getNewSession();

    @Override
    public T find(Long id)
    {
        return session.load(getEntityType(), id, Constants.DEPTH_ENTITY);
    }
    
    @Override
    public Collection<T> findByKey( String keyName, K key )
    {
        Filter filter = new Filter(keyName,ComparisonOperator.EQUALS,key);
        return session.loadAll(getEntityType(), filter);
    }

    @Override
    public Iterable<T> findAll()
    {
        return session.loadAll(getEntityType(), Constants.DEPTH_LIST);
    }
    
    @Override
    public Iterable<T> findByCypher( String cypher, Map<String,?> params )
    {
        return session.query(getEntityType(), cypher, params);
    }

    @Override
    public void delete(Long id)
    {
        session.delete(session.load(getEntityType(), id));
    }
    
    @Override
    public void deleteByCypher( String cypher, Map<String,?> params )
    {
        session.delete(session.queryForObject(getEntityType(),cypher,params));
    }

    @Override
    public T createOrUpdate(T entity)
    {
        session.save(entity, Constants.DEPTH_ENTITY);
        return find(entity.getNid());
    }

    public abstract Class<T> getEntityType();
}
