/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.cluster.QRscoreCW;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class QRscoreCWService extends EntityService<String,QRscoreCW>
{
    // See EntityService for bulk of methods
    
    @Override
    public Class<QRscoreCW> getEntityType() { return QRscoreCW.class; }
}
