package de.whs.studmap.data;

public class Node {
	private int nodeID;
	private String name;
	
	public Node(int nodeID, String name){
		this.nodeID = nodeID;
		this.name = name;
	}
	
	public int getNodeID() {
		return nodeID;
	}
	
	public String getName() {
		return name;
	}
	
}
