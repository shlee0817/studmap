package de.whs.studmap.data;

public class PoI {
	
	private String name;
	private String description;
	private int typeId;
	private Node node;
	
	public PoI(String name, String description, int typeId, Node node) {
		this.name = name;
		this.description = description;
		this.typeId = typeId;
		this.node = node;
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
	
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		
		if (!name.equals(node.getDisplayName())){
			sb.append("-");
			sb.append(node.getDisplayName());
		}
		
		return sb.toString();
	}
}
