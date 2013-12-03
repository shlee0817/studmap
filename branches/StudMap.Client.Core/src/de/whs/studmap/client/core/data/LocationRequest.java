package de.whs.studmap.client.core.data;

import java.util.ArrayList;
import java.util.List;

public class LocationRequest {

	private int MapId;
	
	private int NodeCount;
	
	private List<LocationAPScan> Scans;

	public LocationRequest()
	{
		Scans = new ArrayList<LocationAPScan>();
	}
	
	public int getMapId() {
		return MapId;
	}

	public void setMapId(int mapId) {
		MapId = mapId;
	}

	public int getNodeCount() {
		return NodeCount;
	}

	public void setNodeCount(int nodeCount) {
		NodeCount = nodeCount;
	}
	
	public List<LocationAPScan> getScans(){
		return Scans;
	}
	
	public void addScan(LocationAPScan scan){
		Scans.add(scan);
	}
}
