package de.whs.studmap.client.core.web;

public interface ResponseError {
	public static final int TaskCancelled = -2;
	public static final int ConnectionError = -1;
	public static final int None = 0;

	public static final int DatabaseError = 1;
	public static final int UnknownError = 2;

	public static final int UserNameDuplicate = 101;
	public static final int UserNameInvalid = 102;
	public static final int PasswordInvalid = 103;

	public static final int LoginInvalid = 110;

	public static final int MapIdDoesNotExist = 201;
	public static final int FloorIdDoesNotExist = 202;
	public static final int NodeIdDoesNotExist = 203;
	
	public static final int NoRouteFound = 301;
	public static final int StartNodeNotFound = 302;
	public static final int EndNodeNotFound = 303;

	public static final int PoiTypeIdDoesNotExist = 401;
	public static final int NFCTagDoesNotExist = 402;
	public static final int QRCodeDosNotExist = 403;
	public static final int PoiDoesNotExist = 404;
	public static final int QRCodeIsNullOrEmpty = 405;
	public static final int NFCTagIsNullOrEmpty = 406;
	public static final int NFCTagAllreadyAssigned = 407;
	
	public static final int FingeprintIsNotDefined = 501;

}
