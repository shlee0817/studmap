package de.whs.studmap.data;

public class Node {
	private int nodeID;
	private String displayName;
	private String roomName;
	
	public Node(int nodeID, String roomName, String displayName){
		this.nodeID = nodeID;
		this.roomName = roomName;
		this.displayName = displayName;
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
		
		return roomName + " - " + displayName;
	}
}
