package de.whs.studmap.data;

public interface Constants {
    	//TEST
		 static final int MAP_ID = 3;
	
		//Common responses
		 static final String RESPONSE_STATUS = "Status";
		 static final String RESPONSE_ERRORCODE = "ErrorCode";

		 static final String RESPONSE_PARAM_LIST = "List";
		
		//Methods
		 static final String METHOD_LOGIN = "Login";
		 static final String METHOD_LOGOUT = "Logout";
		 static final String METHOD_REGISTER = "Register";
		 static final String METHOD_GETPOIS = "GetPoIsForMap";
		 static final String METHOD_GETROOMS = "GetRoomsForMap";
		 static final String METHOD_GETFLOORS = "GetFloorsForMap";
			
		//UserController
		 static final String REQUEST_PARAM_USERNAME = "userName";
		 static final String REQUEST_PARAM_PASSWORD = "password";
		 
		 //MapController
		 static final String REQUEST_PARAM_MAPID = "mapId";
		
		//Node
		 static final String RESPONSE_PARAM_NODE_ROOMNAME = "RoomName";
		 static final String RESPONSE_PARAM_NODE_DISPLAYNAME = "DisplayName";
		 static final String RESPONSE_PARAM_NODE_ID = "NodeId";
		
		//Floor
		 static final String RESPONSE_PARAM_FLOOR_ID = "Id";
		 static final String RESPONSE_PARAM_FLOOT_MAPID = "MapId";
		 static final String RESPONSE_PARAM_FLOOR_NAME = "Name";
		 static final String RESPONSE_PARAM_FLOOR_IMAGE_URL = "ImageUrl";
		
		 //PoI
		 static final String RESPONSE_PARAM_POI_NAME = "Name";
		 static final String RESPONSE_PARAM_POI_TYPEID = "Id";
		 static final String RESPONSE_PARAM_POI_NODEID = "NodeId";
		 static final String RESPONSE_PARAM_POI_TYPE = "Type";
		 static final String RESPONSE_PARAM_POI_DESCRIPTION = "Description";
		 
		//Urls
		 static final String URL_USER = "http://193.175.199.115:80/StudMapService/api/Users/";
		 static final String URL_MAPS = "http://193.175.199.115:80/StudMapService/api/Maps/";
		
}
