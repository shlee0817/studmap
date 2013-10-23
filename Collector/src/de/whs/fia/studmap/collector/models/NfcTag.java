package de.whs.fia.studmap.collector.models;

import java.util.StringTokenizer;

public class NfcTag {
	
	public static final String STRINGSEPARATOR = "<;>";
	
	private String id = "";
	private String tagInfo = "";
	
	public NfcTag(String tagInfo){
		StringTokenizer tokenizer = new StringTokenizer(tagInfo, STRINGSEPARATOR);
		
		this.id = tokenizer.nextToken();
		this.tagInfo = tokenizer.nextToken();
	}
	
	public String getId() {
		return id;
	}

	public String getTagInfo() {
		return tagInfo;
	}


}
