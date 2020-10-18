/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.tests.Bx;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class BxService extends EntityService<String,Bx>
{
    // See EntityService for bulk of methods
    @Override
    public Class<Bx> getEntityType() { return Bx.class; }
}
