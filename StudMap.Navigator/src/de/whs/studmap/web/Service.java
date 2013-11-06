package de.whs.studmap.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProtocolException;
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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonReader;

import de.whs.studmap.data.Node;

public class Service {
	
	private static final String RESPONSE_STATUS = "Status";
	private static final String REQUEST_PARAM_USERNAME = "userName";
	private static final String REQUEST_PARAM_PASSWORD = "password";
	
	
	private static final String URL = "http://10.0.2.2:1129/api/Users/";
	
	public static boolean login(String name, String password) throws WebServiceException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		JSONObject jObject =  httpPost("Login", params);
		
		if (jObject == null)
			return false;
		else		
			return true;
	}
	
	public static boolean logout(String name) throws WebServiceException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		
		JSONObject jObject = httpPost("Logout", params);
		
		if (jObject == null)
			return false;
		else		
			return true;
	}
	
	public static boolean register(String name, String password) throws WebServiceException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		JSONObject jObject = httpPost("Register", params);
		
		if (jObject == null)
			return false;
		else		
			return true;
	}
	
	public static List<Node> getPOIs() throws WebServiceException{
		List<Node> nodes = new ArrayList<Node>();
		
		//JSONObject pois = httpGet("GetPOIs");
		//TODO: parse "pois"
		
		return nodes;
	}
	
	public static String getActiveUsers() throws WebServiceException{
		//Wird eigentlich noch garnicht benötigt, eignet sich aber gut zu Testzwecken
		String result = httpGet("GetActiveUsers").toString();
		return result;
	}
  
	private static JSONObject httpPost(String methodName, List<NameValuePair> params) throws WebServiceException {
		
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
					//result = convertStreamToString(entity.getContent());
					String responseString = EntityUtils.toString(entity); 			  
					JSONObject jObject = new JSONObject(responseString);
					
					int status = jObject.getInt(RESPONSE_STATUS);
					if (status == ResponseStatus.Ok)
						return jObject;
					else
						throw new WebServiceException(jObject);
				}				
 
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
	}
	
	private static JSONObject httpGet(String methodName) throws WebServiceException{
		return httpGet(methodName, null);
	}
	
	private static JSONObject httpGet(String methodName, List<NameValuePair> params) 
			throws WebServiceException {
				
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL + methodName);
		
		try {
			
			if (params != null) {
				String paramString = URLEncodedUtils.format(params, "utf-8");
				httpGet = new HttpGet(URL + methodName + "?" + paramString);
			}
			
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			
			if (entity != null){
				//result = convertStreamToString(entity.getContent());
				String responseString = EntityUtils.toString(entity); 			  
				JSONObject jObject = new JSONObject(responseString);
				
				int status = jObject.getInt(RESPONSE_STATUS);
				if (status == ResponseStatus.Ok)
					return jObject;
				else
					throw new WebServiceException(jObject);
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
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
