package de.whs.studmap.navigator.models;

public class MenuItem {

	private String string;
	private int Id;

	public MenuItem(String string, int id) {
		super();
		this.string = string;
		Id = id;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}
}
