package de.whs.studmap.data;

public interface Constants {
    	//TEST
		 static final int MAP_ID = 3;
	
		//Common responses
		 static final String RESPONSE_STATUS = "Status";
		 static final String RESPONSE_ERRORCODE = "ErrorCode";

		 static final String RESPONSE_PARAM_LIST = "List";
		 static final String RESPONSE_PARAM_OBJECT = "Object";
		
		//Methods
		 static final String METHOD_LOGIN = "Login";
		 static final String METHOD_LOGOUT = "Logout";
		 static final String METHOD_REGISTER = "Register";
		 static final String METHOD_GETPOIS = "GetPoIsForMap";
		 static final String METHOD_GETROOMS = "GetRoomsForMap";
		 static final String METHOD_GETFLOORS = "GetFloorsForMap";
		 static final String METHOD_GETNODEINFOFORNODE = "getNodeInformationForNode";
			
		//UserController
		 static final String REQUEST_PARAM_USERNAME = "userName";
		 static final String REQUEST_PARAM_PASSWORD = "password";
		 
		 //MapController
		 static final String REQUEST_PARAM_MAPID = "mapId";
		 static final String REQUEST_PARAM_NODEID = "nodeId";
		
		//Node
		 static final String RESPONSE_PARAM_NODE_ROOMNAME = "RoomName";
		 static final String RESPONSE_PARAM_NODE_DISPLAYNAME = "DisplayName";
		 static final String RESPONSE_PARAM_NODE_ID = "NodeId";
		 static final String RESPONSE_PARAM_NODE_FLOOR_ID = "FloorId";
		
		//Floor
		 static final String RESPONSE_PARAM_FLOOR_ID = "Id";
		 static final String RESPONSE_PARAM_FLOOT_MAPID = "MapId";
		 static final String RESPONSE_PARAM_FLOOR_NAME = "Name";
		 static final String RESPONSE_PARAM_FLOOR_IMAGE_URL = "ImageUrl";
		
		 //PoI
		 static final String RESPONSE_PARAM_POI_NAME = "Name";
		 static final String RESPONSE_PARAM_POI_TYPEID = "Id";
		 static final String RESPONSE_PARAM_POI_ROOM = "Room";
		 static final String RESPONSE_PARAM_POI_POI = "PoI";
		 static final String RESPONSE_PARAM_POI_TYPE = "Type";
		 static final String RESPONSE_PARAM_POI_DESCRIPTION = "Description";
		 
		//Urls
		 static final String URL_USER = "http://193.175.199.115:80/StudMapService/api/Users/";
		 static final String URL_MAPS = "http://193.175.199.115:80/StudMapService/api/Maps/";
		 
		 //Log tags
		 static final String LOG_TAG_WEBSERVICE = "WebService";
		 static final String LOG_TAG_POI__ACTIVITY = "PoI Activity";
		 static final String LOG_TAG_MAIN_ACTIVITY = "Main Activity";
		 static final String LOG_TAG_LOGIN_ACTIVITY = "Login Activity";
		 static final String LOG_TAG_REGISTER_ACTIVITY = "Register Activity";
		 static final String LOG_TAG_POSITION_ACTIVITY = "Position_Activity";
		
}
