package de.whs.studmap.data;

public class AccessPointScan {

	private int ReceivedSignalStrength;
	private AccessPoint AccessPoint;
	
	public int getReceivedSignalStrength() {
		return ReceivedSignalStrength;
	}
	public void setReceivedSignalStrength(int receivedSignalStrength) {
		ReceivedSignalStrength = receivedSignalStrength;
	}
	public AccessPoint getAccessPoint() {
		return AccessPoint;
	}
	public void setAccessPoint(AccessPoint accessPoint) {
		AccessPoint = accessPoint;
	}
}
