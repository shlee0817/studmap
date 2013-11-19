package de.whs.studmap.data;

public class Floor {

	private int id;
	private int mapId;
	private String imageUrl;
	private String name;
	
	public Floor(int id, int mapId, String url, String name) {
		this.id = id;
		this.mapId = mapId;
		this.imageUrl = url;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public int getMapId() {
		return mapId;
	}
	public String getUrl() {
		return imageUrl;
	}
	public String getName() {
		return name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
