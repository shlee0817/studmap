package de.whs.studmap.client.listener;

import java.util.List;

import de.whs.studmap.client.core.data.Node;

public interface OnGetRoomsTaskListener {

	public void onGetRoomsSuccess(List<Node> nodes);
	
	public void onGetRoomsError(int responseError);
	
	public void onGetRoomsCanceled();

}
