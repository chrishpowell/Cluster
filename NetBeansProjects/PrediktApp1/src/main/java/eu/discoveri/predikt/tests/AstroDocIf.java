/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import org.carrot2.clustering.Document;

/**
 *
 * @author Chris Powell, Discoveri OU
 */
public interface AstroDocIf extends Document
{
    long getId();
    String getLangCode();
    String getTitle();
    String getContent();
}
