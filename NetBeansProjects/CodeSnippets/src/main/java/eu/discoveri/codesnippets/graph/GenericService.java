/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.codesnippets.graph;

import eu.discoveri.codesnippets.graphs.GraphEntity;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @param <T> Any class extending GraphEntity
 * @email info@astrology.ninja
 */
public abstract class GenericService<T extends GraphEntity> implements Service<T>
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
    public Iterable<T> findAll()
    {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(Long id)
    {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(Long id)
    {
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public T createOrUpdate(T entity)
    {
        System.out.println("Entity: " +entity.getClass().getPackageName()+":"+entity.getClass().getName());
        Annotation[] ann = entity.getClass().getAnnotations();
        Arrays.stream(ann).forEach(a -> System.out.println("   has annotations: " +a.toString()));
        session.save(entity, DEPTH_ENTITY);
        return find(entity.getNid());
    }

    public abstract Class<T> getEntityType();
}
