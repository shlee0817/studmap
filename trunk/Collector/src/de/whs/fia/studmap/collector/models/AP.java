package de.whs.fia.studmap.collector.models;

public class AP {

	private int Id;
	
	private int ScanId;
	
	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getScanId() {
		return ScanId;
	}

	public void setScanId(int scanId) {
		ScanId = scanId;
	}

	private String BSSID;
	
	private int RSS;

	public String getBSSID() {
		return BSSID;
	}

	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}

	public int getRSS() {
		return RSS;
	}

	public void setRSS(int rSS) {
		RSS = rSS;
	}

	@Override
	public String toString() {
		return "AP [BSSID=" + BSSID + ", RSS=" + RSS + "]";
	}
}
