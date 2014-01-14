package de.whs.studmap.navigator.models;

import de.whs.studmap.client.core.data.Node;

public class Navigation {

	private Node startNode = null;
	private Node endNode = null;
	
	
	public boolean isActiveNavigation() {
		
		return startNode != null && endNode != null;
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public boolean isFinished() {
		
		if(isActiveNavigation())
			return startNode.getNodeID() == endNode.getNodeID();
		
		return false;
	}
	
	public boolean isFloorChangeIsRequired() {
		
		if(!isActiveNavigation())
			return false;
		
		return (startNode.getFloorID() != endNode.getFloorID());
			
	}
}
