package de.whs.studmap.client.core.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Node implements Parcelable {
	private int nodeID;
	private String roomName;
	private String displayName;
	private int floorID;

	public Node(int nodeID, String roomName, String displayName, int floorID) {
		this.nodeID = nodeID;
		this.roomName = roomName;
		this.displayName = displayName;
		this.floorID = floorID;
	}
	
    private Node(Parcel in) {
        nodeID = in.readInt();
        roomName = in.readString();
        displayName = in.readString();
        floorID = in.readInt();
    }

	public int getNodeID() {
		return nodeID;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getRoomName() {
		return roomName;
	}

	@Override
	public String toString() {

		return roomName + "-" + displayName;
	}

	public int getFloorID() {
		return floorID;
	}

	public void setFloorID(int floorID) {
		this.floorID = floorID;
	}

	@Override
	public int describeContents() {
		// we do not need a description!
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
        // same order as in constructor(parcel)
		dest.writeInt(nodeID);
        dest.writeString(roomName);
        dest.writeString(displayName);
        dest.writeInt(floorID);
	}

	public static final Parcelable.Creator<Node> CREATOR = new Parcelable.Creator<Node>() {
		public Node createFromParcel(Parcel in) {
			return new Node(in);
		}

		public Node[] newArray(int size) {
			return new Node[size];
		}
	};
}
