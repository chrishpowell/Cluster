/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.predikt3.cluster;

/**
 *
 * @author chrispowell
 */
public class ClusterDocSent
{
    private int     clusterNum,
                    docId,
                    nid;

    public ClusterDocSent(int clusterNum, int docId, int nid)
    {
        this.clusterNum = clusterNum;
        this.docId = docId;
        this.nid = nid;
    }

    public int getClusterNum() { return clusterNum; }
    public int getDocId() { return docId; }
    public int getNid() { return nid; }
}
