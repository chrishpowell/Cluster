/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;

import eu.discoveri.predikt.cluster.QRscoreCW;
import java.util.List;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class ScoreNSim
{
    // QRscoreCW
    private QRscoreCW           qrscw;
    // SentenceNode(s)
    private List<SentenceNode>  lsn;
    
    public ScoreNSim( QRscoreCW qrscw, List<SentenceNode>  lsn )
    {
        this.qrscw = qrscw;
        this.lsn = lsn;
    }

    public QRscoreCW getQrscw() { return qrscw; }
    public List<SentenceNode> getLsn() { return lsn; }
}
