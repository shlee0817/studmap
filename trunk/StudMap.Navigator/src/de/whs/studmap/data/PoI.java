package de.whs.studmap.data;

public class PoI {
	
	private String name;
	private String description;
	private int typeId;
	private int nodeId;
	
	public PoI(String name, String description, int typeId, int nodeId) {
		this.name = name;
		this.description = description;
		this.typeId = typeId;
		this.nodeId = nodeId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getTypeId() {
		return typeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	@Override
	public String toString(){
		
		return name + " - " + description;
	}
}
