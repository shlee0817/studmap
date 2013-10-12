package de.whs.fia.studmap.collector.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Enth�lt alle Informationen zu einem Wlan Scan.
 * Dazu geh�ren Informationen zum Ort des Scans,
 * identifiziert �ber die NodeId (anschlie�ende Verkn�pfung mit der Datenbank)
 * und einer Liste von AccessPoints.
 */
public class Scan {

	private int Id;
	
	private int NodeId;
	
	private List<AP> aps = new ArrayList<AP>();
	
	public int getNodeId() {
		return NodeId;
	}

	public void setNodeId(int nodeId) {
		NodeId = nodeId;
	}
	
	public List<AP> getAPs(){
		return aps;
	}
	
	public void addAP(AP ap){
		if(ap != null)
			aps.add(ap);
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}
}
