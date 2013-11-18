package de.whs.studmap.data;

public class Floor {

	private int id;
	private String url;
	private String name;
	
	public Floor(int id, String url, String name) {
		this.id = id;
		this.url = url;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public String getUrl() {
		return url;
	}
	public String getName() {
		return name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
