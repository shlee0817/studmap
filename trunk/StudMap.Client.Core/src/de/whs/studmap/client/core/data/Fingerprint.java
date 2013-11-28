package de.whs.studmap.client.core.data;

import java.util.ArrayList;
import java.util.List;

public class Fingerprint {

	private int NodeId;
	private List<AccessPointScan> AccessPointScans;
	
	public Fingerprint()
	{
		AccessPointScans = new ArrayList<AccessPointScan>();
	}
	
	public int getNodeId() {
		return NodeId;
	}

	public void setNodeId(int nodeId) {
		NodeId = nodeId;
	}

	public List<AccessPointScan> getAccessPointScans() {
		return AccessPointScans;
	}

	public void setAccessPointScans(List<AccessPointScan> accessPointScans) {
		AccessPointScans = accessPointScans;
	}
}
