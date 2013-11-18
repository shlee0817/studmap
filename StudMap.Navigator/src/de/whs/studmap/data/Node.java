package de.whs.studmap.data;

public class Node {
	private int nodeID;
	private String displayName;
	private String roomName;
	private int x;
	private int y;
	
	public Node(int nodeID, String roomName, String displayName, int x, int y){
		this.nodeID = nodeID;
		this.roomName = roomName;
		this.displayName = displayName;
		this.x = x;
		this.y = y;
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

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	@Override
	public String toString(){
		
		return roomName + " - " + displayName;
	}
}
