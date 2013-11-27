package de.whs.studmap.data;

public class Node {
	private int nodeID;
	private String displayName;
	private String roomName;
	private int floorID;
		
	public Node(int nodeID, String roomName, String displayName, int floorID){
		this.nodeID = nodeID;
		this.roomName = roomName;
		this.displayName = displayName;
		this.floorID = floorID;
	}
	
	public int getNodeID() {
		return nodeID;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getRoomName() {
		return roomName;
	}
	
	@Override
	public String toString(){
		
		return roomName + "-" + displayName;
	}

	public int getFloorID() {
		return floorID;
	}

	public void setFloorID(int floorID) {
		this.floorID = floorID;
	}
}
