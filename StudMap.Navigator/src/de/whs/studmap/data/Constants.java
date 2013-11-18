package de.whs.studmap.data;

public interface Constants {
	
		//Common responses
		 static final String RESPONSE_STATUS = "Status";
		 static final String RESPONSE_ERRORCODE = "ErrorCode";

		 static final String RESPONSE_PARAM_LIST = "list";
		
		//Methods
		 static final String METHOD_LOGIN = "Login";
		 static final String METHOD_LOGOUT = "Logout";
		 static final String METHOD_REGISTER = "Register";
		 static final String METHOD_GETPOIS = "getPois";
		 static final String METHOD_GETROOMS = "getRooms";
		 static final String METHOD_GETFLOORS = "getFloors";
			
		//User
		 static final String REQUEST_PARAM_USERNAME = "userName";
		 static final String REQUEST_PARAM_PASSWORD = "password";
		
		//Node
		 static final String RESPONSE_PARAM_NODE_ROOMNAME = "roomName";
		 static final String RESPONSE_PARAM_NODE_DISPLAYNAME = "displayName";
		 static final String RESPONSE_PARAM_NODE_X = "x";
		 static final String RESPONSE_PARAM_NODE_Y = "y";
		static final String RESPONSE_PARAM_NODE_ID = "id";
		
		//Floor
		 static final String RESPONSE_PARAM_FLOOR_ID = "id";
		 static final String RESPONSE_PARAM_FLOOR_NAME = "name";
		 static final String RESPONSE_PARAM_FLOOR_URL = "url";
		
		//Urls
		 static final String URL_USER = "http://139.175.199.115:80/StudMapService/api/Users/";
		 static final String URL_MAPS = "http://139.175.199.115:80/StudMapService/api/Maps/";
		
}
