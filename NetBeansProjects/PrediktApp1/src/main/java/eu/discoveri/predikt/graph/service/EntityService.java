/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;
import eu.discoveri.predikt.graph.GraphEntity;

import java.lang.annotation.Annotation;
import java.util.Map;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @param <T> Any class extending GraphEntity
 * @email info@astrology.ninja
 */
public abstract class EntityService<T extends GraphEntity> implements Service<T>
{
    // Load depth, here just entity simple properties, no relationships
    // Default: 1, load simple properties of the entity and its immediate relations.
    private static final int DEPTH_LIST = 1;
    // Save entity and immediate relations.  -1 not for relationsships!
    // Default: -1, save everything reachable from this entity that has been modified
    private static final int DEPTH_ENTITY = 1;
    // Session.  Note Session is not thread safe.
    protected Session session = DiscoveriSessionFactory.getInstance().getSession();

    @Override
    public T find(Long id)
    {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public Iterable<T> findAll()
    {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }
    
    @Override
    public Result findByCypher( String cypher, Map<String,?> params )
    {
        return session.query(cypher, params);
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
        Annotation[] ann = entity.getClass().getAnnotations();
        session.save(entity, DEPTH_ENTITY);
        return find(entity.getNid());
    }

    public abstract Class<T> getEntityType();
}
