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

import de.whs.studmap.data.Constants;
import de.whs.studmap.data.Floor;
import de.whs.studmap.data.Node;
import de.whs.studmap.data.PoI;

public class Service implements Constants {
	
	public static boolean login(String name, String password) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		httpPost(URL_USER,METHOD_LOGIN, params);

		return true;
	}
	
	public static boolean logout(String name) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		
		httpPost(URL_USER, METHOD_LOGOUT, params);
		
		return true;
	}
	
	public static boolean register(String name, String password) throws WebServiceException, ConnectException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_USERNAME, name));
		params.add(new BasicNameValuePair(REQUEST_PARAM_PASSWORD, password));
		
		httpPost(URL_USER, METHOD_REGISTER, params);
		
		return true;
	}
	
	public static List<PoI> getPOIs() throws WebServiceException, ConnectException{
		List<PoI> poiList = new ArrayList<PoI>();
		
		
		try {
			JSONObject pois = httpGet(URL_MAPS, METHOD_GETPOIS);
			JSONArray poiArray = pois.getJSONArray(RESPONSE_PARAM_LIST);
			
			for (int i = 0; i < poiArray.length(); i++){
				JSONObject o = poiArray.getJSONObject(i);
				PoI poi = parseJsonToPoI(o);
				poiList.add(poi);
			}
			
		} catch (JSONException ignore) {
			ignore.printStackTrace();
		}	
		
		return poiList;
	}
	
	public static List<Node> getRooms(int mapId) throws WebServiceException, ConnectException{
		List<Node> nodes = new ArrayList<Node>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_MAPID, String.valueOf(mapId)));
		
		try {
			JSONObject rooms = httpGet(URL_MAPS,METHOD_GETROOMS,params);
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
	
	public static List<Floor> getFloorsForMap(int mapId) throws WebServiceException, ConnectException{
		List<Floor> floorList = new ArrayList<Floor>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_MAPID, String.valueOf(mapId)));
		
		try {
			JSONObject floors = httpGet(URL_MAPS, METHOD_GETFLOORS, params);
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
		String result = httpGet(URL_USER, "GetActiveUsers").toString();
		//TODO: parse activeUsers
		return result;
	}
  
	private static JSONObject httpPost(String url, String methodName, List<NameValuePair> params) 
			throws WebServiceException, ConnectException {
		
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		 
			try {
				// Add data
				String entityString = URLEncodedUtils.format(params, "utf-8");
				HttpPost httpPost = new HttpPost(url + methodName + "?" + entityString);
 
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
	
	private static JSONObject httpGet(String url, String methodName) throws WebServiceException, ConnectException{
		return httpGet(url, methodName, null);
	}
	
	private static JSONObject httpGet(String url, String methodName, List<NameValuePair> params) 
			throws WebServiceException, ConnectException {
				
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url + methodName);
		
		try {
			
			if (params != null) {
				String paramString = URLEncodedUtils.format(params, "utf-8");
				httpGet = new HttpGet(url + methodName + "?" + paramString);
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
			node = new Node(id,roomName,displayName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return node;
	}
	
	private static Floor parseJsonToFloor(JSONObject o){
		Floor floor = null;
		try {			
			int id = o.getInt(RESPONSE_PARAM_FLOOR_ID);
			int mapId = o.getInt(RESPONSE_PARAM_FLOOT_MAPID);
			String imageUrl = o.getString(RESPONSE_PARAM_FLOOR_IMAGE_URL);
			String name = o.getString(RESPONSE_PARAM_FLOOR_NAME);
			floor = new Floor(id, mapId, imageUrl, name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return floor;
	}
	
	private static PoI parseJsonToPoI(JSONObject o){
		PoI poi = null;
		try {			
			String description = o.getString(RESPONSE_PARAM_POI_DESCRIPTION);
			int nodeId = o.getInt(RESPONSE_PARAM_POI_NODEID);
			JSONObject type = o.getJSONObject(RESPONSE_PARAM_POI_TYPE);
			int typeId = type.getInt(RESPONSE_PARAM_POI_TYPEID);
			String name = type.getString(RESPONSE_PARAM_POI_NAME);
			poi = new PoI(name, description, typeId,nodeId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return poi;
	}
	 
}
