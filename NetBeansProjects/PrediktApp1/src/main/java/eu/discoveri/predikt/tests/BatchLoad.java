/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package eu.discoveri.predikt.tests;

import eu.discoveri.predikt.graph.DiscoveriSessionFactory;;
import java.util.List;
import org.neo4j.ogm.session.Session;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class BatchLoad
{
    public static void main(String[] args)
    {
        // Session
        System.out.println("... Setup graph database");
        DiscoveriSessionFactory discSess = DiscoveriSessionFactory.getInstance();
        Session sess = discSess.getNewSession();
        
        Ax ax0 = new Ax(0);
        Ax ax1 = new Ax(1);
        
        List<Cx> lcx1 = List.of(new Cx(0),new Cx(1),new Cx(2),new Cx(3));
        List<Cx> lcx2 = List.of(new Cx(4),new Cx(5),new Cx(6),new Cx(7));
        List<Bx> lbx = List.of(new Bx(ax0,ax1,lcx1,0.25),new Bx(ax0,ax1,lcx2,0.75));
        
        Bx.writeBatch(sess, lbx);
        
        discSess.close();
    }
}
