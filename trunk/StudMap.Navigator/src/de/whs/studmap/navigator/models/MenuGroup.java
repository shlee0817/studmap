package de.whs.studmap.navigator.models;

import java.util.ArrayList;
import java.util.List;

public class MenuGroup {

	private String string;
	private List<MenuItem> children = new ArrayList<MenuItem>();

	public MenuGroup(String string) {
		this.setString(string);
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public void setChildren(List<MenuItem> children) {
		this.children = children;
	}
}
