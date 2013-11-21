package de.whs.studmap.web;

import org.json.JSONObject;

public class WebServiceException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSONObject jsonObject = null;
	
	public WebServiceException(JSONObject jsonObject){
		this.jsonObject = jsonObject;
	}
	
	public JSONObject getJsonObject(){ return jsonObject;}

}
