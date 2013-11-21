package de.whs.studmap.data;

public class AccessPoint {
	private int Id;
	private String MAC;
	private int ReceivedSignalStrength;
	
	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getMAC() {
		return MAC;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public AccessPoint()
	{
		MAC = "";
	}

	public int getReceivedSignalStrength() {
		return ReceivedSignalStrength;
	}

	public void setReceivedSignalStrength(int receivedSignalStrength) {
		ReceivedSignalStrength = receivedSignalStrength;
	}
}
