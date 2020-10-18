/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.config.Constants;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class DiscoveriSessionFactory
{
    // final String NEO4J = "bolt://localhost";
    // final String USER = "neo4j", PWD = "password";
    private static final Configuration configuration = new Configuration.Builder()
                                                        .uri("bolt://localhost")
                                                        .credentials("neo4j", "karabiner")
                                                        .build();
    private static final SessionFactory sessionFactory = new SessionFactory(configuration, Constants.PKGGRAPH,Constants.PKGSENTS,Constants.PKGCLUSTER,Constants.PKGTESTS);
    private static final DiscoveriSessionFactory inst = new DiscoveriSessionFactory();

    public static DiscoveriSessionFactory getInstance()
    {
        return inst;
    }

    // Prevent external instantiation
    private DiscoveriSessionFactory() {}

    // Session
    public Session getNewSession()
    {
        return sessionFactory.openSession();
    }
    
    // Session factory
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
    
    // Close (all resources)
    public void close()
    {
        sessionFactory.close();
    }
}
