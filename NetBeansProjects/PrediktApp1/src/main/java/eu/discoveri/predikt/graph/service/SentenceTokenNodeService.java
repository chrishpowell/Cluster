/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph.service;

import eu.discoveri.predikt.graph.SentenceTokenNode;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class SentenceTokenNodeService extends EntityService<SentenceTokenNode>
{
    // See EntityService for bulk of methods
    
    @Override
    public Class<SentenceTokenNode> getEntityType() { return SentenceTokenNode.class; }
}
