package de.whs.studmap.web;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.whs.studmap.data.Floor;
import de.whs.studmap.data.Node;

public class Service {
	
	public static final String RESPONSE_STATUS = "Status";
	public static final String RESPONSE_ERRORCODE = "ErrorCode";
	private static final String REQUEST_PARAM_USERNAME = "userName";
	private static final String REQUEST_PARAM_PASSWORD = "password";
	
	//Methods
	private static final String METHOD_LOGIN = "Login";
	private static final String METHOD_LOGOUT = "Logout";
	private static final String METHOD_REGISTER = "Register";
	private static final String METHOD_GETPOIS = "getPois";
	private static final String METHOD_GETROOMS = "getRooms";
	private static final String METHOD_GETFLOORS = "getFloors";
	
	private static final String RESPONSE_PARAM_LIST = "list";
	
	//Node
	private static final String RESPONSE_PARAM_NODE_ROOMNAME = "roomName";
	private static final String RESPONSE_PARAM_NODE_DISPLAYNAME = "displayName";
	private static final String RESPONSE_PARAM_NODE_X = "x";
	private static final String RESPONSE_PARAM_NODE_Y = "y";
	private static final String RESPONSE_PARAM_NODE_ID = "id";
	
	//Floor
	private static final String RESPONSE_PARAM_FLOOR_ID = "id";
	private static final String RESPONSE_PARAM_FLOOR_NAME = "name";
	private static final String RESPONSE_PARAM_FLOOR_URL = "url";
	
	private static final String URL = "http://10.0.2.2:1120/api/Users/";
	
	public static boolean login(String name, String password) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		httpPost(METHOD_LOGIN, params);

		return true;
	}
	
	public static boolean logout(String name) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		
		httpPost(METHOD_LOGOUT, params);
		
		return true;
	}
	
	public static boolean register(String name, String password) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		httpPost(METHOD_REGISTER, params);
		
		return true;
	}
	
	public static List<Node> getPOIs() throws WebServiceException{
		List<Node> nodes = new ArrayList<Node>();
		
		//JSONObject pois = httpGet(METHOD_GETPOIS);
		//TODO: parse "pois"
		
		return nodes;
	}
	
	public static List<Node> getAllRooms() throws WebServiceException, ConnectException{
		List<Node> nodes = new ArrayList<Node>();
		
		try {
			JSONObject rooms = httpGet(METHOD_GETROOMS);
			JSONArray roomArray = rooms.getJSONArray(RESPONSE_PARAM_LIST);
			
			for (int i = 0; i < roomArray.length(); i++){
				JSONObject o = roomArray.getJSONObject(i);
				Node node = parseJsonToNode(o);
				nodes.add(node);
			}
			
		} catch (JSONException ignore) {
			ignore.printStackTrace();
		}		
		
		return nodes;		
	}
	
	public static List<Floor> getFloors() throws WebServiceException, ConnectException{
		List<Floor> floorList = new ArrayList<Floor>();
		
		try {
			JSONObject floors = httpGet(METHOD_GETFLOORS);
			JSONArray roomArray = floors.getJSONArray(RESPONSE_PARAM_LIST);
			
			for (int i = 0; i < roomArray.length(); i++){
				JSONObject o = roomArray.getJSONObject(i);
				Floor floor = parseJsonToFloor(o);
				floorList.add(floor);
			}
			
		} catch (JSONException ignore) {
			ignore.printStackTrace();
		}		
		
		return floorList;		
	}
	
	public static String getActiveUsers() throws WebServiceException, ConnectException{
		//Wird eigentlich noch garnicht benötigt, eignet sich aber gut zu Testzwecken
		String result = httpGet("GetActiveUsers").toString();
		//TODO: parse activeUsers
		return result;
	}
  
	private static JSONObject httpPost(String methodName, List<NameValuePair> params) 
			throws WebServiceException, ConnectException {
		
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		 
			try {
				// Add data
				String entityString = URLEncodedUtils.format(params, "utf-8");
				HttpPost httpPost = new HttpPost(URL + methodName + "?" + entityString);
 
				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				
				if (entity != null){
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
			
			throw new ConnectException();
	}
	
	private static JSONObject httpGet(String methodName) throws WebServiceException, ConnectException{
		return httpGet(methodName, null);
	}
	
	private static JSONObject httpGet(String methodName, List<NameValuePair> params) 
			throws WebServiceException, ConnectException {
				
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
		
		throw new ConnectException();
	}
	
	private static Node parseJsonToNode(JSONObject o){
		Node node = null;		
		try {
			int id = o.getInt(RESPONSE_PARAM_NODE_ID);
			String roomName = o.getString(RESPONSE_PARAM_NODE_ROOMNAME);
			String displayName = o.getString(RESPONSE_PARAM_NODE_DISPLAYNAME);
			int x = o.getInt(RESPONSE_PARAM_NODE_X);
			int y = o.getInt(RESPONSE_PARAM_NODE_Y);
			node = new Node(id,roomName,displayName,x,y);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return node;
	}
	
	private static Floor parseJsonToFloor(JSONObject o){
		Floor floor = null;
		try {			
			int id = o.getInt(RESPONSE_PARAM_FLOOR_ID);
			String url = o.getString(RESPONSE_PARAM_FLOOR_URL);
			String name = o.getString(RESPONSE_PARAM_FLOOR_NAME);
			floor = new Floor(id, url, name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return floor;
	}
	 
}
