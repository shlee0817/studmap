package de.whs.studmap.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import de.whs.studmap.data.Node;

public class Service {
	
	private static final String URL = "http://10.0.2.2:1129/api/Users/";
	
	public static String login(String name, String password){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", name));
		params.add(new BasicNameValuePair("password", password));
		
		return httpPost("Login", params);		
	}
	
	public static List<Node> getPOIs(){
		List<Node> nodes = new ArrayList<Node>();
		
		String pois = httpGet("GetPOIs");
		//TODO: parse POI string "pois"
		
		return nodes;
	}
	
	public static String getActiveUsers(){
		return httpGet("GetActiveUsers");
	}
	
	
  
	private static String httpPost(String methodName, List<NameValuePair> params) {
		String result = "";
		
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL + methodName);
 
			try {
				// Add data
				httpPost.setEntity(new UrlEncodedFormEntity(params));
 
				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				
				if (entity != null){
					result = convertStreamToString(entity.getContent());
				}
				return result;
 
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				return "";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return "";
			} catch(Exception e){
				return "";
			}
			
	}
	
	private static String httpGet(String methodName){
		return httpGet(methodName, null);
	}
	
	private static String httpGet(String methodName, List<NameValuePair> params){
		String result = "";
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL + methodName);
		
		try {
			//TODO : params iwie einbringen - auf "null" prüfen
			
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			
			if (entity != null){
				result = convertStreamToString(entity.getContent());
			}
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
	
	private static String convertStreamToString(InputStream is) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder result = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            result.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return result.toString();
	}
 
}
