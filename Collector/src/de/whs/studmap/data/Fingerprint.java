package de.whs.studmap.data;

import java.util.ArrayList;
import java.util.List;

public class Fingerprint {

	private int NodeId;
	private List<AccessPoint> AccessPoints;
	
	public Fingerprint()
	{
		AccessPoints = new ArrayList<AccessPoint>();
	}
	
	public int getNodeId() {
		return NodeId;
	}

	public void setNodeId(int nodeId) {
		NodeId = nodeId;
	}

	public List<AccessPoint> getAccessPoints() {
		return AccessPoints;
	}

	public void setAccessPoints(List<AccessPoint> accessPoints) {
		AccessPoints = accessPoints;
	}
}
