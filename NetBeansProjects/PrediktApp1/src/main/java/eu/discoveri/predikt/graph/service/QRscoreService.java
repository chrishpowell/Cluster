/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.cluster.QRscore;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreService extends EntityService<QRscore>
{
    // See EntityService for bulk of methods
    
    @Override
    public Class<QRscore> getEntityType() { return QRscore.class; }
}
